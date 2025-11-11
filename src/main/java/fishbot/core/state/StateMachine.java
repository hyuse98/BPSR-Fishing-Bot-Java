package fishbot.core.state;

import fishbot.config.Config;
import fishbot.core.event.BotEventBus;
import fishbot.core.game.GameController;
import fishbot.utils.Logger;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class StateMachine {

    private final GameController controller;
    private final Logger logger;
    private final Map<StateType, BaseState> states = new HashMap<>();

    private StateType currentStateName;
    private BaseState currentState;
    private long stateStartTime;

    public StateMachine(GameController controller, BotEventBus eventBus) {
        this.controller = controller;
        this.logger = new Logger(eventBus);

    }

    public void addState(StateType type, BaseState state) {
        states.put(type, state);
    }

    public void setState(StateType newStateName, boolean force) {
        if (!force && newStateName == currentStateName) return;

        if (currentState != null) {
            currentState.reset();
        }

        if (currentStateName == null) {
            logger.info("Current State: " + newStateName);
        } else if (newStateName != currentStateName) {
            logger.info("Changing State: " + currentStateName + " -> " + newStateName);
        }

        currentStateName = newStateName;
        currentState = states.get(newStateName);
        stateStartTime = System.currentTimeMillis();
    }

    public void resetCurrentState() {
        if (currentState != null) currentState.reset();
    }

    public void handle(BufferedImage screen) {
        if (checkTimeout()) return;

        StateType nextState = currentState.handle(screen);
        setState(nextState, false);
    }

    private boolean checkTimeout() {
        Integer timeoutLimit = Config.TIMEOUTS.get(currentStateName);

        if (timeoutLimit == null) return false;

        long elapsed = (System.currentTimeMillis() - stateStartTime) / 1000;

        if (elapsed > timeoutLimit) {

            logger.warn("Current State was Exceed time limit: " + currentStateName + " " + timeoutLimit + "s!");
            logger.warn("Restarting Machine...");

            controller.releaseAllControls();
            controller.pressKey(KeyEvent.VK_ESCAPE);
            sleep();

            setState(StateType.STARTING, true);
            return true;
        }
        return false;
    }

    private void sleep() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }
    }
}
