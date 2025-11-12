package fishbot.core.game;

import fishbot.core.event.BotEventBus;
import fishbot.utils.Logger;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class GameController {

    private Robot robot;
    private final Logger logger;

    public GameController(BotEventBus eventBus) {
        this.logger = new Logger(eventBus);

        try {
            this.robot = new Robot();
            this.robot.setAutoDelay(50);
        } catch (AWTException e) {
            logger.error("Internal Critical Error, Controller: " + e.getMessage());
        }
    }

    public void pressKey(int keyCode) {
        logger.info("Pressing Key" + keyCode);
        robot.keyPress(keyCode);
        sleep(100);
        robot.keyRelease(keyCode);
        sleep(100);
    }

    public void keyDown(int keyCode) {
        logger.info("Holding Key" + keyCode);
        robot.keyPress(keyCode);
    }

    public void keyUp(int keyCode) {
        logger.info("Releasing Key" + keyCode);
        robot.keyRelease(keyCode);
    }

    public void clickLeft() {
        logger.info("Pressing Left Button");
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        sleep(50);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        sleep(150);
    }

    public void moveTo(int x, int y) {
        logger.info("Moving Cursor to: (" + x + ", " + y + ")");
        Point start = MouseInfo.getPointerInfo().getLocation();
        int steps = 10;
        for (int i = 0; i <= steps; i++) {
            int curX = start.x + ((x - start.x) * i / steps);
            int curY = start.y + ((y - start.y) * i / steps);
            robot.mouseMove(curX, curY);
            sleep(10);
        }
        sleep(100);
    }

    public void mouseDownLeft() {
        logger.info("Holding Left Button");
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        sleep(100);
    }

    public void mouseUpLeft() {
        logger.info("Releasing Left Button");
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        sleep(100);
    }

    public void releaseAllControls() {
        logger.info("Releasing all Buttons...");
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
        robot.keyRelease(KeyEvent.VK_A);
        robot.keyRelease(KeyEvent.VK_D);
        robot.keyRelease(KeyEvent.VK_S);
        robot.keyRelease(KeyEvent.VK_F);
        robot.keyRelease(KeyEvent.VK_M);
        sleep(100);
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }
}
