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

public class CheckingRodState extends BaseState {

    private final Logger logger;

    public CheckingRodState(FishingBot bot, BotEventBus eventBus) { super(bot);
        this.logger = new Logger(eventBus);
    }

    @Override
    public StateType handle(BufferedImage screen) {

        logger.info("Verifying Fishing Pole...");
        sleep(1000);

        boolean isRodBroken = (
                detector.find(screen, "broken_rod") != null
        );

        if (isRodBroken) {

            logger.info("Fishing Pole not Found or Broken...");
            logger.info("Changing...");
            sleep(1000);

            logger.info("Pressing M...");
            controller.pressKey(KeyEvent.VK_M);
            sleep(1000);

            Point rodPos = Config.getRoiCenter("new_rod");
            if (rodPos != null) {
                controller.moveTo(rodPos.x, rodPos.y);
                sleep(1000);
                controller.clickLeft();
                sleep(1000);
            }

            logger.info("Fishing Pole Changed...");
        } else {
            sleep(1000);
            logger.info("Fishing Pole OK...");
        }
        return StateType.CASTING_BAIT;
    }
}
