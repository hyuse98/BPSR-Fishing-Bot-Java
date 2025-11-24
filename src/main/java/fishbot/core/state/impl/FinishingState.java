package fishbot.core.state.impl;

import fishbot.config.Config;
import fishbot.core.FishingBot;
import fishbot.core.event.BotEventBus;
import fishbot.core.state.BaseState;
import fishbot.core.state.StateType;
import fishbot.utils.Logger;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class FinishingState extends BaseState {

    private final Logger logger;

    public FinishingState(FishingBot bot, BotEventBus eventBus) {
        super(bot);
        this.logger = new Logger(eventBus);
    }

    @Override
    public StateType handle(BufferedImage screen) {

        if (detector.find(screen, "continue") == null) {
            logger.info("Pressing ESC...");
            controller.pressKey(KeyEvent.VK_ESCAPE);
            return StateType.STARTING;
        }

        if (detector.find(screen, "continue") != null) {
            logger.info("Continue BTN...");
            sleep(1000);
            Point pos = Config.getRoiCenter("continue");
            if (pos != null) {
                controller.moveTo(pos.x, pos.y);
            }
            sleep(1000);
            controller.clickLeft();
            return StateType.CHECKING_ROD;
        }

        if (detector.find(screen, "fishing_spot_btn") != null) {
            return StateType.STARTING;
        }

        return StateType.FINISHING;
    }
}