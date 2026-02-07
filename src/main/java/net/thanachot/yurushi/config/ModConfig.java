package net.thanachot.yurushi.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.fabricmc.loader.api.FabricLoader;
import net.thanachot.yurushi.Yurushi;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ModConfig {

    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("Yurushi");
    private static final Path CONFIG_PATH = CONFIG_DIR.resolve("Yurushi.toml");

    public static String botToken = "";
    public static String adminChannelId = "";
    public static List<String> whitelistRole = new ArrayList<>();

    public static void load() {
        if (!Files.exists(CONFIG_DIR)) {
            try {
                Files.createDirectories(CONFIG_DIR);
            } catch (IOException e) {
                Yurushi.LOGGER.error("Failed to create config directory", e);
            }
        }

        MessageConfig.load();
        copyDefaultConfig();

        try (CommentedFileConfig config = CommentedFileConfig.builder(CONFIG_PATH)
                .autosave()
                .build()) {
            config.load();

            botToken = config.getOrElse("botToken", "");
            adminChannelId = config.getOrElse("adminChannelId", "");
            whitelistRole = config.getOrElse("whitelistRole", new ArrayList<>());

            config.set("botToken", botToken);
            config.set("adminChannelId", adminChannelId);
            config.set("whitelistRole", whitelistRole);
        }
    }

    private static void copyDefaultConfig() {
        if (Files.exists(CONFIG_PATH)) {
            return;
        }

        try (InputStream is = ModConfig.class.getResourceAsStream("/Yurushi.toml")) {
            if (is != null) {
                Files.copy(is, CONFIG_PATH);
            }
        } catch (IOException ignored) {
        }
    }

    public static List<String> validate() {
        List<String> errors = new ArrayList<>();

        if (botToken == null || botToken.isBlank()) {
            errors.add("botToken is required");
        }

        if (adminChannelId == null || adminChannelId.isBlank()) {
            errors.add("adminChannelId is required");
        }

        return errors;
    }

    public static List<String> validateWithJda(JDA jda) {
        List<String> errors = new ArrayList<>();

        if (jda.getTextChannelById(adminChannelId) == null) {
            errors.add("adminChannelId '" + adminChannelId + "' not found in Discord");
        }

        for (String roleId : whitelistRole) {
            if (!roleExistsInAnyGuild(jda, roleId)) {
                errors.add("whitelistRole '" + roleId + "' not found in Discord");
            }
        }

        return errors;
    }

    private static boolean roleExistsInAnyGuild(JDA jda, String roleId) {
        for (Guild guild : jda.getGuilds()) {
            roleId = roleId.trim();
            if (guild.getRoleById(roleId) != null) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasErrors() {
        return !validate().isEmpty();
    }

    public static boolean hasWhitelistPermission(Member member) {
        if (member == null)
            return false;

        if (member.hasPermission(Permission.ADMINISTRATOR))
            return true;

        if (whitelistRole == null || whitelistRole.isEmpty())
            return true;

        for (Role role : member.getRoles()) {
            if (whitelistRole.contains(role.getId())) {
                return true;
            }
        }
        return false;
    }
}
