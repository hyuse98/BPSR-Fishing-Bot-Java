package fishbot.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * POJO representing the external config.json structure.
 * Gson maps JSON fields directly to these public fields.
 */
public class ConfigData {

    public double detectionPrecision = 0.65;
    public int startCountdown = 5;
    public int targetFps = 60;
    public boolean quickSkipMode = false;

    public MonitorConfig monitor = new MonitorConfig();

    public Map<String, String> templates = new LinkedHashMap<>();
    public Map<String, RoiConfig> rois = new LinkedHashMap<>();
    public Map<String, Integer> timeouts = new LinkedHashMap<>();

    public static class MonitorConfig {
        public int x = 0;
        public int y = 0;
        public int width = 1920;
        public int height = 1080;
    }

    public static class RoiConfig {
        public int x;
        public int y;
        public int width;
        public int height;

        public RoiConfig() {}

        public RoiConfig(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}
