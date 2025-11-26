package fishbot.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class DetectionDebugger extends JFrame {
    private final DefaultTableModel tableModel;
    private final Map<String, Integer> rowIndexMap = new HashMap<>();

    public DetectionDebugger() {
        super("Detection Debugger");
        setSize(350, 400);
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        String[] columns = {"Template", "Highest Match", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(tableModel);
        table.setEnabled(false);

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void updateDetection(String template, double bestScore, boolean matched) {
        SwingUtilities.invokeLater(() -> {
            String scoreStr = String.format("%.2f%%", bestScore * 100);
            String status = matched ? "FOUND" : "SEARCHING...";

            if (rowIndexMap.containsKey(template)) {
                int row = rowIndexMap.get(template);
                tableModel.setValueAt(scoreStr, row, 1);
                tableModel.setValueAt(status, row, 2);
            } else {
                rowIndexMap.put(template, tableModel.getRowCount());
                tableModel.addRow(new Object[]{template, scoreStr, status});
            }
        });
    }
}