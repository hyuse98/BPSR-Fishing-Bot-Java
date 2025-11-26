package fishbot.ui;

import javax.swing.*;
import java.awt.*;

public class LogWindow extends JFrame {

    private final JTextArea logArea;

    public LogWindow() {
        setTitle("Logs");
        setSize(450, 350);
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLayout(new BorderLayout());

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        add(new JScrollPane(logArea), BorderLayout.CENTER);
    }

    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}