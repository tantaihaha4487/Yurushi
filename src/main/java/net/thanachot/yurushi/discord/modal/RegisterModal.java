package net.thanachot.yurushi.discord.modal;

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
import net.thanachot.yurushi.config.MessageConfig;
import net.thanachot.yurushi.config.ModConfig;
import net.thanachot.yurushi.discord.button.ApproveButton;
import net.thanachot.yurushi.discord.button.DenyButton;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RegisterModal extends BaseModal {

    public static final String PREFIX = "register_modal";
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

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(MessageConfig.get("embed.request.title"))
                .setColor(new Color(233, 136, 255))
                .setThumbnail(requester.getEffectiveAvatarUrl())
                .addField(MessageConfig.get("embed.request.fields.discord_user"),
                        requester.getAsMention(), true)
                .addField(MessageConfig.get("embed.request.fields.minecraft_username"),
                        "`" + minecraftUsername + "`", true)
                .addField(MessageConfig.get("embed.request.fields.description"),
                        description.isEmpty()
                                ? MessageConfig.get(
                                "embed.request.fields.description_empty")
                                : "``" + description + "``",
                        false)
                .setFooter(MessageConfig.get("embed.request.footer", "user_id", requester.getId()))
                .setTimestamp(Instant.now());

        List<Button> buttons = new ArrayList<>();

        buttons.add(new ApproveButton(requester.getId(), minecraftUsername).create());
        buttons.add(new DenyButton(requester.getId(), minecraftUsername).create());

        TextChannel adminChannel = event.getJDA().getTextChannelById(ModConfig.adminChannelId);

        if (adminChannel != null) {
            var messageAction = adminChannel.sendMessageEmbeds(embed.build());
            messageAction = messageAction.setComponents(ActionRow.of(buttons));
            messageAction.queue();

            EmbedBuilder replyEmbed = new EmbedBuilder()
                    .setTitle(MessageConfig.get("modal.register.success"))
                    .setColor(new Color(233, 136, 255))
                    .addField(MessageConfig.get("embed.request.fields.discord_user"),
                            requester.getAsMention(), true)
                    .addField(MessageConfig.get("embed.request.fields.minecraft_username"),
                            "`" + minecraftUsername + "`", true)
                    .setTimestamp(Instant.now());

            event.replyEmbeds(replyEmbed.build())
                    .setEphemeral(true)
                    .queue();
        } else {
            event.reply(MessageConfig.get("error.admin_channel_not_found"))
                    .setEphemeral(true)
                    .queue();
            Yurushi.LOGGER.error("Admin channel not found for whitelist requests.");
        }
    }

    @Override
    public Modal create() {
        TextInput minecraftUsername = TextInput.create(MINECRAFT_USERNAME_INPUT_ID, TextInputStyle.SHORT)
                .setPlaceholder(MessageConfig.get("modal.register.inputs.username_placeholder"))
                .setRequired(true)
                .setMinLength(3)
                .setMaxLength(16)
                .build();

        TextInput description = TextInput.create(DESCRIPTION_INPUT_ID, TextInputStyle.PARAGRAPH)
                .setPlaceholder(MessageConfig.get("modal.register.inputs.description_placeholder"))
                .setRequired(false)
                .setMaxLength(500)
                .build();

        return Modal.create(PREFIX, MessageConfig.get("modal.register.title"))
                .addComponents(
                        Label.of(MessageConfig.get("modal.register.inputs.username_label"),
                                minecraftUsername),
                        Label.of(MessageConfig.get("modal.register.inputs.description_label"),
                                description))
                .build();
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }
}
