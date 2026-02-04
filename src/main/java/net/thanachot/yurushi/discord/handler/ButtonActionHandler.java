package net.thanachot.yurushi.discord.handler;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.thanachot.yurushi.discord.ModalManager;

public class ButtonActionHandler {

    public static void handleDenial(ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();
        String[] parts = buttonId.split(":");
        if (parts.length < 3) {
            event.reply("Invalid button data.").setEphemeral(true).queue();
            return;
        }

        String userId = parts[1];
        String minecraftUsername = parts[2];

        event.replyModal(ModalManager.getDenialModal(userId, minecraftUsername)).queue();
    }
}
