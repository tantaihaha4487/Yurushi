package net.thanachot.yurushi.discord;

import net.thanachot.yurushi.discord.button.ActionButton;
import net.thanachot.yurushi.discord.modal.BaseModal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComponentIdTest {
    @Test
    void parsesButtonIdsWithoutChangingTheirContract() {
        String[] parts = ActionButton.parseButtonId("whitelist_approve:123:PlayerName");

        assertArrayEquals(new String[]{"whitelist_approve", "123", "PlayerName"}, parts);
        assertTrue(ActionButton.isValidButtonData(parts));
        assertFalse(ActionButton.isValidButtonData(new String[]{"whitelist_approve", "123"}));
    }

    @Test
    void parsesModalIdsWithoutChangingTheirContract() {
        String[] parts = BaseModal.parseModalId("denial_modal:123:PlayerName");

        assertArrayEquals(new String[]{"denial_modal", "123", "PlayerName"}, parts);
        assertTrue(BaseModal.isValidModalData(parts, 3));
        assertFalse(BaseModal.isValidModalData(new String[]{"denial_modal"}, 3));
    }
}
