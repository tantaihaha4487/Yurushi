package net.thanachot.yurushi.discord.button;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.thanachot.yurushi.Yurushi;
import net.thanachot.yurushi.config.ModConfig;
import net.thanachot.yurushi.discord.modal.DenialModal;
import net.thanachot.yurushi.util.MinotarUtil;

import java.awt.*;
import java.time.Instant;

public class DenyButton extends ActionButton {

    public static final String PREFIX = "whitelist_deny";

    public DenyButton(String userId, String minecraftUsername) {
        super(userId, minecraftUsername);
    }

    public static void sendDenialDM(JDA jda, String userId, String minecraftUsername, String reason) {
        jda.retrieveUserById(userId).queue(user -> {
            if (user != null) {
                user.openPrivateChannel().queue(channel -> {
                    EmbedBuilder dmEmbed = new EmbedBuilder()
                            .setTitle("Whitelist Request Denied")
                            .setColor(new Color(237, 66, 69))
                            .setDescription("Unfortunately, your whitelist request has been denied.")
                            .addField("Minecraft Username", "`" + minecraftUsername + "`", false)
                            .addField("Reason", reason, false)
                            .setFooter("Please contact an administrator if you have questions.")
                            .setTimestamp(Instant.now());
                    channel.sendMessageEmbeds(dmEmbed.build()).queue(
                            success -> {
                            },
                            error -> Yurushi.LOGGER.error("Could not send DM to user: {}", userId));
                }, error -> Yurushi.LOGGER.error("Could not open private channel for user: {}", userId));
            }
        }, error -> Yurushi.LOGGER.error("Could not retrieve user: {}", userId));
    }

    public static void updateOriginalMessage(Message message, String minecraftUsername, String reason, String adminName) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Whitelist Request - Denied")
                .setColor(new Color(237, 66, 69))
                .setThumbnail(MinotarUtil.getAvatarUrl(minecraftUsername))
                .addField("Minecraft Username", "`" + minecraftUsername + "`", false)
                .addField("Reason", reason, false)
                .addField("Denied By", adminName, false)
                .setFooter("Please contact an administrator if you have questions.")
                .setTimestamp(Instant.now());

        message.editMessageEmbeds(embed.build()).setComponents().queue();
    }

    @Override
    public void handle(ButtonInteractionEvent event) {
        if (!hasRequiredRole(event, ModConfig.whitelistRole)) {
            event.reply("❌ You don't have permission to deny requests.").setEphemeral(true).queue();
            return;
        }

        DenialModal denialModal = new DenialModal(userId, minecraftUsername);
        event.replyModal(denialModal.create()).queue();
    }

    @Override
    public Button create() {
        return Button.danger(buildButtonId(), "Deny");
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }
}
