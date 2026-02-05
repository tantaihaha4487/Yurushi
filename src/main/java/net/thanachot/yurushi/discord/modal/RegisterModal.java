package net.thanachot.yurushi.discord.modal;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.modals.Modal;
import net.thanachot.yurushi.Yurushi;
import net.thanachot.yurushi.discord.button.ApproveButton;
import net.thanachot.yurushi.discord.button.DenyButton;

import java.awt.*;
import java.time.Instant;
import java.util.Objects;

public class RegisterModal extends BaseModal {

    public static final String PREFIX = "register_modal";
    private static final String ADMIN_CHANNEL_ID = "1468343272995819560";
    private static final String MINECRAFT_USERNAME_INPUT_ID = "minecraft_username";
    private static final String DESCRIPTION_INPUT_ID = "description";

    @Override
    public void handle(ModalInteractionEvent event) {
        User requester = event.getUser();
        String minecraftUsername = Objects.requireNonNull(event.getValue(MINECRAFT_USERNAME_INPUT_ID))
                .getAsString();
        String description = event.getValue(DESCRIPTION_INPUT_ID) != null
                ? Objects.requireNonNull(event.getValue(DESCRIPTION_INPUT_ID)).getAsString()
                : "No description provided";

        ApproveButton approveButton = new ApproveButton(requester.getId(), minecraftUsername);
        DenyButton denyButton = new DenyButton(requester.getId(), minecraftUsername);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Whitelist Request")
                .setColor(new Color(233, 136, 255))
                .setThumbnail(requester.getEffectiveAvatarUrl())
                .addField("Discord User", requester.getAsMention(), true)
                .addField("Minecraft Username", "`" + minecraftUsername + "`", true)
                .addField("Description",
                        description.isEmpty() ? "*No description provided*"
                                : "``" + description + "``",
                        false)
                .setFooter("User ID: " + requester.getId())
                .setTimestamp(Instant.now());

        TextChannel adminChannel = event.getJDA().getTextChannelById(ADMIN_CHANNEL_ID);

        if (adminChannel != null) {
            adminChannel.sendMessageEmbeds(embed.build())
                    .setComponents(ActionRow.of(approveButton.create(), denyButton.create()))
                    .queue();

            event.reply(
                    "Your whitelist request has been submitted!\n" +
                            "**Minecraft Username:** ``" + minecraftUsername + "``\n" +
                            "An admin will review your request soon.")
                    .setEphemeral(true)
                    .queue();
        } else {
            event.reply("An error occurred. Please contact an administrator.")
                    .setEphemeral(true)
                    .queue();
            Yurushi.LOGGER.error("Admin channel not found for whitelist requests.");
        }
    }

    @Override
    public Modal create() {
        TextInput minecraftUsername = TextInput.create(MINECRAFT_USERNAME_INPUT_ID, TextInputStyle.SHORT)
                .setPlaceholder("Enter your Minecraft username")
                .setRequired(true)
                .setMinLength(3)
                .setMaxLength(16)
                .build();

        TextInput description = TextInput.create(DESCRIPTION_INPUT_ID, TextInputStyle.PARAGRAPH)
                .setPlaceholder("Tell us a bit about yourself or why you want to join...")
                .setRequired(false)
                .setMaxLength(500)
                .build();

        return Modal.create(PREFIX, "Whitelist Registration")
                .addComponents(
                        Label.of("Minecraft Username", minecraftUsername),
                        Label.of("Description (Optional)", description))
                .build();
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }
}
