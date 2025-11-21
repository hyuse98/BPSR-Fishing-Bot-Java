package fishbot.core.state.impl;

import fishbot.config.Config;
import fishbot.core.event.BotEventBus;
import fishbot.core.FishingBot;
import fishbot.core.state.BaseState;
import fishbot.core.state.StateType;
import fishbot.utils.Logger;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class PlayingMinigameState extends BaseState {

    private final Logger logger;

    private String currentDirection = null;
    private long lastActionTime = 0;

    private static final long COOLDOWN_MS = 350;

    public PlayingMinigameState(FishingBot bot, BotEventBus eventBus) {
        super(bot);
        this.logger = new Logger(eventBus);
    }

    @Override
    public StateType handle(BufferedImage screen) {

        boolean fishComplete = false;
        boolean failed = false;

        // Success flow - Normal Path
        if (detector.find(screen, "success") != null) {
            logger.info("Catch Successful");
            fishComplete = true;
        }

        // Continue Successful flow - Trigger when normal path fail
        if (detector.find(screen, "continue") != null) {
            logger.info("Catch Successful");
            fishComplete = true;
        }

        // Failure flow - Normal Path
        if (detector.find(screen, "failure") != null) {
            fishComplete = true;
            failed = true;
            logger.info("Fish Escaped");
        }

        //State Handler
        if (fishComplete) {
            controller.releaseAllControls();
            currentDirection = null;
            lastActionTime = 0;

            if (failed) {
                sleep(1000);
                return StateType.CHECKING_ROD;
            }

            if (Config.QUICK_SKIP_MODE) {
                sleep(1000);
                controller.pressKey(KeyEvent.VK_ESCAPE);
                return StateType.STARTING;
            }

            return StateType.FINISHING;
        }

        handleArrow("left", KeyEvent.VK_A, KeyEvent.VK_D, screen);
        handleArrow("right", KeyEvent.VK_D, KeyEvent.VK_A, screen);

        return StateType.PLAYING_MINIGAME;
    }

    private void handleArrow(String direction, int keyToPress, int keyToRelease, BufferedImage screen) {

        if (detector.find(screen, direction + "_arrow") != null) {
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastActionTime >= COOLDOWN_MS) {

                String oppositeDirection = direction.equals("left") ? "right" : "left";

                if (currentDirection == null) {
                    logger.info("Moving to: " + direction + " **HOLDING KEY...");
                    controller.keyDown(keyToPress);
                    currentDirection = direction;
                    lastActionTime = currentTime;

                } else if (currentDirection.equals(oppositeDirection)) {
                    logger.info("Change Direction **RELASING KEY");
                    controller.keyUp(keyToRelease);
                    currentDirection = null;
                    lastActionTime = currentTime;
                }
            }
        }
    }
}