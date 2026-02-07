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

    /**
     * Parses the button ID into parts.
     * Expected format: prefix:userId:minecraftUsername
     */
    public static String[] parseButtonId(String buttonId) {
        return buttonId.split(":");
    }

    /**
     * visual explanation:
     * parts[0] = prefix (e.g. whitelist_approve)
     * parts[1] = userId (Discord User ID)
     * parts[2] = minecraftUsername
     */
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
