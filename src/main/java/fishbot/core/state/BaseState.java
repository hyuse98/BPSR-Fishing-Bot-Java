package fishbot.core.state;

import fishbot.core.FishingBot;
import fishbot.core.game.Detector;
import fishbot.core.game.GameController;

import java.awt.image.BufferedImage;

public abstract class BaseState {
    protected final FishingBot bot;
    protected final Detector detector;
    protected final GameController controller;

    public BaseState(FishingBot bot) {
        this.bot = bot;
        this.detector = bot.getDetector();
        this.controller = bot.getController();
    }

    public abstract StateType handle(BufferedImage screen);

    public void reset() {}

    protected void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }
}
