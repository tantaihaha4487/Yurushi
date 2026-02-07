package net.thanachot.yurushi.discord.button;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.thanachot.yurushi.Yurushi;
import net.thanachot.yurushi.config.MessageConfig;
import net.thanachot.yurushi.config.ModConfig;
import net.thanachot.yurushi.manager.WhitelistManager;
import net.thanachot.yurushi.util.MinotarUtil;
import net.thanachot.yurushi.util.ServerAccessor;

import java.awt.*;
import java.time.Instant;

public class ApproveButton extends ActionButton {

    public static final String PREFIX = "whitelist_approve";

    public ApproveButton(String userId, String minecraftUsername) {
        super(userId, minecraftUsername);
    }

    @Override
    public void handle(ButtonInteractionEvent event) {
        if (!ModConfig.hasWhitelistPermission(event.getMember())) {
            event.reply(MessageConfig.get("error.no_permission")).setEphemeral(true).queue();
            return;
        }

        event.deferReply(true).queue();

        var whitelistManager = ServerAccessor.getWhitelistManager();
        if (whitelistManager.isEmpty()) {
            event.getHook().editOriginal(MessageConfig.get("error.server_unavailable")).queue();
            return;
        }

        processWhitelist(event, whitelistManager.get());
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
            event.getHook().editOriginal(MessageConfig.get("error.unexpected")).queue();
            return null;
        });
    }

    private void handleSuccess(ButtonInteractionEvent event, WhitelistManager.WhitelistResult result) {
        Yurushi.LOGGER.info("Whitelist approved for {} by {} (UUID: {})",
                minecraftUsername, event.getUser().getName(), result.uuid());

        Message message = event.getMessage();
        updateOriginalMessage(message, event.getUser().getAsMention());

        sendApprovalDM(event);

        event.getHook().editOriginal(MessageConfig.get("button.approve.success",
                "minecraft_username", minecraftUsername,
                "uuid", result.uuid().toString())).queue();
    }

    private void handleAlreadyWhitelisted(ButtonInteractionEvent event) {
        Message message = event.getMessage();
        if (message.getEmbeds().isEmpty()) {
            event.getHook().editOriginal(
                            MessageConfig.get("button.approve.already_whitelisted", "minecraft_username", minecraftUsername))
                    .queue();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder(message.getEmbeds().getFirst())
                .setTitle(MessageConfig.get("embed.already_whitelisted.title"))
                .setColor(Color.YELLOW)
                .setThumbnail(MinotarUtil.getAvatarUrl(minecraftUsername))
                .setFooter(MessageConfig.get("embed.already_whitelisted.footer"))
                .setTimestamp(Instant.now());

        message.editMessageEmbeds(embed.build()).setComponents().queue();
        event.getHook().editOriginal(
                        MessageConfig.get("button.approve.already_whitelisted", "minecraft_username", minecraftUsername))
                .queue();
    }

    private void handlePlayerNotFound(ButtonInteractionEvent event) {
        event.getHook()
                .editOriginal(
                        MessageConfig.get("button.approve.player_not_found", "minecraft_username", minecraftUsername))
                .queue();
    }

    private void handleError(ButtonInteractionEvent event, WhitelistManager.WhitelistResult result) {
        event.getHook().editOriginal(MessageConfig.get("button.approve.failed",
                "minecraft_username", minecraftUsername,
                "error", result.errorMessage())).queue();
    }

    @Override
    public Button create() {
        return Button.success(buildButtonId(), MessageConfig.get("button.approve.label"));
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    private void updateOriginalMessage(Message message, String adminName) {

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(MessageConfig.get("embed.approved.title"))
                .setColor(new Color(87, 242, 135))
                .setThumbnail(MinotarUtil.getAvatarUrl(minecraftUsername))
                .addField(MessageConfig.get("embed.request.fields.minecraft_username"), "`" + minecraftUsername + "`",
                        true)
                .addField(MessageConfig.get("embed.approved.fields.approved_by"), adminName, false)
                .setFooter(MessageConfig.get("embed.approved.footer"))
                .setTimestamp(Instant.now());

        message.editMessageEmbeds(embed.build()).setComponents().queue();
    }

    private void sendApprovalDM(ButtonInteractionEvent event) {
        event.getJDA().retrieveUserById(userId).queue(user -> {
            if (user == null)
                return;

            user.openPrivateChannel().queue(channel -> {
                EmbedBuilder dmEmbed = new EmbedBuilder()
                        .setTitle(MessageConfig.get("dm.approved.title"))
                        .setColor(new Color(87, 242, 135))
                        .setAuthor(event.getJDA().getSelfUser().getName(), null,
                                event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                        .setThumbnail(event.getUser().getEffectiveAvatarUrl())
                        .setDescription(MessageConfig.get("dm.approved.description"))
                        .addField(MessageConfig.get("embed.request.fields.minecraft_username"),
                                "`" + minecraftUsername + "`", false)
                        .setFooter(MessageConfig.get("embed.approved.footer"))
                        .setTimestamp(Instant.now());
                channel.sendMessageEmbeds(dmEmbed.build()).queue(
                        success -> {
                        },
                        error -> Yurushi.LOGGER.error("Could not send DM to user: {}", userId));
            }, error -> Yurushi.LOGGER.error("Could not open private channel for user: {}", userId));
        }, error -> Yurushi.LOGGER.error("Could not retrieve user: {}", userId));
    }
}
