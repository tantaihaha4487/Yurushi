package net.thanachot.yurushi.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageConfigTest {
    @Test
    void replacesPairedPlaceholdersAndIgnoresAnUnpairedKey() {
        assertEquals("Hello Alex ({uuid})",
                MessageConfig.replacePlaceholders("Hello {name} ({uuid})", "name", "Alex", "uuid"));
    }
}
