package net.thanachot.yurushi.discord.modal;

import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.modals.Modal;
import net.thanachot.yurushi.discord.button.DenyButton;

import java.util.Optional;

public class DenialModal extends BaseModal {

    public static final String PREFIX = "denial_modal";
    private static final String DENIAL_REASON_INPUT_ID = "denial_reason";

    private final String userId;
    private final String minecraftUsername;

    public DenialModal(String userId, String minecraftUsername) {
        this.userId = userId;
        this.minecraftUsername = minecraftUsername;
    }

    @Override
    public void handle(ModalInteractionEvent event) {
        String denialReason = Optional.ofNullable(event.getValue(DENIAL_REASON_INPUT_ID))
                .map(ModalMapping::getAsString)
                .filter(s -> !s.isBlank())
                .orElse("*No reason provided*");

        DenyButton.sendDenialDM(event.getJDA(), userId, minecraftUsername, denialReason);

        if (event.getMessage() != null) {
            DenyButton.updateOriginalMessage(event.getMessage(), minecraftUsername, denialReason,
                    event.getUser().getAsMention());
        }

        event.reply("Whitelist request for `" + minecraftUsername + "` has been denied.\n" +
                        "**Reason:** " + denialReason)
                .setEphemeral(true)
                .queue();
    }

    @Override
    public Modal create() {
        TextInput reasonInput = TextInput.create(DENIAL_REASON_INPUT_ID, TextInputStyle.PARAGRAPH)
                .setPlaceholder("Provide a reason for denying the whitelist request...")
                .setRequired(false)
                .setMinLength(0)
                .setMaxLength(500)
                .build();

        return Modal.create(buildModalId(), "Deny Whitelist Request")
                .addComponents(Label.of("Denial Reason", reasonInput))
                .build();
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    public String getUserId() {
        return userId;
    }

    public String getMinecraftUsername() {
        return minecraftUsername;
    }

    private String buildModalId() {
        return PREFIX + ":" + userId + ":" + minecraftUsername;
    }
}
