package net.thanachot.yurushi.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.thanachot.yurushi.Yurushi;
import net.thanachot.yurushi.config.MessageConfig;
import net.thanachot.yurushi.config.ModConfig;

import java.awt.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerNotificationUtil {

    private static final Set<UUID> GREETED_PLAYERS = new HashSet<>();

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            UUID playerUuid = player.getUUID();
            String playerName = player.getName().getString();
            boolean isFirstJoin = isFirstJoin(player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)));

            if (isFirstJoin) {
                sendFirstJoinGreeting(playerName, playerUuid);
            }

            CompletableFuture.runAsync(() -> {
                sendJoinNotification(playerName, playerUuid);
            }).exceptionally(throwable -> {
                Yurushi.LOGGER.error("Error sending player join notification for {}", playerName, throwable);
                return null;
            });
        });
    }

    public static boolean isFirstJoin(int playTime) {
        return playTime == 0;
    }

    static boolean shouldNotify(boolean enabled, String channelId) {
        return enabled && channelId != null && !channelId.isBlank();
    }

    public static void sendFirstJoinGreeting(String playerName, UUID playerUuid) {
        if (GREETED_PLAYERS.contains(playerUuid))
            return;

        GREETED_PLAYERS.add(playerUuid);
        sendGreetingNotification(playerName, playerUuid);
    }

    public static EmbedBuilder createBanEmbed(String playerName, String reason, String bannedBy) {
        String title = MessageConfig.get("notifications.ban.title");
        String description = MessageConfig.get("notifications.ban.description",
                "player_name", playerName);
        String footerText = MessageConfig.get("notifications.ban.footer",
                "timestamp", Instant.now().toString());

        String reasonField = MessageConfig.get("notifications.ban.fields.reason");
        String bannedByField = MessageConfig.get("notifications.ban.fields.banned_by");

        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(Color.RED)
                .setThumbnail(MinotarUtil.getAvatarUrl(playerName))
                .addField(reasonField, reason.isBlank() ? "*No reason provided*" : reason, false)
                .addField(bannedByField, bannedBy.isBlank() ? "Server" : bannedBy, false)
                .setFooter(footerText)
                .setTimestamp(Instant.now());
    }

    public static void sendBanNotification(String playerName, UUID playerUuid, String reason, String bannedBy) {
        if (!shouldNotify(ModConfig.banNotifierEnabled, ModConfig.banNotifierChannelId))
            return;

        CompletableFuture.runAsync(() -> {
            try {
                TextChannel channel = Yurushi.getJda().getTextChannelById(ModConfig.banNotifierChannelId);
                if (channel == null) {
                    Yurushi.LOGGER.warn("Ban notification channel not found: {}", ModConfig.banNotifierChannelId);
                    return;
                }

                EmbedBuilder embed = createBanEmbed(playerName, reason, bannedBy);
                channel.sendMessageEmbeds(embed.build()).queue();
                Yurushi.LOGGER.info("Ban notification sent for player: {}", playerName);

            } catch (Exception e) {
                Yurushi.LOGGER.error("Failed to send ban notification for {}", playerName, e);
            }
        });
    }

    public static EmbedBuilder createGreetingEmbed(String playerName) {
        String title = MessageConfig.get("notifications.greeting.title");
        String description = MessageConfig.get("notifications.greeting.description",
                "player_name", playerName);

        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(new Color(88, 101, 242))
                .setThumbnail(MinotarUtil.getAvatarUrl(playerName))
                .setTimestamp(Instant.now());
    }

    private static void sendGreetingNotification(String playerName, UUID playerUuid) {
        if (!shouldNotify(ModConfig.greetingEnabled, ModConfig.greetingChannelId))
            return;

        try {
            TextChannel channel = Yurushi.getJda().getTextChannelById(ModConfig.greetingChannelId);
            if (channel == null) {
                Yurushi.LOGGER.warn("Greeting channel not found: {}", ModConfig.greetingChannelId);
                return;
            }

            EmbedBuilder embed = createGreetingEmbed(playerName);
            channel.sendMessageEmbeds(embed.build()).queue();
            Yurushi.LOGGER.info("Greeting sent for new player: {}", playerName);

        } catch (Exception e) {
            Yurushi.LOGGER.error("Failed to send greeting for {}", playerName, e);
        }
    }

    public static EmbedBuilder createJoinEmbed(String playerName) {
        String title = MessageConfig.get("notifications.join.title");
        String description = MessageConfig.get("notifications.join.description",
                "player_name", playerName);

        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(Color.GREEN)
                .setThumbnail(MinotarUtil.getAvatarUrl(playerName))
                .setTimestamp(Instant.now());
    }

    private static void sendJoinNotification(String playerName, UUID playerUuid) {
        if (!shouldNotify(ModConfig.joinNotifierEnabled, ModConfig.joinNotifierChannelId))
            return;

        try {
            TextChannel channel = Yurushi.getJda().getTextChannelById(ModConfig.joinNotifierChannelId);
            if (channel == null) {
                Yurushi.LOGGER.warn("Join notification channel not found: {}", ModConfig.joinNotifierChannelId);
                return;
            }

            EmbedBuilder embed = createJoinEmbed(playerName);
            channel.sendMessageEmbeds(embed.build()).queue();
            Yurushi.LOGGER.info("Join notification sent for player: {}", playerName);

        } catch (Exception e) {
            Yurushi.LOGGER.error("Failed to send join notification for {}", playerName, e);
        }
    }
}
