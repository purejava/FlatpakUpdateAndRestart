package org.purejava.portal;

import org.freedesktop.dbus.FileDescriptor;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.Variant;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UpdatePortalTest {

    private static final Logger LOG = LoggerFactory.getLogger(UpdatePortalTest.class);
    private static Context context;
    private static UpdatePortal portal;

    @BeforeAll
    static void setUp() {
        context = new Context();
        context.ensureService();
        portal = new UpdatePortal();
    }

    @AfterAll
    static void tearDown() {
        context.after();
        portal.close();
    }

    @Test
    void isAvailable() {
        assertTrue(portal.isAvailable());
    }

    @Test
    void getVersion() {
        assertTrue(portal.getVersion() >= 7);
    }

    @Test
    void getSupports() {
        assertTrue(portal.getSupports() >= 1);
    }

    @Test
    void createUpdateMonitor() {
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
        int allValidFlags = 0;

        for (FlatpakSpawnFlag flag : FlatpakSpawnFlag.values()) {
            allValidFlags |= flag.getValue();
        }

        var invalidFlags = new UInt32(512);
        assertTrue(portal.areFlagsValid(new UInt32(allValidFlags)));
        assertFalse(portal.areFlagsValid(invalidFlags));
    }

    @Test
    void spawnProcessTest() throws IOException {
        var cwdPath = Util.stringToByteList(System.getProperty("user.dir"));

        List<List<Byte>> argv = List.of(Util.stringToByteList("org.purejava.App"));

        Map<UInt32, FileDescriptor> fds = Collections.emptyMap();

        Map<String, String> envs = Map.of();

        var flags = new UInt32(0);

        Map<String, Variant<?>> options = UpdatePortal.OPTIONS_DUMMY;

        DBusExecutionException exception = assertThrows(
                DBusExecutionException.class,
                () -> portal.Spawn(cwdPath, argv, fds, envs, flags, options)
        );

        assertTrue(
                exception.getMessage().contains("org.freedesktop.portal.Flatpak.Spawn only works in a flatpak"),
                "Expected error message to contain: 'org.freedesktop.portal.Flatpak.Spawn only works in a flatpak'"
        );
    }
}