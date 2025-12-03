package fishbot.ui;

import fishbot.config.Config;
import fishbot.config.ConfigLoader;
import fishbot.core.state.StateType;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Settings panel for general config values and state timeouts.
 * All changes update Config in memory; Save persists to config.json.
 */
public class ConfigPanel extends JFrame {

    private final JSpinner spinPrecision;
    private final JSpinner spinCountdown;
    private final JSpinner spinFps;
    private final JCheckBox chkQuickSkip;

    private final Map<StateType, JSpinner> timeoutSpinners = new LinkedHashMap<>();

    private boolean updatingFromCode = false;

    public ConfigPanel() {
        super("Settings");

        setSize(330, 420);
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        setResizable(false);

        // ============================================
        // General Section
        // ============================================
        JPanel generalSection = new JPanel(new GridLayout(4, 2, 6, 4));
        generalSection.setBorder(BorderFactory.createTitledBorder("General"));

        spinPrecision = new JSpinner(new SpinnerNumberModel(Config.DETECTION_PRECISION, 0.01, 1.0, 0.05));
        spinCountdown = new JSpinner(new SpinnerNumberModel(Config.START_COUNTDOWN, 0, 30, 1));
        spinFps = new JSpinner(new SpinnerNumberModel(Config.TARGET_FPS, 1, 240, 1));
        chkQuickSkip = new JCheckBox("", Config.QUICK_SKIP_MODE);

        spinPrecision.addChangeListener(e -> {
            if (!updatingFromCode) Config.DETECTION_PRECISION = (double) spinPrecision.getValue();
        });
        spinCountdown.addChangeListener(e -> {
            if (!updatingFromCode) Config.START_COUNTDOWN = (int) spinCountdown.getValue();
        });
        spinFps.addChangeListener(e -> {
            if (!updatingFromCode) Config.TARGET_FPS = (int) spinFps.getValue();
        });
        chkQuickSkip.addActionListener(e -> {
            if (!updatingFromCode) Config.QUICK_SKIP_MODE = chkQuickSkip.isSelected();
        });

        generalSection.add(new JLabel(" Detection Precision:"));
        generalSection.add(spinPrecision);
        generalSection.add(new JLabel(" Start Countdown (s):"));
        generalSection.add(spinCountdown);
        generalSection.add(new JLabel(" Target FPS:"));
        generalSection.add(spinFps);
        generalSection.add(new JLabel(" Quick Skip Mode:"));
        generalSection.add(chkQuickSkip);

        // ============================================
        // Timeouts Section
        // ============================================
        JPanel timeoutsSection = new JPanel(new GridLayout(StateType.values().length, 2, 6, 4));
        timeoutsSection.setBorder(BorderFactory.createTitledBorder("Timeouts (seconds)"));

        for (StateType state : StateType.values()) {
            int currentTimeout = Config.TIMEOUTS.getOrDefault(state, 10);
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(currentTimeout, 1, 300, 1));

            final StateType stateRef = state;
            spinner.addChangeListener(e -> {
                if (!updatingFromCode) Config.TIMEOUTS.put(stateRef, (int) spinner.getValue());
            });

            timeoutSpinners.put(state, spinner);
            timeoutsSection.add(new JLabel(" " + state.name() + ":"));
            timeoutsSection.add(spinner);
        }

        // ============================================
        // Buttons
        // ============================================
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));

        JButton btnSave = new JButton("Save to config.json");
        JButton btnReset = new JButton("Reset");

        btnSave.addActionListener(e -> {
            Config.saveToFile();
            JOptionPane.showMessageDialog(this,
                    "Config saved!\nPath: " + ConfigLoader.getConfigPath().toAbsolutePath(),
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
        });

        btnReset.addActionListener(e -> {
            fishbot.config.ConfigData data = ConfigLoader.load();
            loadFromData(data);
        });

        buttonsPanel.add(btnSave);
        buttonsPanel.add(btnReset);

        // ============================================
        // Assemble
        // ============================================
        add(generalSection);
        add(timeoutsSection);
        add(buttonsPanel);
    }

    private void loadFromData(fishbot.config.ConfigData data) {
        updatingFromCode = true;

        Config.DETECTION_PRECISION = data.detectionPrecision;
        Config.START_COUNTDOWN = data.startCountdown;
        Config.TARGET_FPS = data.targetFps;
        Config.QUICK_SKIP_MODE = data.quickSkipMode;

        spinPrecision.setValue(data.detectionPrecision);
        spinCountdown.setValue(data.startCountdown);
        spinFps.setValue(data.targetFps);
        chkQuickSkip.setSelected(data.quickSkipMode);

        for (Map.Entry<String, Integer> entry : data.timeouts.entrySet()) {
            StateType state = StateType.valueOf(entry.getKey());
            Config.TIMEOUTS.put(state, entry.getValue());
            JSpinner spinner = timeoutSpinners.get(state);
            if (spinner != null) spinner.setValue(entry.getValue());
        }

        updatingFromCode = false;
    }
}
