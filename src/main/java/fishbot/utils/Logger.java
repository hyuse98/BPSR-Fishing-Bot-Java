package fishbot.utils;

import fishbot.core.event.BotEventBus;

public class Logger {

    private final BotEventBus eventBus;

    public Logger(BotEventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void info(String message) {
        String parse = "[INFO] " + message;
        System.out.println(parse);

        if (this.eventBus != null) {
            this.eventBus.publishLog(parse);
        }
    }

    public void error(String message) {
        String parse = "[ERROR] " + message;
        System.out.println(parse);

        if (this.eventBus != null) {
            this.eventBus.publishLog(parse);
        }
    }

    public void warn(String message) {
        String parse = "[WARN] " + message;
        System.out.println(parse);

        if (this.eventBus != null) {
            this.eventBus.publishLog(parse);
        }
    }
}
