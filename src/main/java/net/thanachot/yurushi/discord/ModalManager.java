package net.thanachot.yurushi.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.modals.Modal;
import net.thanachot.yurushi.Yurushi;
import net.thanachot.yurushi.discord.handler.ButtonActionHandler;

import java.awt.*;
import java.time.Instant;
import java.util.Objects;

public class ModalManager {

    private static final String ADMIN_CHANNEL_ID = "1468343272995819560";

    private static final String REGISTER_MODAL_ID = "register_modal";
    private static final String MINECRAFT_USERNAME_INPUT_ID = "minecraft_username";
    private static final String DESCRIPTION_INPUT_ID = "description";

    public static Modal getRegisterModal() {
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

        // Create the modal with Label components
        return Modal.create(REGISTER_MODAL_ID, "Whitelist Registration")
                .addComponents(
                        Label.of("Minecraft Username", minecraftUsername),
                        Label.of("Description (Optional)", description))
                .build();
    }

    public static Modal getDenialModal(String userId, String minecraftUsername) {
        TextInput reasonInput = TextInput.create("denial_reason", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Provide a reason for denying the whitelist request...")
                .setRequired(false)
                .setMinLength(0)
                .setMaxLength(500)
                .build();

        // Encode user data in the modal ID for later retrieval

        return Modal.create("denial_modal:" + userId + ":" + minecraftUsername, "Deny Whitelist Request")
                .addComponents(Label.of("Denial Reason", reasonInput))
                .build();
    }

    public static void handleRegisterModal(ModalInteractionEvent event) {
        User requester = event.getUser();
        String minecraftUsername = Objects.requireNonNull(event.getValue(MINECRAFT_USERNAME_INPUT_ID)).getAsString();
        String description = event.getValue(DESCRIPTION_INPUT_ID) != null
                ? Objects.requireNonNull(event.getValue(DESCRIPTION_INPUT_ID)).getAsString()
                : "No description provided";

        String buttonData = requester.getId() + ":" + minecraftUsername;
        Button approveButton = Button.success("whitelist_approve:" + buttonData, "Approve");
        Button denyButton = Button.danger("whitelist_deny:" + buttonData, "Deny");

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Whitelist Request")
                .setColor(new Color(233, 136, 255))
                .setThumbnail(requester.getEffectiveAvatarUrl())
                .addField("Discord User", requester.getAsMention(), true)
                .addField("Minecraft Username", "`" + minecraftUsername + "`", true)
                .addField("Description", description.isEmpty() ? "*No description provided*" : "``" + description + "``", false)
                .setFooter("User ID: " + requester.getId())
                .setTimestamp(Instant.now());

        TextChannel adminChannel = event.getJDA().getTextChannelById(ADMIN_CHANNEL_ID);

        if (adminChannel != null) {
            adminChannel.sendMessageEmbeds(embed.build())
                    .setComponents(ActionRow.of(approveButton, denyButton))
                    .queue();

            // TODO: Add to config
            event.reply(
                            "Your whitelist request has been submitted!\n" +
                                    "**Minecraft Username:** ``" + minecraftUsername + "``\n" +
                                    "An admin will review your request soon."
                    )
                    .setEphemeral(true)
                    .queue();
        } else {
            event.reply("An error occurred. Please contact an administrator.")
                    .setEphemeral(true)
                    .queue();
            Yurushi.LOGGER.error("Admin channel not found for whitelist requests.");
        }
    }

    public static void handleDenialModal(ModalInteractionEvent event) {
        String modalId = event.getModalId();
        String[] parts = modalId.split(":");
        if (parts.length < 3) {
            event.reply("Invalid modal data.").setEphemeral(true).queue();
            return;
        }

        String userId = parts[1];
        String minecraftUsername = parts[2];
        String denialReason = event.getValue("denial_reason") != null
                ? Objects.requireNonNull(event.getValue("denial_reason")).getAsString()
                : "No reason provided";


        event.deferEdit().queue();
    }
}
