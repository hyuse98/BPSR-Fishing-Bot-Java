package fishbot.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads config.json from the folder where the JAR resides.
 * If the file doesn't exist, generates a default one automatically.
 */
public class ConfigLoader {

    private static final String CONFIG_FILE_NAME = "config.json";

    public static ConfigData load() {
        Path configPath = getConfigPath();

        if (Files.exists(configPath)) {
            try {
                String json = Files.readString(configPath);
                Gson gson = new Gson();
                ConfigData data = gson.fromJson(json, ConfigData.class);
                System.out.println("[INFO] Config loaded from: " + configPath.toAbsolutePath());
                return data;
            } catch (Exception e) {
                System.err.println("[ERROR] Failed to read config: " + e.getMessage());
                System.err.println("[INFO] Falling back to defaults...");
            }
        } else {
            ConfigData defaults = createDefaults();
            saveConfig(configPath, defaults);
            System.out.println("[INFO] Default config generated at: " + configPath.toAbsolutePath());
            return defaults;
        }

        return createDefaults();
    }

    public static void save(ConfigData data) {
        saveConfig(getConfigPath(), data);
    }

    public static Path getConfigPath() {
        try {
            Path jarPath = Path.of(ConfigLoader.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            Path jarDir = Files.isDirectory(jarPath) ? jarPath : jarPath.getParent();
            return jarDir.resolve(CONFIG_FILE_NAME);
        } catch (Exception e) {
            // Fallback to current working directory
            return Path.of(CONFIG_FILE_NAME);
        }
    }

    public static void saveConfig(Path path, ConfigData data) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(data);
            Files.writeString(path, json);
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to save config: " + e.getMessage());
        }
    }

    private static ConfigData createDefaults() {
        ConfigData data = new ConfigData();

        // Templates
        data.templates.put("fishing_spot_btn", "fishing_spot_btn.png");
        data.templates.put("broken_rod", "broken_rod.png");
        data.templates.put("new_rod", "new_rod.png");
        data.templates.put("reg_rod", "reg_pole.png");
        data.templates.put("sturdy_rod", "sturdy_pole.png");
        data.templates.put("flex_rod", "flex_pole.png");
        data.templates.put("exclamation", "exclamation.png");
        data.templates.put("left_arrow", "left_arrow.png");
        data.templates.put("right_arrow", "right_arrow.png");
        data.templates.put("failure", "fish_escaped.png");
        data.templates.put("success", "success.png");
        data.templates.put("continue", "continue.png");
        data.templates.put("level_check", "level_check.png");
        data.templates.put("connect_server", "connect.png");

        // ROIs - HD 1366x768
//        data.rois.put("fishing_spot_btn", new ConfigData.RoiConfig(996, 384, 86, 39));
//        data.rois.put("broken_rod", new ConfigData.RoiConfig(1163, 698, 178, 45));
//        data.rois.put("reg_rod", new ConfigData.RoiConfig(1165, 700, 149, 23));
//        data.rois.put("sturdy_rod", new ConfigData.RoiConfig(1165, 700, 138, 26));
//        data.rois.put("flex_rod", new ConfigData.RoiConfig(1165, 700, 145, 26));
//        data.rois.put("new_rod", new ConfigData.RoiConfig(1155, 400, 132, 46));
//        data.rois.put("exclamation", new ConfigData.RoiConfig(661, 311, 37, 101));
//        data.rois.put("left_arrow", new ConfigData.RoiConfig(526, 348, 157, 71));
//        data.rois.put("right_arrow", new ConfigData.RoiConfig(683, 348, 157, 71));
//        data.rois.put("failure", new ConfigData.RoiConfig(692, 448, 499, 72));
//        data.rois.put("success", new ConfigData.RoiConfig(505, 441, 406, 92));
//        data.rois.put("continue", new ConfigData.RoiConfig(1024, 670, 218, 53));
//        data.rois.put("level_check", new ConfigData.RoiConfig(783, 700, 34, 21));
//        data.rois.put("connect_server", new ConfigData.RoiConfig(752, 543, 198, 48));

        // (ROIs) - FullHD 1080p
        data.rois.put("fishing_spot_btn", new ConfigData.RoiConfig(1400, 540, 121, 55));
        data.rois.put("broken_rod", new ConfigData.RoiConfig(1635, 982, 250, 63));
        data.rois.put("reg_rod", new ConfigData.RoiConfig(1638, 985, 210, 33));
        data.rois.put("sturdy_rod", new ConfigData.RoiConfig(1637, 984, 194, 37));
        data.rois.put("flex_rod", new ConfigData.RoiConfig(1637, 984, 204, 36));
        data.rois.put("new_rod", new ConfigData.RoiConfig(1624, 563, 185, 65));
        data.rois.put("exclamation", new ConfigData.RoiConfig(929, 438, 52, 142));
        data.rois.put("left_arrow", new ConfigData.RoiConfig(740, 490, 220, 100));
        data.rois.put("right_arrow", new ConfigData.RoiConfig(960, 490, 220, 100));
        data.rois.put("failure", new ConfigData.RoiConfig(973, 630, 702, 101));
        data.rois.put("success", new ConfigData.RoiConfig(710, 620, 570, 130));
        data.rois.put("continue", new ConfigData.RoiConfig(1439, 942, 306, 75));
        data.rois.put("level_check", new ConfigData.RoiConfig(1101, 985, 48, 29));
        data.rois.put("connect_server", new ConfigData.RoiConfig(1057, 763, 279, 67));

        // Timeouts (seconds) - keyed by StateType name
        data.timeouts.put("STARTING", 10);
        data.timeouts.put("CHECKING_ROD", 15);
        data.timeouts.put("CASTING_BAIT", 15);
        data.timeouts.put("WAITING_FOR_BITE", 25);
        data.timeouts.put("PLAYING_MINIGAME", 30);
        data.timeouts.put("FINISHING", 10);

        return data;
    }
}
