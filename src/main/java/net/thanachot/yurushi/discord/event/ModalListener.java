package net.thanachot.yurushi.discord.event;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.thanachot.yurushi.discord.modal.BaseModal;
import net.thanachot.yurushi.discord.modal.DenialModal;
import net.thanachot.yurushi.discord.modal.RegisterModal;
import org.jspecify.annotations.NonNull;

public class ModalListener extends ListenerAdapter {

    @Override
    public void onModalInteraction(@NonNull ModalInteractionEvent event) {
        String modalId = event.getModalId();
        String[] parts = BaseModal.parseModalId(modalId);
        String prefix = parts[0];

        BaseModal modal = switch (prefix) {
            case RegisterModal.PREFIX -> new RegisterModal();
            case DenialModal.PREFIX -> {
                if (!BaseModal.isValidModalData(parts, 3)) {
                    event.reply("Invalid modal data.").setEphemeral(true).queue();
                    yield null;
                }
                yield new DenialModal(parts[1], parts[2]);
            }
            default -> null;
        };

        if (modal != null) {
            modal.handle(event);
        }
    }
}
