package net.thanachot.yurushi.config;

import net.dv8tion.jda.api.entities.Member;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModConfigTest {
    @AfterEach
    void resetRoles() {
        ModConfig.whitelistRole = new ArrayList<>();
    }

    @Test
    void rejectsMissingDiscordMember() {
        assertFalse(ModConfig.hasWhitelistPermission(null));
    }

    @Test
    void emptyRoleListKeepsTheAllowFallback() {
        ModConfig.whitelistRole = List.of();
        Member member = (Member) Proxy.newProxyInstance(
                Member.class.getClassLoader(), new Class[]{Member.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "hasPermission" -> false;
                    case "getRoles" -> List.of();
                    default -> defaultValue(method.getReturnType());
                });

        assertTrue(ModConfig.hasWhitelistPermission(member));
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        return 0;
    }
}
