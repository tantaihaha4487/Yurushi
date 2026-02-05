package net.thanachot.yurushi.discord.modal;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.modals.Modal;

public abstract class BaseModal {

    public abstract void handle(ModalInteractionEvent event);

    public abstract Modal create();

    public abstract String getPrefix();

    public static String[] parseModalId(String modalId) {
        return modalId.split(":");
    }

    public static boolean isValidModalData(String[] parts, int requiredParts) {
        return parts.length >= requiredParts;
    }
}
