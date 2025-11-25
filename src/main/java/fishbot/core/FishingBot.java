package fishbot.core;

import fishbot.core.event.BotEventBus;
import fishbot.core.game.Detector;
import fishbot.core.game.GameController;
import fishbot.core.state.StateMachine;
import fishbot.core.state.StateType;
import fishbot.core.state.impl.*;
import fishbot.utils.Logger;

import java.awt.image.BufferedImage;

import static fishbot.config.Config.START_COUNTDOWN;

public class FishingBot implements Runnable {

    private final BotEventBus eventBus;
    private final Detector detector;
    private final GameController controller;
    private final StateMachine stateMachine;
    private final Logger logger;

    private volatile boolean isRunning;
    private volatile boolean paused;

    public FishingBot(BotEventBus eventBus) {
        this.eventBus = eventBus;
        this.logger = new Logger(eventBus);
        this.isRunning = false;
        this.paused = true;

        this.controller = new GameController(eventBus);
        this.detector = new Detector(eventBus);

        this.stateMachine = new StateMachine(this.controller, eventBus);

        registerStates();
    }

    private void registerStates() {
        stateMachine.addState(StateType.STARTING, new StartingState(this, eventBus));
        stateMachine.addState(StateType.CHECKING_ROD, new CheckingRodState(this, eventBus));
        stateMachine.addState(StateType.CASTING_BAIT, new CastingBaitState(this, eventBus));
        stateMachine.addState(StateType.WAITING_FOR_BITE, new WaitingForBiteState(this, eventBus));
        stateMachine.addState(StateType.PLAYING_MINIGAME, new PlayingMinigameState(this, eventBus));
        stateMachine.addState(StateType.FINISHING, new FinishingState(this, eventBus));
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                if (!paused) {
                    BufferedImage screen = detector.captureScreen();
                    stateMachine.handle(screen);
                    //TODO(Frame Rate)
                    Thread.sleep(10);
                } else {
                    Thread.sleep(200);
                }
            } catch (Exception e) {
                logger.error("Internal Critical Error, Running: " + e.getMessage());
            }
        }
    }

    public void start() {

        if (isRunning) return;
        this.isRunning = true;

        new Thread(this).start();

        new Thread(() -> {
            for (int x = START_COUNTDOWN; x > 0; x--) {
                logger.info("STARTING IN " + x);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            logger.info("STARTED");
            if (eventBus != null) {
                eventBus.publishStateChange("RUNNING");
            }
            stateMachine.setState(StateType.STARTING, true);

            paused = false;
        }).start();
    }

    public void stop() {
        if (!isRunning) return;
        this.isRunning = false;
        this.paused = true;

        logger.info("STOPPED");
        if (eventBus != null) {
            eventBus.publishStateChange("STOPPED");
        }
    }

    public Detector getDetector() {
        return detector;
    }

    public GameController getController() {
        return controller;
    }
}