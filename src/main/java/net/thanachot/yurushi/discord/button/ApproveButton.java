package net.thanachot.yurushi.discord.button;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.thanachot.yurushi.Yurushi;
import net.thanachot.yurushi.config.ModConfig;
import net.thanachot.yurushi.manager.ServerAccessor;
import net.thanachot.yurushi.manager.WhitelistManager;

import java.awt.*;
import java.time.Instant;

public class ApproveButton extends ActionButton {

    public static final String PREFIX = "whitelist_approve";

    public ApproveButton(String userId, String minecraftUsername) {
        super(userId, minecraftUsername);
    }

    @Override
    public void handle(ButtonInteractionEvent event) {
        if (!hasRequiredRole(event, ModConfig.whitelistRole)) {
            event.reply("❌ You don't have permission to approve requests.").setEphemeral(true).queue();
            return;
        }

        event.deferReply(true).queue();

        ServerAccessor.getWhitelistManager().ifPresentOrElse(
                whitelistManager -> processWhitelist(event, whitelistManager),
                () -> event.getHook().editOriginal("❌ Server is not available. Please try again later.").queue());
    }

    private void processWhitelist(ButtonInteractionEvent event, WhitelistManager whitelistManager) {
        whitelistManager.addToWhitelist(minecraftUsername).thenAccept(result -> {
            switch (result.status()) {
                case SUCCESS -> handleSuccess(event, result);
                case ALREADY_WHITELISTED -> handleAlreadyWhitelisted(event);
                case PLAYER_NOT_FOUND -> handlePlayerNotFound(event);
                case ERROR -> handleError(event, result);
            }
        }).exceptionally(throwable -> {
            Yurushi.LOGGER.error("Failed to process whitelist for {}", minecraftUsername, throwable);
            event.getHook().editOriginal("❌ An unexpected error occurred while processing the whitelist.").queue();
            return null;
        });
    }

    private void handleSuccess(ButtonInteractionEvent event, WhitelistManager.WhitelistResult result) {
        Yurushi.LOGGER.info("Whitelist approved for {} by {} (UUID: {})",
                minecraftUsername, event.getUser().getName(), result.uuid());

        Message message = event.getMessage();
        updateOriginalMessage(message, event.getUser().getAsMention(), result);

        sendApprovalDM(event);

        event.getHook().editOriginal("✅ Whitelist request for `" + minecraftUsername +
                "` has been approved!\n" +
                "**UUID:** `" + result.uuid() + "`\n").queue();
    }

    private void handleAlreadyWhitelisted(ButtonInteractionEvent event) {
        event.getHook().editOriginal("⚠️ `" + minecraftUsername + "` is already whitelisted on the server.").queue();
    }

    private void handlePlayerNotFound(ButtonInteractionEvent event) {
        event.getHook().editOriginal("❌ Player `" + minecraftUsername +
                "` was not found on Mojang's servers.\n" +
                "This username might be incorrect or doesn't exist.").queue();
    }

    private void handleError(ButtonInteractionEvent event, WhitelistManager.WhitelistResult result) {
        event.getHook().editOriginal("❌ Failed to whitelist `" + minecraftUsername +
                "`.\nError: " + result.errorMessage()).queue();
    }

    @Override
    public Button create() {
        return Button.success(buildButtonId(), "Approve");
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    private void updateOriginalMessage(Message message, String adminName, WhitelistManager.WhitelistResult result) {

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Whitelist Request - Approved")
                .setColor(new Color(87, 242, 135))
                .addField("Minecraft Username", "`" + minecraftUsername + "`", true)
                .addField("UUID", "`" + result.uuid() + "`", false)
                .addField("Approved By", adminName, false)
                .setTimestamp(Instant.now());

        message.editMessageEmbeds(embed.build()).setComponents().queue();
    }

    private void sendApprovalDM(ButtonInteractionEvent event) {
        event.getJDA().retrieveUserById(userId).queue(user -> {
            if (user != null) {
                user.openPrivateChannel().queue(channel -> {
                    EmbedBuilder dmEmbed = new EmbedBuilder()
                            .setTitle("Whitelist Request Approved")
                            .setColor(new Color(87, 242, 135))
                            .setDescription("Congratulations! Your whitelist request has been approved.")
                            .addField("Minecraft Username", "`" + minecraftUsername + "`", false)
                            .addField("Status", "You can now join the server!", false)
                            .setTimestamp(Instant.now());
                    channel.sendMessageEmbeds(dmEmbed.build()).queue(
                            success -> {
                            },
                            error -> Yurushi.LOGGER.error("Could not send DM to user: {}", userId));
                }, error -> Yurushi.LOGGER.error("Could not open private channel for user: {}", userId));
            }
        }, error -> Yurushi.LOGGER.error("Could not retrieve user: {}", userId));
    }
}
