package org.purejava.portal;

import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.Variant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class UpdatePortalTest {

    private static final Logger LOG = LoggerFactory.getLogger(UpdatePortalTest.class);
    private Context context;
    private UpdatePortal portal;

    @BeforeEach
    void setUp() {
        context = new Context();
        context.ensureService();
    }

    @AfterEach
    void tearDown() {
        context.after();
        portal.close();
    }

    @Test
    void isAvailable() {
        portal = new UpdatePortal();
        assertTrue(portal.isAvailable());
    }

    @Test
    void getVersion() {
        portal = new UpdatePortal();
        assertTrue(portal.getVersion() >= 7);
    }

    @Test
    void getSupports() {
        portal = new UpdatePortal();
        assertTrue(portal.getSupports() >= 1);
    }

    @Test
    void createUpdateMonitor() {
        portal = new UpdatePortal();
        Map<String, Variant<?>> options = new HashMap<>();
        // Currently, options are not supported
        DBusExecutionException exception = assertThrows(
                DBusExecutionException.class,
                () -> portal.CreateUpdateMonitor(options)
        );

        assertTrue(
                exception.getMessage().contains("Updates only supported by flatpak apps"),
                "Expected error message to contain: 'Updates only supported by flatpak apps'"
        );
    }

    @Test
    void checkSpawnFlags() {
        portal = new UpdatePortal();
        int allValidFlags = 0;

        for (FlatpakSpawnFlag flag : FlatpakSpawnFlag.values()) {
            allValidFlags |= flag.getValue();
        }

        UInt32 invalidFlags = new UInt32(512);
        assertTrue(portal.areFlagsValid(new UInt32(allValidFlags)));
        assertFalse(portal.areFlagsValid(invalidFlags));
    }
}