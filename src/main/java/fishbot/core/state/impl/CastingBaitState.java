package fishbot.core.state.impl;

import fishbot.config.Config;
import fishbot.core.event.BotEventBus;
import fishbot.core.FishingBot;
import fishbot.core.state.BaseState;
import fishbot.core.state.StateType;
import fishbot.utils.Logger;

import java.awt.image.BufferedImage;

public class CastingBaitState extends BaseState {

    private final Logger logger;

    public CastingBaitState(FishingBot bot, BotEventBus eventBus) {
        super(bot);
        this.logger = new Logger(eventBus);
    }

    @Override
    public StateType handle(BufferedImage screen) {

        logger.info("Preparing to Trowing Bait...");
        sleep(1000);

        //TODO Change roi math
        int cx = Config.MONITOR_X + (Config.MONITOR_WIDTH / 2);
        int cy = Config.MONITOR_Y + (Config.MONITOR_HEIGHT / 2);

        controller.moveTo(cx, cy);
        sleep(1000);

        logger.info("Focusing Screen...");
        controller.clickLeft();
        sleep(500);

        logger.info("Trowing Bait...");
        controller.mouseDownLeft();
        sleep(100);
        controller.mouseUpLeft();
        sleep(1000);

        return StateType.WAITING_FOR_BITE;
    }
}
