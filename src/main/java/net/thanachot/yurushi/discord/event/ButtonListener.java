package net.thanachot.yurushi.discord.event;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.thanachot.yurushi.discord.handler.ButtonActionHandler;
import org.jspecify.annotations.NonNull;

public class ButtonListener extends ListenerAdapter {
    @Override
    public void onButtonInteraction(@NonNull ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();

        if (buttonId.startsWith("whitelist_deny:")) {
            ButtonActionHandler.handleDenial(event);
        }
    }
}
