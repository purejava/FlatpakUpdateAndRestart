package org.purejava.portal;

/**
 * Represents the supported Flatpak spawn flags.
 */
public enum FlatpakSpawnFlag {
    /**
     * Clear the environment.
     */
    CLEAR_ENV(1),

    /**
     * Spawn the latest version of the app.
     */
    LATEST_VERSION(2),

    /**
     * Spawn in a sandbox (equivalent of the sandbox option of flatpak run).
     */
    SANDBOX(4),

    /**
     * Spawn without network (equivalent of the unshare=network option of flatpak run).
     */
    NO_NETWORK(8),

    /**
     * Kill the sandbox when the caller disappears from the session bus.
     */
    WATCH_BUS(16),

    /**
     * Expose the sandbox pids in the caller's sandbox, only supported if using user namespaces for containers (not setuid).
     * Added in version 3 of this interface (available from Flatpak 1.6.0 and later).
     */
    EXPOSE_PIDS(32),

    /**
     * Emit a SpawnStarted signal once the sandboxed process has been fully started.
     * Added in version 4 of this interface (available from Flatpak 1.8.0 and later).
     */
    NOTIFY_START(64),

    /**
     * Expose the sandbox process IDs in the caller's sandbox and the caller's process IDs in the new sandbox.
     * Only supported if using user namespaces for containers (not setuid).
     * Added in version 5 of this interface (available from Flatpak 1.10.0 and later).
     */
    SHARE_PIDS(128),

    /**
     * Don't provide app files at /app in the new sandbox. Instead, /app will be an empty directory.
     * This flag and the app-fd option are mutually exclusive.
     * The caller's Flatpak app files and extensions will be mounted on /run/parent/app.
     * Added in version 6 of this interface (available from Flatpak 1.12.0 and later).
     */
    EMPTY_APP(256);

    private final int value;

    FlatpakSpawnFlag(int value) {
        this.value = value;
    }

    /**
     * Gets the UInt32 value of the flag.
     *
     * @return the numeric value of the flag
     */
    public int getValue() {
        return value;
    }
}

