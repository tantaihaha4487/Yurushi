package net.thanachot.yurushi.manager;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WhitelistManagerTest {
    @Test
    void generatesVanillaCompatibleOfflineUuid() {
        assertEquals(UUID.fromString("b50ad385-829d-3141-a216-7e7d7539ba7f"),
                WhitelistManager.generateOfflineUuid("Notch"));
    }

    @Test
    void mapsWhitelistStatusesToStableMessages() {
        UUID uuid = UUID.randomUUID();
        assertEquals("Successfully whitelisted Alex",
                WhitelistManager.WhitelistResult.success("Alex", uuid).getMessage());
        assertEquals("Alex is already whitelisted",
                WhitelistManager.WhitelistResult.alreadyWhitelisted("Alex").getMessage());
        assertEquals("Player Alex was not found on Mojang's servers",
                WhitelistManager.WhitelistResult.playerNotFound("Alex").getMessage());
        assertEquals("Error whitelisting Alex: failed",
                WhitelistManager.WhitelistResult.error("Alex", "failed").getMessage());
    }
}
