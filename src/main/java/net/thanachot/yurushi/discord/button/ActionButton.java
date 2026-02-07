package net.thanachot.yurushi.discord.button;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.List;

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

    protected boolean hasRequiredRole(ButtonInteractionEvent event, List<String> allowedRoleIds) {
        Member member = event.getMember();
        if (member == null)
            return false;

        if (member.hasPermission(Permission.ADMINISTRATOR))
            return true;

        if (allowedRoleIds == null || allowedRoleIds.isEmpty())
            return true;

        for (Role role : member.getRoles()) {
            if (allowedRoleIds.contains(role.getId())) {
                return true;
            }
        }
        return false;
    }
}
