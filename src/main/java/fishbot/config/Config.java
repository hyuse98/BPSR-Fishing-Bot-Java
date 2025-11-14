package fishbot.config;

import fishbot.core.state.StateType;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Config {
    public static final String TEMPLATES_PATH = "/templates/";

    public static double DETECTION_PRECISION;
    public static int START_COUNTDOWN;
    public static int TARGET_FPS;
    public static boolean QUICK_SKIP_MODE;

    public static int MONITOR_X;
    public static int MONITOR_Y;
    public static int MONITOR_WIDTH;
    public static int MONITOR_HEIGHT;

    public static final Map<String, Rectangle> ROIS = new HashMap<>();
    public static final Map<String, String> TEMPLATES = new HashMap<>();
    public static final Map<StateType, Integer> TIMEOUTS = new HashMap<>();

    static {
        ConfigData data = ConfigLoader.load();

        DETECTION_PRECISION = data.detectionPrecision;
        START_COUNTDOWN = data.startCountdown;
        TARGET_FPS = data.targetFps;
        QUICK_SKIP_MODE = data.quickSkipMode;

        MONITOR_X = data.monitor.x;
        MONITOR_Y = data.monitor.y;
        MONITOR_WIDTH = data.monitor.width;
        MONITOR_HEIGHT = data.monitor.height;

        TEMPLATES.putAll(data.templates);

        for (Map.Entry<String, ConfigData.RoiConfig> entry : data.rois.entrySet()) {
            ConfigData.RoiConfig roi = entry.getValue();
            ROIS.put(entry.getKey(), new Rectangle(roi.x, roi.y, roi.width, roi.height));
        }

        for (Map.Entry<String, Integer> entry : data.timeouts.entrySet()) {
            TIMEOUTS.put(StateType.valueOf(entry.getKey()), entry.getValue());
        }
    }

    public static void updateRoi(String key, java.awt.Rectangle rect) {
        ROIS.put(key, rect);
    }

    public static Point getRoiCenter(String key) {
        Rectangle roi = ROIS.get(key);
        if (roi == null) return null;
        return new Point(roi.x + roi.width / 2, roi.y + roi.height / 2);
    }

    public static void updateMonitor(int x, int y, int width, int height) {
        MONITOR_X = x;
        MONITOR_Y = y;
        MONITOR_WIDTH = width;
        MONITOR_HEIGHT = height;
    }

    public static void saveToFile() {
        ConfigData data = new ConfigData();
        data.detectionPrecision = DETECTION_PRECISION;
        data.startCountdown = START_COUNTDOWN;
        data.targetFps = TARGET_FPS;
        data.quickSkipMode = QUICK_SKIP_MODE;

        data.monitor.x = MONITOR_X;
        data.monitor.y = MONITOR_Y;
        data.monitor.width = MONITOR_WIDTH;
        data.monitor.height = MONITOR_HEIGHT;

        data.templates.putAll(TEMPLATES);

        for (Map.Entry<String, Rectangle> entry : ROIS.entrySet()) {
            Rectangle r = entry.getValue();
            data.rois.put(entry.getKey(), new ConfigData.RoiConfig(r.x, r.y, r.width, r.height));
        }

        for (Map.Entry<StateType, Integer> entry : TIMEOUTS.entrySet()) {
            data.timeouts.put(entry.getKey().name(), entry.getValue());
        }

        ConfigLoader.save(data);
    }
}
