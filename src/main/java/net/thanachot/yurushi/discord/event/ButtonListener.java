package net.thanachot.yurushi.discord.event;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.thanachot.yurushi.discord.button.ActionButton;
import net.thanachot.yurushi.discord.button.ApproveButton;
import net.thanachot.yurushi.discord.button.DenyButton;
import org.jspecify.annotations.NonNull;

public class ButtonListener extends ListenerAdapter {
    @Override
    public void onButtonInteraction(@NonNull ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();
        String[] parts = ActionButton.parseButtonId(buttonId);

        if (!ActionButton.isValidButtonData(parts)) {
            event.reply("Invalid button data.").setEphemeral(true).queue();
            return;
        }

        String prefix = parts[0];
        String userId = parts[1];
        String minecraftUsername = parts[2];

        ActionButton button = switch (prefix) {
            case ApproveButton.PREFIX -> new ApproveButton(userId, minecraftUsername);
            case DenyButton.PREFIX -> new DenyButton(userId, minecraftUsername);
            default -> null;
        };

        if (button != null) {
            button.handle(event);
        }
    }
}
