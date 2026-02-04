package net.thanachot.yurushi.discord.event;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.thanachot.yurushi.discord.ModalManager;
import net.thanachot.yurushi.discord.handler.ButtonActionHandler;
import org.jspecify.annotations.NonNull;

public class ModalListener extends ListenerAdapter {

    @Override
    public void onModalInteraction(@NonNull ModalInteractionEvent event) {
        switch (event.getModalId()) {
            case "register_modal" -> {
                ModalManager.handleRegisterModal(event);
            }
            case "denial_modal" -> {
                showDenialModal(event);
            }
        }
    }

    public static void showDenialModal(ModalInteractionEvent event) {
        String modalId = event.getModalId();
        String[] parts = modalId.split(":");
        if (parts.length < 3) {
            event.reply("Invalid modal data.").setEphemeral(true).queue();
            return;
        }

        String userId = parts[1];
        String minecraftUsername = parts[2];
        String denialReason = event.getValue("denial_reason") != null
                ? event.getValue("denial_reason").getAsString()
                : "No reason provided";
        User admin = event.getUser();


        event.reply("Whitelist request for `" + minecraftUsername + "` has been denied.\n" +
                        "**Reason:** " + denialReason)
                .setEphemeral(true)
                .queue();

    }
}
