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

        JToggleButton btnToggleConfig = new JToggleButton("Config");
        btnToggleConfig.addActionListener(e -> {
            if (btnToggleConfig.isSelected()) {
                if (configPanel == null) {
                    configPanel = new ConfigPanel();
                    configPanel.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent we) {
                            btnToggleConfig.setSelected(false);
                            configPanel = null;
                        }
                    });
                }
                configPanel.setVisible(true);
            } else {
                if (configPanel != null) {
                    configPanel.setVisible(false);
                    configPanel.dispose();
                    configPanel = null;
                }
            }
        });

        JPanel topPanel = new JPanel();
        topPanel.add(btnStart);
        topPanel.add(btnStop);
        topPanel.add(btnToggleROI);
        topPanel.add(btnToggleDebugger);
        topPanel.add(btnToggleLogs);
        topPanel.add(btnToggleConfig);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(stateLabel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.LINE_START);
        add(centerPanel, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeRoiEditor();
                if (detectionDebugger != null) detectionDebugger.dispose();
                if (configPanel != null) configPanel.dispose();
                logWindow.dispose();
            }
        });

        onLogMessage("Welcome BP:SR Fishing Bot!");
        onLogMessage(
                """
                        ⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⣟⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
                        ⣿⣿⣿⣿⣿⠿⢿⢛⢟⢛⡛⡳⠳⢾⣮⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿
                        ⣿⡿⡋⢩⢠⢃⡑⡡⣑⢅⠕⣌⢋⡒⣒⢎⠮⠝⠙⠏⠷⣳⣷⣿⣿
                        ⣿⣯⣲⡣⣢⡱⡹⡪⢖⠝⠪⠊⡉⣉⣠⣤⢶⡾⢾⣞⡻⣎⢿⣿⣿
                        ⣿⣿⣿⣿⣿⣾⣷⣷⣷⣻⡚⣽⣟⡯⠷⣝⣫⣾⣿⣿⣿⣿⣷⣿⣿
                        ⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿"""
        );
    }

    private void closeRoiEditor() {
        if (roiEditor != null) {
            roiEditor.setVisible(false);
            roiEditor.dispose();
            roiEditor = null;
        }
        if (roiEditorPanel != null) {
            roiEditorPanel.setVisible(false);
            roiEditorPanel.dispose();
            roiEditorPanel = null;
        }
    }

    public void setBot(FishingBot bot) {
        this.bot = bot;
    }

    @Override
    public void onLogMessage(String message) {
        if (logWindow != null) logWindow.appendLog(message);
    }

    @Override
    public void onStateChanged(String stateName) {
        SwingUtilities.invokeLater(() -> stateLabel.setText("Status: " + stateName));
    }

    @Override
    public void onFishCaught(int totalCaught) {
        if (logWindow != null) logWindow.appendLog(">>> Peixe fisgado! Total: " + totalCaught);
    }

    @Override
    public void onDetectionUpdate(String templateName, double score, boolean matched) {
        if (detectionDebugger != null && detectionDebugger.isVisible()) {
            detectionDebugger.updateDetection(templateName, score, matched);
        }
    }

    @Override
    public void onImageProcessed(org.bytedeco.opencv.opencv_core.Mat image) {
        if (detectionDebugger != null && detectionDebugger.isVisible()) {
            //detectionDebugger.updateImage(image);
            System.out.println();
        }
    }
}