package fishbot.ui;

import fishbot.config.Config;
import fishbot.config.ConfigLoader;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Form panel with monitor config + ROI spinners for fine-tuning values.
 * Bidirectionally synced with the RoiEditor overlay.
 */
public class RoiEditorPanel extends JFrame implements RoiEditor.RoiChangeListener {

    private final JComboBox<String> roiSelector;
    private final JSpinner spinX, spinY, spinW, spinH;
    private final JSpinner spinMonX, spinMonY, spinMonW, spinMonH;
    private final RoiEditor roiEditor;

    private boolean updatingFromCode = false;

    public RoiEditorPanel(RoiEditor roiEditor) {
        super("ROI Editor");
        this.roiEditor = roiEditor;

        setSize(350, 380);
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        setResizable(false);

        // Register as listener for overlay drag events
        roiEditor.addRoiChangeListener(this);

        // ============================================
        // Monitor Config Section
        // ============================================
        JPanel monitorSection = new JPanel(new BorderLayout(6, 4));
        monitorSection.setBorder(BorderFactory.createTitledBorder("Monitor"));

        JPanel monitorSpinners = new JPanel(new GridLayout(2, 4, 4, 4));
        monitorSpinners.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        spinMonX = createMonitorSpinner();
        spinMonY = createMonitorSpinner();
        spinMonW = createMonitorSpinner();
        spinMonH = createMonitorSpinner();

        // Load current monitor values
        updatingFromCode = true;
        spinMonX.setValue(Config.MONITOR_X);
        spinMonY.setValue(Config.MONITOR_Y);
        spinMonW.setValue(Config.MONITOR_WIDTH);
        spinMonH.setValue(Config.MONITOR_HEIGHT);
        updatingFromCode = false;

        monitorSpinners.add(new JLabel(" X:"));
        monitorSpinners.add(spinMonX);
        monitorSpinners.add(new JLabel(" Y:"));
        monitorSpinners.add(spinMonY);
        monitorSpinners.add(new JLabel(" Width:"));
        monitorSpinners.add(spinMonW);
        monitorSpinners.add(new JLabel(" Height:"));
        monitorSpinners.add(spinMonH);

        monitorSection.add(monitorSpinners, BorderLayout.CENTER);

        // ============================================
        // ROI Editor Section
        // ============================================
        JPanel roiSection = new JPanel(new BorderLayout(6, 4));
        roiSection.setBorder(BorderFactory.createTitledBorder("ROI"));

        // ROI Selector
        JPanel selectorPanel = new JPanel(new BorderLayout(6, 0));
        selectorPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));

        String[] roiKeys = Config.ROIS.keySet().toArray(new String[0]);
        roiSelector = new JComboBox<>(roiKeys);
        roiSelector.insertItemAt("-- Select ROI --", 0);
        roiSelector.setSelectedIndex(0);

        roiSelector.addActionListener(e -> {
            if (updatingFromCode) return;
            int idx = roiSelector.getSelectedIndex();
            if (idx <= 0) {
                roiEditor.selectRoi(null);
                setRoiSpinnersEnabled(false);
                clearRoiSpinners();
            } else {
                String key = (String) roiSelector.getSelectedItem();
                roiEditor.selectRoi(key);
                Rectangle roi = Config.ROIS.get(key);
                if (roi != null) {
                    loadRoiSpinners(roi);
                    setRoiSpinnersEnabled(true);
                }
            }
        });

        selectorPanel.add(new JLabel("ROI:"), BorderLayout.WEST);
        selectorPanel.add(roiSelector, BorderLayout.CENTER);

        // ROI Spinners
        JPanel roiSpinnersPanel = new JPanel(new GridLayout(2, 4, 4, 4));
        roiSpinnersPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        spinX = createRoiSpinner();
        spinY = createRoiSpinner();
        spinW = createRoiSpinner();
        spinH = createRoiSpinner();

        roiSpinnersPanel.add(new JLabel(" X:"));
        roiSpinnersPanel.add(spinX);
        roiSpinnersPanel.add(new JLabel(" Y:"));
        roiSpinnersPanel.add(spinY);
        roiSpinnersPanel.add(new JLabel(" Width:"));
        roiSpinnersPanel.add(spinW);
        roiSpinnersPanel.add(new JLabel(" Height:"));
        roiSpinnersPanel.add(spinH);

        setRoiSpinnersEnabled(false);

        roiSection.add(selectorPanel, BorderLayout.NORTH);
        roiSection.add(roiSpinnersPanel, BorderLayout.CENTER);

        // ============================================
        // Buttons
        // ============================================
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));

        JButton btnSave = new JButton("Save to config.json");
        JButton btnReset = new JButton("Reset");

        btnSave.addActionListener(e -> {
            Config.saveToFile();
            JOptionPane.showMessageDialog(this,
                    "Config saved to config.json!\nPath: " + ConfigLoader.getConfigPath().toAbsolutePath(),
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
        });

        btnReset.addActionListener(e -> {
            // Reload everything from file
            fishbot.config.ConfigData data = ConfigLoader.load();

            // Reset monitor
            Config.updateMonitor(data.monitor.x, data.monitor.y, data.monitor.width, data.monitor.height);
            updatingFromCode = true;
            spinMonX.setValue(Config.MONITOR_X);
            spinMonY.setValue(Config.MONITOR_Y);
            spinMonW.setValue(Config.MONITOR_WIDTH);
            spinMonH.setValue(Config.MONITOR_HEIGHT);
            updatingFromCode = false;

            // Reset ROIs
            Config.ROIS.clear();
            for (Map.Entry<String, fishbot.config.ConfigData.RoiConfig> entry : data.rois.entrySet()) {
                fishbot.config.ConfigData.RoiConfig roi = entry.getValue();
                Config.ROIS.put(entry.getKey(), new Rectangle(roi.x, roi.y, roi.width, roi.height));
            }

            // Refresh ROI spinners
            String selected = roiEditor.getSelectedRoiKey();
            if (selected != null) {
                Rectangle roi = Config.ROIS.get(selected);
                if (roi != null) loadRoiSpinners(roi);
            }

            roiEditor.updateBounds();
        });

        buttonsPanel.add(btnSave);
        buttonsPanel.add(btnReset);

        // ============================================
        // Assemble
        // ============================================
        add(monitorSection);
        add(roiSection);
        add(buttonsPanel);
    }


}
