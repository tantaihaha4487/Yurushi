package net.thanachot.yurushi.manager;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.MinecraftServer;
import net.minecraft.commands.CommandSourceStack;
import net.thanachot.yurushi.Yurushi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class WhitelistManager {

    private static final String MOJANG_API_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private final MinecraftServer server;

    public WhitelistManager(MinecraftServer server) {
        this.server = server;
    }

    public CompletableFuture<WhitelistResult> addToWhitelist(String username) {
        boolean isOnlineMode = server.usesAuthentication();

        if (isOnlineMode) {
            return addOnlineModeWhitelist(username);
        } else {
            return addOfflineModeWhitelist(username);
        }
    }

    private CompletableFuture<WhitelistResult> addOnlineModeWhitelist(String username) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Optional<MojangProfile> profile = fetchMojangProfile(username);

                if (profile.isEmpty())
                    return WhitelistResult.playerNotFound(username);


                MojangProfile mojangProfile = profile.get();
                return scheduleWhitelistAdd(mojangProfile.username(), mojangProfile.uuid()).join();

            } catch (Exception e) {
                Yurushi.LOGGER.error("Failed to add {} to whitelist (online mode)", username, e);
                return WhitelistResult.error(username, e.getMessage());
            }
        });
    }

    private CompletableFuture<WhitelistResult> addOfflineModeWhitelist(String username) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                UUID offlineUuid = generateOfflineUuid(username);
                return scheduleWhitelistAdd(username, offlineUuid).join();
            } catch (Exception e) {
                Yurushi.LOGGER.error("Failed to add {} to whitelist (offline mode)", username, e);
                return WhitelistResult.error(username, e.getMessage());
            }
        });
    }

    private CompletableFuture<WhitelistResult> scheduleWhitelistAdd(String username, UUID uuid) {
        CompletableFuture<WhitelistResult> result = new CompletableFuture<>();
        server.execute(() -> {
            try {
                if (isPlayerWhitelisted(username)) {
                    result.complete(WhitelistResult.alreadyWhitelisted(username));
                    return;
                }

                CommandSourceStack source = server.createCommandSourceStack();
                var dispatcher = server.getCommands().getDispatcher();
                dispatcher.execute("whitelist add " + username, source);
                Yurushi.LOGGER.info("Added {} ({}) to whitelist (mode: {})",
                        username, uuid, server.usesAuthentication() ? "online" : "offline");
                result.complete(WhitelistResult.success(username, uuid));
            } catch (Exception e) {
                Yurushi.LOGGER.error("Failed to execute whitelist add for {}", username, e);
                result.complete(WhitelistResult.error(username, e.getMessage()));
            }
        });
        return result;
    }

    private Optional<MojangProfile> fetchMojangProfile(String username) throws Exception {
        URL url = URI.create(MOJANG_API_URL + username).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        int responseCode = connection.getResponseCode();

        if (responseCode == 204 || responseCode == 404) {
            return Optional.empty();
        }

        if (responseCode != 200) {
            throw new RuntimeException("Mojang API returned status code: " + responseCode);
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
            String id = json.get("id").getAsString();
            String name = json.get("name").getAsString();

            UUID uuid = parseUuidFromMojang(id);
            return Optional.of(new MojangProfile(name, uuid));
        }
    }

    private UUID parseUuidFromMojang(String id) {
        String formatted = id.replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                "$1-$2-$3-$4-$5");
        return UUID.fromString(formatted);
    }

    static UUID generateOfflineUuid(String username) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));

            hash[6] &= 0x0f;
            hash[6] |= 0x30;
            hash[8] &= 0x3f;
            hash[8] |= (byte) 0x80;

            long msb = 0;
            long lsb = 0;
            for (int i = 0; i < 8; i++) {
                msb = (msb << 8) | (hash[i] & 0xff);
            }
            for (int i = 8; i < 16; i++) {
                lsb = (lsb << 8) | (hash[i] & 0xff);
            }

            return new UUID(msb, lsb);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate offline UUID", e);
        }
    }

    public CompletableFuture<Boolean> removeFromWhitelist(String username) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        server.execute(() -> {
            try {
                if (!isPlayerWhitelisted(username)) {
                    result.complete(false);
                    return;
                }

                CommandSourceStack source = server.createCommandSourceStack();
                var dispatcher = server.getCommands().getDispatcher();
                dispatcher.execute("whitelist remove " + username, source);
                Yurushi.LOGGER.info("Removed {} from whitelist", username);
                result.complete(true);
            } catch (Exception e) {
                Yurushi.LOGGER.error("Failed to remove {} from whitelist", username, e);
                result.complete(false);
            }
        });
        return result;
    }

    public boolean isPlayerWhitelisted(String username) {
        var whitelist = server.getPlayerList().getWhiteList();
        var names = whitelist.getUserList();

        for (String name : names) {
            if (name.equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    public boolean isOnlineMode() {
        return server.usesAuthentication();
    }

    public record MojangProfile(String username, UUID uuid) {
    }

    public record WhitelistResult(
            Status status,
            String username,
            UUID uuid,
            String errorMessage) {
        public static WhitelistResult success(String username, UUID uuid) {
            return new WhitelistResult(Status.SUCCESS, username, uuid, null);
        }

        public static WhitelistResult alreadyWhitelisted(String username) {
            return new WhitelistResult(Status.ALREADY_WHITELISTED, username, null, null);
        }

        public static WhitelistResult playerNotFound(String username) {
            return new WhitelistResult(Status.PLAYER_NOT_FOUND, username, null, null);
        }

        public static WhitelistResult error(String username, String errorMessage) {
            return new WhitelistResult(Status.ERROR, username, null, errorMessage);
        }

        public boolean isSuccess() {
            return status == Status.SUCCESS;
        }

        public String getMessage() {
            return switch (status) {
                case SUCCESS -> "Successfully whitelisted " + username;
                case ALREADY_WHITELISTED -> username + " is already whitelisted";
                case PLAYER_NOT_FOUND -> "Player " + username + " was not found on Mojang's servers";
                case ERROR -> "Error whitelisting " + username + ": " + errorMessage;
            };
        }

        public enum Status {
            SUCCESS,
            ALREADY_WHITELISTED,
            PLAYER_NOT_FOUND,
            ERROR
        }
    }
}
