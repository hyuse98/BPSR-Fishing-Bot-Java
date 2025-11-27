package fishbot.ui;

import fishbot.config.Config;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Interactive transparent overlay for visual ROI editing.
 * Click to select ROIs, drag to move, drag corners to resize.
 */
public class RoiEditor extends JWindow {

    private static final int HANDLE_SIZE = 8;
    private static final Color SELECTED_COLOR = new Color(0, 200, 255);
    private static final Color SELECTED_FILL = new Color(0, 200, 255, 40);

    private String selectedRoiKey = null;
    private DragMode dragMode = DragMode.NONE;
    private Point dragStart = null;
    private Rectangle originalRect = null;

    private final List<RoiChangeListener> listeners = new ArrayList<>();

    public interface RoiChangeListener {
        void onRoiSelected(String key, Rectangle roi);
        void onRoiChanged(String key, Rectangle roi);
        void onRoiDeselected();
    }

    private enum DragMode {
        NONE, MOVE, RESIZE_NW, RESIZE_NE, RESIZE_SW, RESIZE_SE,
        RESIZE_N, RESIZE_S, RESIZE_W, RESIZE_E
    }

    public RoiEditor() {
        setBackground(new Color(0, 0, 0, 0));
        setAlwaysOnTop(true);
        setFocusableWindowState(true);
        setBounds(Config.MONITOR_X, Config.MONITOR_Y, Config.MONITOR_WIDTH, Config.MONITOR_HEIGHT);

        RoiMouseHandler handler = new RoiMouseHandler();
        addMouseListener(handler);
        addMouseMotionListener(handler);
    }

    public void updateBounds() {
        setBounds(Config.MONITOR_X, Config.MONITOR_Y, Config.MONITOR_WIDTH, Config.MONITOR_HEIGHT);
        repaint();
    }

    public void addRoiChangeListener(RoiChangeListener listener) {
        listeners.add(listener);
    }

    public void selectRoi(String key) {
        this.selectedRoiKey = key;
        repaint();
    }

    public String getSelectedRoiKey() {
        return selectedRoiKey;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw monitor resolution boundary
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10.0f, new float[]{10.0f, 5.0f}, 0.0f));
        g2d.drawRect(0, 0, Config.MONITOR_WIDTH - 1, Config.MONITOR_HEIGHT - 1);

        String resLabel = Config.MONITOR_WIDTH + "x" + Config.MONITOR_HEIGHT;
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(new Color(0, 0, 0, 180));
        int labelWidth = g2d.getFontMetrics().stringWidth(resLabel) + 10;
        g2d.fillRect(4, 4, labelWidth, 22);
        g2d.setColor(Color.WHITE);
        g2d.drawString(resLabel, 9, 20);

        // Draw ROIs
        Color[] colors = {Color.RED, Color.GREEN, Color.CYAN, Color.YELLOW, Color.MAGENTA, Color.ORANGE};
        int colorIndex = 0;

        for (String key : Config.ROIS.keySet()) {
            Rectangle roi = Config.ROIS.get(key);
            if (roi == null) continue;

            boolean isSelected = key.equals(selectedRoiKey);
            Color drawColor = isSelected ? SELECTED_COLOR : colors[colorIndex % colors.length];

            // Fill for selected
            if (isSelected) {
                g2d.setColor(SELECTED_FILL);
                g2d.fillRect(roi.x, roi.y, roi.width, roi.height);
            }

            // Border
            g2d.setColor(drawColor);
            g2d.setStroke(new BasicStroke(isSelected ? 3 : 2));
            g2d.drawRect(roi.x, roi.y, roi.width, roi.height);

            // Label
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            String label = isSelected ? key + " [SELECTED]" : key;
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(roi.x, roi.y - 18, g2d.getFontMetrics().stringWidth(label) + 6, 18);
            g2d.setColor(isSelected ? SELECTED_COLOR : Color.WHITE);
            g2d.drawString(label, roi.x + 3, roi.y - 4);

            // Draw resize handles for selected ROI
            if (isSelected) {
                drawHandles(g2d, roi);
            }

            colorIndex++;
        }
    }


}
