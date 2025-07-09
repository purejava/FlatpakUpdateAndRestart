package org.purejava.portal;

import org.freedesktop.dbus.FileDescriptor;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.Variant;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.purejava.portal.rest.UpdateCheckerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    void spawnProcess() {
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

    @Test
    void restAPI() {
        String appId = "org.gimp.GIMP";
        try (ExecutorService executor = Executors.newFixedThreadPool(10)) {
            var task = new UpdateCheckerTask(appId);
            executor.submit(task);
            try {
                var latestVersion = task.get();
                assertTrue(isVersionGreaterOrEqual(latestVersion, "3.0.4"));
            } catch (Exception e) {
                LOG.error(e.toString(), e.getCause());
            }
        }
    }

    public static boolean isVersionGreaterOrEqual(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");

        int length = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < length; i++) {
            int p1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int p2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;

            if (p1 < p2) return false;
            if (p1 > p2) return true;
        }

        return true;
    }
}
