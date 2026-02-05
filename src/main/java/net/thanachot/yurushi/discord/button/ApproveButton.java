package net.thanachot.yurushi.discord.button;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.thanachot.yurushi.Yurushi;

import java.awt.*;
import java.time.Instant;

public class ApproveButton extends ActionButton {

    public static final String PREFIX = "whitelist_approve";

    public ApproveButton(String userId, String minecraftUsername) {
        super(userId, minecraftUsername);
    }

    @Override
    public void handle(ButtonInteractionEvent event) {
        Yurushi.LOGGER.info("Whitelist approved for {} by {}", minecraftUsername, event.getUser().getName());

        Message message = event.getMessage();
        updateOriginalMessage(message, event.getUser().getAsMention());

        sendApprovalDM(event);

        event.reply("Whitelist request for `" + minecraftUsername + "` has been approved!")
                .setEphemeral(true)
                .queue();
    }

    @Override
    public Button create() {
        return Button.success(buildButtonId(), "Approve");
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    private void updateOriginalMessage(Message message, String adminName) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Whitelist Request - Approved")
                .setColor(new Color(87, 242, 135))
                .addField("Minecraft Username", "`" + minecraftUsername + "`", false)
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
