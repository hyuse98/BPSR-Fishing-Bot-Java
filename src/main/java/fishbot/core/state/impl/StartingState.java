package fishbot.core.state.impl;

import fishbot.core.event.BotEventBus;
import fishbot.core.FishingBot;
import fishbot.core.state.BaseState;
import fishbot.core.state.StateType;
import fishbot.utils.Logger;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class StartingState extends BaseState {

    private final Logger logger;

    private long lastSearchLog = 0;

    public StartingState(FishingBot bot, BotEventBus eventBus) {
        super(bot);
        this.logger = new Logger(eventBus);
    }

    @Override
    public StateType handle(BufferedImage screen) {

        Point spotPos = detector.find(screen, "fishing_spot_btn");

        if (spotPos != null) {
            logger.info("Fishing Spot Found...");
            sleep(500);
            logger.info("Pressing F...");
            controller.pressKey(KeyEvent.VK_F);
            sleep(2000);
            return StateType.CHECKING_ROD;
        }

        long current = System.currentTimeMillis();
        if (current - lastSearchLog > 2000) {
            logger.info("Searching Fishing Spot...");
            lastSearchLog = current;
        }

        return StateType.STARTING;
    }
}