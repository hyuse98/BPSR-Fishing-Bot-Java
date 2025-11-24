package fishbot.core.state.impl;

import fishbot.core.event.BotEventBus;
import fishbot.core.FishingBot;
import fishbot.core.state.BaseState;
import fishbot.core.state.StateType;
import fishbot.utils.Logger;

import java.awt.*;
import java.awt.image.BufferedImage;

public class WaitingForBiteState extends BaseState {

    private final Logger logger;

    private long lastLog = 0;

    public WaitingForBiteState(FishingBot bot, BotEventBus eventBus) {
        super(bot);
        this.logger = new Logger(eventBus);
    }

    @Override
    public StateType handle(BufferedImage screen) {

        Point pos = detector.find(screen, "exclamation");

        if (pos != null) {
            logger.info("Fish Bit the Bait");
            controller.mouseDownLeft();
            return StateType.PLAYING_MINIGAME;
        }

        long current = System.currentTimeMillis();
        if (current - lastLog > 5000) {
            logger.info("Waiting Fish Bit the Bait...");
            lastLog = current;
        }
        return StateType.WAITING_FOR_BITE;
    }
}
