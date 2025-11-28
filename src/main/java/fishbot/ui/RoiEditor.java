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

    private void drawHandles(Graphics2D g2d, Rectangle roi) {
        g2d.setStroke(new BasicStroke(1));
        int hs = HANDLE_SIZE;

        Rectangle[] handles = getHandleRects(roi);
        for (Rectangle h : handles) {
            g2d.setColor(Color.WHITE);
            g2d.fillRect(h.x, h.y, h.width, h.height);
            g2d.setColor(SELECTED_COLOR);
            g2d.drawRect(h.x, h.y, h.width, h.height);
        }
    }

    private Rectangle[] getHandleRects(Rectangle roi) {
        int hs = HANDLE_SIZE;
        int mx = roi.x + roi.width / 2 - hs / 2;
        int my = roi.y + roi.height / 2 - hs / 2;

        return new Rectangle[]{
                // Corners: NW, NE, SW, SE
                new Rectangle(roi.x - hs / 2, roi.y - hs / 2, hs, hs),
                new Rectangle(roi.x + roi.width - hs / 2, roi.y - hs / 2, hs, hs),
                new Rectangle(roi.x - hs / 2, roi.y + roi.height - hs / 2, hs, hs),
                new Rectangle(roi.x + roi.width - hs / 2, roi.y + roi.height - hs / 2, hs, hs),
                // Edges: N, S, W, E
                new Rectangle(mx, roi.y - hs / 2, hs, hs),
                new Rectangle(mx, roi.y + roi.height - hs / 2, hs, hs),
                new Rectangle(roi.x - hs / 2, my, hs, hs),
                new Rectangle(roi.x + roi.width - hs / 2, my, hs, hs),
        };
    }

    private DragMode[] HANDLE_MODES = {
            DragMode.RESIZE_NW, DragMode.RESIZE_NE, DragMode.RESIZE_SW, DragMode.RESIZE_SE,
            DragMode.RESIZE_N, DragMode.RESIZE_S, DragMode.RESIZE_W, DragMode.RESIZE_E
    };

    private class RoiMouseHandler extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            Point p = e.getPoint();

            // Right-click to deselect
            if (SwingUtilities.isRightMouseButton(e)) {
                selectedRoiKey = null;
                dragMode = DragMode.NONE;
                repaint();
                for (RoiChangeListener l : listeners) l.onRoiDeselected();
                return;
            }

            // Check if clicking on a handle of the selected ROI
            if (selectedRoiKey != null) {
                Rectangle selectedRoi = Config.ROIS.get(selectedRoiKey);
                if (selectedRoi != null) {
                    Rectangle[] handles = getHandleRects(selectedRoi);
                    for (int i = 0; i < handles.length; i++) {
                        if (handles[i].contains(p)) {
                            dragMode = HANDLE_MODES[i];
                            dragStart = p;
                            originalRect = new Rectangle(selectedRoi);
                            return;
                        }
                    }

                    // Check if clicking inside the selected ROI (move)
                    if (selectedRoi.contains(p)) {
                        dragMode = DragMode.MOVE;
                        dragStart = p;
                        originalRect = new Rectangle(selectedRoi);
                        return;
                    }
                }
            }

            // Check if clicking on any ROI to select it
            for (String key : Config.ROIS.keySet()) {
                Rectangle roi = Config.ROIS.get(key);
                if (roi != null && roi.contains(p)) {
                    selectedRoiKey = key;
                    dragMode = DragMode.MOVE;
                    dragStart = p;
                    originalRect = new Rectangle(roi);
                    repaint();
                    for (RoiChangeListener l : listeners) l.onRoiSelected(key, roi);
                    return;
                }
            }

            // Clicked on nothing — deselect
            selectedRoiKey = null;
            dragMode = DragMode.NONE;
            repaint();
            for (RoiChangeListener l : listeners) l.onRoiDeselected();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (dragMode == DragMode.NONE || selectedRoiKey == null || dragStart == null || originalRect == null) {
                return;
            }

            int dx = e.getX() - dragStart.x;
            int dy = e.getY() - dragStart.y;

            Rectangle newRect = computeNewRect(dx, dy);
            if (newRect == null) return;

            // Enforce minimum size
            if (newRect.width < 5) newRect.width = 5;
            if (newRect.height < 5) newRect.height = 5;

            Config.updateRoi(selectedRoiKey, newRect);
            repaint();
            for (RoiChangeListener l : listeners) l.onRoiChanged(selectedRoiKey, newRect);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            dragMode = DragMode.NONE;
            dragStart = null;
            originalRect = null;
        }

        private Rectangle computeNewRect(int dx, int dy) {
            int ox = originalRect.x, oy = originalRect.y;
            int ow = originalRect.width, oh = originalRect.height;

            return switch (dragMode) {
                case MOVE -> new Rectangle(ox + dx, oy + dy, ow, oh);
                case RESIZE_NW -> new Rectangle(ox + dx, oy + dy, ow - dx, oh - dy);
                case RESIZE_NE -> new Rectangle(ox, oy + dy, ow + dx, oh - dy);
                case RESIZE_SW -> new Rectangle(ox + dx, oy, ow - dx, oh + dy);
                case RESIZE_SE -> new Rectangle(ox, oy, ow + dx, oh + dy);
                case RESIZE_N -> new Rectangle(ox, oy + dy, ow, oh - dy);
                case RESIZE_S -> new Rectangle(ox, oy, ow, oh + dy);
                case RESIZE_W -> new Rectangle(ox + dx, oy, ow - dx, oh);
                case RESIZE_E -> new Rectangle(ox, oy, ow + dx, oh);
                default -> null;
            };
        }
    }
}
