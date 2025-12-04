package fishbot.ui;

import fishbot.core.event.BotEventListener;
import fishbot.core.FishingBot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class BotDashboard extends JFrame implements BotEventListener {

    private final JLabel stateLabel;
    private final JToggleButton btnToggleROI;
    private final JToggleButton btnToggleDebugger;
    private final JToggleButton btnToggleLogs;

    private FishingBot bot;
    private RoiEditor roiEditor;
    private RoiEditorPanel roiEditorPanel;
    private DetectionDebugger detectionDebugger;
    private ConfigPanel configPanel;
    private final LogWindow logWindow;

    public BotDashboard() {
        setTitle("BR:SR Fishing Bot");
        setSize(500, 70);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setAlwaysOnTop(true);

        logWindow = new LogWindow();
        logWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                btnToggleLogs.setSelected(false);
            }
        });

        stateLabel = new JLabel("Status: IDLE", SwingConstants.CENTER);

        JButton btnStart = new JButton("Start");
        JButton btnStop = new JButton("Stop");
        btnToggleROI = new JToggleButton("ROIs");
        btnToggleDebugger = new JToggleButton("Debugger");
        btnToggleLogs = new JToggleButton("Logs");

        btnStart.addActionListener(e -> {
            if (bot != null) bot.start();
        });

        btnStop.addActionListener(e -> {
            if (bot != null) bot.stop();
        });

        btnToggleROI.addActionListener(e -> {
            if (btnToggleROI.isSelected()) {
                if (roiEditor == null) {
                    roiEditor = new RoiEditor();
                    roiEditorPanel = new RoiEditorPanel(roiEditor);

                    roiEditorPanel.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent we) {
                            btnToggleROI.setSelected(false);
                            closeRoiEditor();
                        }
                    });
                }
                roiEditor.setVisible(true);
                roiEditorPanel.setVisible(true);
            } else {
                closeRoiEditor();
            }
        });

        btnToggleDebugger.addActionListener(e -> {
            if (btnToggleDebugger.isSelected()) {
                if (detectionDebugger == null) {
                    detectionDebugger = new DetectionDebugger();

                    detectionDebugger.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent we) {
                            btnToggleDebugger.setSelected(false);
                            detectionDebugger = null;
                        }
                    });
                }
                detectionDebugger.setVisible(true);
            } else {
                if (detectionDebugger != null) {
                    detectionDebugger.setVisible(false);
                    detectionDebugger.dispose();
                    detectionDebugger = null;
                }
            }
        });

        btnToggleLogs.addActionListener(e -> {
            logWindow.setVisible(btnToggleLogs.isSelected());
        });


}