package net.thanachot.yurushi.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.thanachot.yurushi.Yurushi;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class MessageConfig {

    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("Yurushi");
    private static final Path CONFIG_PATH = CONFIG_DIR.resolve("messages.toml");
    private static CommentedFileConfig config;

    public static void load() {
        if (!Files.exists(CONFIG_DIR)) {
            try {
                Files.createDirectories(CONFIG_DIR);
            } catch (IOException e) {
                Yurushi.LOGGER.error("Failed to create config directory", e);
            }
        }

        if (!Files.exists(CONFIG_PATH)) {
            copyDefaultConfig();
        }

        config = CommentedFileConfig.builder(CONFIG_PATH)
                .autosave()
                .build();
        config.load();
    }

    private static void copyDefaultConfig() {
        try (InputStream is = MessageConfig.class.getResourceAsStream("/messages.toml")) {
            if (is != null)
                Files.copy(is, CONFIG_PATH);
        } catch (IOException e) {
            Yurushi.LOGGER.error("Failed to copy default messages.toml", e);
        }
    }

    public static String get(String key) {
        if (config == null) return key;
        return config.getOrElse(key, key);
    }

    public static String get(String key, String... replacements) {
        String msg = get(key);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                msg = msg.replace("{" + replacements[i] + "}", String.valueOf(replacements[i + 1]));
            }
        }
        return msg;
    }
}
