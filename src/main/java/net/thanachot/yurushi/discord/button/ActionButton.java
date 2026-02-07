package net.thanachot.yurushi.discord.button;

import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public abstract class ActionButton {

    protected final String userId;
    protected final String minecraftUsername;

    protected ActionButton(String userId, String minecraftUsername) {
        this.userId = userId;
        this.minecraftUsername = minecraftUsername;
    }

    public static String[] parseButtonId(String buttonId) {
        return buttonId.split(":");
    }

    public static boolean isValidButtonData(String[] parts) {
        return parts.length >= 3;
    }

    public abstract void handle(ButtonInteractionEvent event);

    public abstract Button create();

    public abstract String getPrefix();

    public String getUserId() {
        return userId;
    }

    public String getMinecraftUsername() {
        return minecraftUsername;
    }

    protected String buildButtonId() {
        return getPrefix() + ":" + userId + ":" + minecraftUsername;
    }

}
