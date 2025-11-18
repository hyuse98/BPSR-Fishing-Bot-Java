package fishbot.core.game;

import fishbot.config.Config;
import fishbot.core.event.BotEventBus;
import fishbot.utils.Logger;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacv.Java2DFrameUtils;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Rect;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.bytedeco.opencv.global.opencv_core.minMaxLoc;
import static org.bytedeco.opencv.global.opencv_core.split;
import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_UNCHANGED;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imdecode;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

public class Detector {
    private Robot robot;
    private final BotEventBus eventBus;
    private final Rectangle captureArea;
    private final Map<String, TemplateData> templatesCache = new HashMap<>();
    private final Logger logger;

    public Detector(BotEventBus eventBus) {
        this.eventBus = eventBus;
        this.logger = new Logger(eventBus);
        this.captureArea = new Rectangle(Config.MONITOR_X, Config.MONITOR_Y, Config.MONITOR_WIDTH, Config.MONITOR_HEIGHT);
        try {
            this.robot = new Robot();
            loadTemplates();
        } catch (AWTException e) {
            logger.error("Internal Critical Error: " + e.getMessage());
        }
    }

    private static class TemplateData {
        Mat grayImage;
        Mat mask;

        TemplateData(Mat grayImage, Mat mask) {
            this.grayImage = grayImage;
            this.mask = mask;
        }
    }

    private void loadTemplates() {

        logger.info("Loading Templates...");

        // Resolve external templates folder next to the JAR
        java.nio.file.Path externalDir = null;
        try {
            java.nio.file.Path jarPath = java.nio.file.Path.of(
                    Detector.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            java.nio.file.Path jarDir = java.nio.file.Files.isDirectory(jarPath) ? jarPath : jarPath.getParent();
            externalDir = jarDir.resolve("templates");
        } catch (Exception e) {
            // Fallback to CWD
            externalDir = java.nio.file.Path.of("templates");
        }

        for (Map.Entry<String, String> entry : Config.TEMPLATES.entrySet()) {
            String fileName = entry.getValue();
            byte[] bytes = null;
            String source;

            // Try external folder first
            java.nio.file.Path externalFile = externalDir.resolve(fileName);
            if (java.nio.file.Files.exists(externalFile)) {
                try {
                    bytes = java.nio.file.Files.readAllBytes(externalFile);
                    source = "external: " + externalFile.toAbsolutePath();
                } catch (Exception e) {
                    logger.error("Failed to read external template: " + externalFile + ": " + e.getMessage());
                    bytes = null;
                    source = null;
                }
            } else {
                source = null;
            }

            // Fallback to classpath resource
            if (bytes == null) {
                String resourcePath = Config.TEMPLATES_PATH + fileName;
                try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
                    if (is == null) {
                        logger.error("Template not found (external or classpath): " + fileName);
                        continue;
                    }
                    bytes = is.readAllBytes();
                    source = "classpath: " + resourcePath;
                } catch (Exception e) {
                    logger.error("Failed to read classpath template: " + fileName + ": " + e.getMessage());
                    continue;
                }
            }

            try {
                Mat img = imdecode(new Mat(bytes), IMREAD_UNCHANGED);

                if (img == null || img.empty()) {
                    logger.error("Failed to decode template: " + fileName);
                    continue;
                }

                Mat templateGray = new Mat();
                Mat mask = new Mat();

                if (img.channels() == 4) {
                    MatVector channels = new MatVector();
                    split(img, channels);

                    mask = channels.get(3).clone();

                    threshold(mask, mask, 127, 255, THRESH_BINARY);

                    Mat bgr = new Mat();
                    cvtColor(img, bgr, COLOR_BGRA2BGR);
                    cvtColor(bgr, templateGray, COLOR_BGR2GRAY);

                    bgr.release();
                    channels.close();
                } else {
                    cvtColor(img, templateGray, COLOR_BGR2GRAY);
                }

                templatesCache.put(entry.getKey(), new TemplateData(templateGray, mask));
                logger.info("Loaded: " + entry.getKey() + " <- " + source);
                img.release();

            } catch (Exception e) {
                logger.error("Exception processing template " + fileName + ": " + e.getMessage());
            }
        }
    }

    public BufferedImage captureScreen() {
        return robot.createScreenCapture(captureArea);
    }

    public java.awt.Point find(BufferedImage screen, String templateName) {
        TemplateData templateData = templatesCache.get(templateName);
        Rectangle roi = Config.ROIS.get(templateName);

        if (templateData == null || roi == null) return null;

        int x = Math.max(0, roi.x);
        int y = Math.max(0, roi.y);
        int w = Math.min(roi.width, screen.getWidth() - x);
        int h = Math.min(roi.height, screen.getHeight() - y);

        if (w <= templateData.grayImage.cols() || h <= templateData.grayImage.rows()) return null;

        Mat screenMat = Java2DFrameUtils.toMat(screen);
        Mat screenGray = new Mat();

        if (screenMat.channels() == 4) {
            cvtColor(screenMat, screenGray, COLOR_BGRA2GRAY);
        } else if (screenMat.channels() == 3) {
            cvtColor(screenMat, screenGray, COLOR_BGR2GRAY);
        } else {
            screenMat.copyTo(screenGray);
        }

        Rect openCvRoi = new Rect(x, y, w, h);
        Mat searchArea = new Mat(screenGray, openCvRoi);
        Mat result = new Mat();

        if (!templateData.mask.empty()) {
            matchTemplate(searchArea, templateData.grayImage, result, TM_CCOEFF_NORMED, templateData.mask);
        } else {
            matchTemplate(searchArea, templateData.grayImage, result, TM_CCOEFF_NORMED);
        }

        DoublePointer minVal = new DoublePointer(1);
        DoublePointer maxVal = new DoublePointer(1);
        Point minLoc = new Point();
        Point maxLoc = new Point();

        minMaxLoc(result, minVal, maxVal, minLoc, maxLoc, null);

        double maxScore = Math.max(0.0, maxVal.get());

        //TODO(Watch it)
        double minAcuraccy = Config.DETECTION_PRECISION;
        if (templateName.equals("left_arrow") || templateName.equals("right_arrow")) {
            minAcuraccy = 0.40;
        } else if (templateName.equals("success") || templateName.equals("failure")) {
            minAcuraccy = 0.40;
        }

        java.awt.Point pointResult = null;

        if (maxScore >= minAcuraccy) {
            int centerX = x + maxLoc.x() + (templateData.grayImage.cols() / 2);
            int centerY = y + maxLoc.y() + (templateData.grayImage.rows() / 2);
            pointResult = new java.awt.Point(centerX, centerY);
            if (eventBus != null) {
                eventBus.publishDetectionUpdate(templateName, maxScore, true);
            }
        } else {
            if (eventBus != null) {
                eventBus.publishDetectionUpdate(templateName, maxScore, false);
            }
        }

        screenMat.release();
        screenGray.release();
        searchArea.release();
        result.release();
        minVal.close();
        maxVal.close();
        minLoc.close();
        maxLoc.close();

        return pointResult;
    }
}