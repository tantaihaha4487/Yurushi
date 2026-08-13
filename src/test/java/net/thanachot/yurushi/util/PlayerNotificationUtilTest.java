package net.thanachot.yurushi.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerNotificationUtilTest {
    @Test
    void gatesNotificationsByToggleAndChannel() {
        assertTrue(PlayerNotificationUtil.shouldNotify(true, "123"));
        assertFalse(PlayerNotificationUtil.shouldNotify(false, "123"));
        assertFalse(PlayerNotificationUtil.shouldNotify(true, ""));
        assertFalse(PlayerNotificationUtil.shouldNotify(true, "   "));
        assertFalse(PlayerNotificationUtil.shouldNotify(true, null));
    }

    @Test
    void classifiesFirstJoinFromPlayTime() {
        assertTrue(PlayerNotificationUtil.isFirstJoin(0));
        assertFalse(PlayerNotificationUtil.isFirstJoin(1));
    }
}
