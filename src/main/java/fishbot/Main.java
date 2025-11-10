package fishbot;

import fishbot.core.FishingBot;
import fishbot.core.event.BotEventBus;
import fishbot.core.event.impl.BotEventBusImpl;
import fishbot.ui.BotDashboard;

import javax.swing.*;

public class Main {
    static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
            //<https://www.formdev.com/flatlaf/>
        }
        BotEventBus eventBus = new BotEventBusImpl();

        FishingBot bot = new FishingBot(eventBus);

        BotDashboard dashboard = new BotDashboard();

        dashboard.setBot(bot);

        eventBus.register(dashboard);

        dashboard.setVisible(true);
    }
}