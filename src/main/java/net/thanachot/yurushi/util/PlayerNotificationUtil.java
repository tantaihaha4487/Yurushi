package net.thanachot.yurushi.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
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
            ServerPlayerEntity player = handler.getPlayer();
            UUID playerUuid = player.getUuid();
            String playerName = player.getName().getString();

            CompletableFuture.runAsync(() -> {
                sendJoinNotification(playerName, playerUuid);
            }).exceptionally(throwable -> {
                Yurushi.LOGGER.error("Error sending player join notification for {}", playerName, throwable);
                return null;
            });
        });
    }

    public static void sendFirstJoinGreeting(String playerName, UUID playerUuid) {
        if (GREETED_PLAYERS.contains(playerUuid))
            return;

        GREETED_PLAYERS.add(playerUuid);
        sendGreetingNotification(playerName, playerUuid);
    }

    public static void sendBanNotification(String playerName, UUID playerUuid, String reason, String bannedBy) {
        if (!ModConfig.banNotifierEnabled || ModConfig.banNotifierChannelId.isBlank())
            return;

        CompletableFuture.runAsync(() -> {
            try {
                TextChannel channel = Yurushi.getJda().getTextChannelById(ModConfig.banNotifierChannelId);
                if (channel == null) {
                    Yurushi.LOGGER.warn("Ban notification channel not found: {}", ModConfig.banNotifierChannelId);
                    return;
                }

                String title = MessageConfig.get("notifications.ban.title");
                String description = MessageConfig.get("notifications.ban.description",
                        "player_name", playerName);
                String footerText = MessageConfig.get("notifications.ban.footer",
                        "timestamp", Instant.now().toString());

                String reasonField = MessageConfig.get("notifications.ban.fields.reason");
                String bannedByField = MessageConfig.get("notifications.ban.fields.banned_by");

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle(title)
                        .setDescription(description)
                        .setColor(Color.RED)
                        .setThumbnail(MinotarUtil.getAvatarUrl(playerName))
                        .addField(reasonField, reason.isBlank() ? "*No reason provided*" : reason, false)
                        .addField(bannedByField, bannedBy.isBlank() ? "Server" : bannedBy, false)
                        .setFooter(footerText)
                        .setTimestamp(Instant.now());

                channel.sendMessageEmbeds(embed.build()).queue();
                Yurushi.LOGGER.info("Ban notification sent for player: {}", playerName);

            } catch (Exception e) {
                Yurushi.LOGGER.error("Failed to send ban notification for {}", playerName, e);
            }
        });
    }

    private static void sendGreetingNotification(String playerName, UUID playerUuid) {
        if (!ModConfig.greetingEnabled || ModConfig.greetingChannelId.isBlank())
            return;

        try {
            TextChannel channel = Yurushi.getJda().getTextChannelById(ModConfig.greetingChannelId);
            if (channel == null) {
                Yurushi.LOGGER.warn("Greeting channel not found: {}", ModConfig.greetingChannelId);
                return;
            }

            String title = MessageConfig.get("notifications.greeting.title");
            String description = MessageConfig.get("notifications.greeting.description",
                    "player_name", playerName);
            String footerText = MessageConfig.get("notifications.greeting.footer",
                    "timestamp", Instant.now().toString());

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(title)
                    .setDescription(description)
                    .setColor(new Color(88, 101, 242))
                    .setThumbnail(MinotarUtil.getAvatarUrl(playerName))
                    .setFooter(footerText)
                    .setTimestamp(Instant.now());

            channel.sendMessageEmbeds(embed.build()).queue();
            Yurushi.LOGGER.info("Greeting sent for new player: {}", playerName);

        } catch (Exception e) {
            Yurushi.LOGGER.error("Failed to send greeting for {}", playerName, e);
        }
    }

    private static void sendJoinNotification(String playerName, UUID playerUuid) {
        if (!ModConfig.joinNotifierEnabled || ModConfig.joinNotifierChannelId.isBlank())
            return;

        try {
            TextChannel channel = Yurushi.getJda().getTextChannelById(ModConfig.joinNotifierChannelId);
            if (channel == null) {
                Yurushi.LOGGER.warn("Join notification channel not found: {}", ModConfig.joinNotifierChannelId);
                return;
            }

            String title = MessageConfig.get("notifications.join.title");
            String description = MessageConfig.get("notifications.join.description",
                    "player_name", playerName);
            String footerText = MessageConfig.get("notifications.join.footer",
                    "timestamp", Instant.now().toString());

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(title)
                    .setDescription(description)
                    .setColor(Color.GREEN)
                    .setThumbnail(MinotarUtil.getAvatarUrl(playerName))
                    .setFooter(footerText)
                    .setTimestamp(Instant.now());

            channel.sendMessageEmbeds(embed.build()).queue();
            Yurushi.LOGGER.info("Join notification sent for player: {}", playerName);

        } catch (Exception e) {
            Yurushi.LOGGER.error("Failed to send join notification for {}", playerName, e);
        }
    }
}
