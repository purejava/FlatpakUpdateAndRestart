package org.purejava.portal;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.FileDescriptor;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.MethodNoReply;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.Variant;

import java.util.List;
import java.util.Map;

@DBusInterfaceName("org.freedesktop.portal.Flatpak")
public interface Flatpak extends DBusInterface {

    /**
     * Read-only property "version"
     */
    UInt32 version();

    /**
     * Read-only property "supports"
     */
    UInt32 supports();

    /**
     * Signal emitted when a spawned process has fully started.
     */
    class SpawnStarted extends DBusSignal {
        public final UInt32 pid;
        public final UInt32 relPid;

        public SpawnStarted(String path, UInt32 pid, UInt32 relPid) throws DBusException {
            super(path, pid, relPid);
            this.pid = pid;
            this.relPid = relPid;
        }
    }

    /**
     * Signal emitted when a spawned process exits.
     */
    class SpawnExited extends DBusSignal {
        public final UInt32 pid;
        public final UInt32 exitStatus;

        public SpawnExited(String path, UInt32 pid, UInt32 exitStatus) throws DBusException {
            super(path, pid, exitStatus);
            this.pid = pid;
            this.exitStatus = exitStatus;
        }
    }

    /**
     * Creates an update monitor object that will emit signals when an update for the caller becomes available,
     * and can be used to install it.
     *
     * @param options Vardict with optional further information.
     * @return Object path for the org.freedesktop.portal.Flatpak.UpdateMonitor object.
     *
     * @see Flatpak.UpdateMonitor
     */
    DBusPath CreateUpdateMonitor(Map<String, Variant<?>> options);

    /**
     * This method lets you start a new instance of your application, optionally enabling a tighter sandbox.
     *
     * @param cwdPath the working directory for the new process
     * @param argv    the argv for the new process, starting with the executable to launch
     * @param fds     an array of file descriptors to pass to the new process
     * @param envs    an array of variable/value pairs for the environment of the new process
     * @param flags   flags, see <a href="https://docs.flatpak.org/en/latest/portal-api-reference.html#gdbus-org.freedesktop.portal.Flatpak">flags</a>
     * @param options Vardict with optional further information
     * @return the PID of the new process
     */
    UInt32 Spawn(
            List<Byte> cwdPath,
            List<List<Byte>> argv,
            Map<UInt32, FileDescriptor> fds,
            Map<String, String> envs,
            UInt32 flags,
            Map<String, Variant<?>> options
    );

    /**
     * Send a Unix signal to a previously spawned process.
     *
     * @param pid            the PID inside the container to signal
     * @param signal         the signal to send (see signal(7))
     * @param toProcessGroup whether to send the signal to the process group
     */
    void SpawnSignal(UInt32 pid, UInt32 signal, boolean toProcessGroup);

    @DBusInterfaceName("org.freedesktop.portal.Flatpak.UpdateMonitor")
    interface UpdateMonitor extends DBusInterface {

        /**
         * Gets emitted to indicate progress of the installation.
         */
        class Progress extends DBusSignal {
            public final Map<String, Variant<?>> info;

            public Progress(String path, Map<String, Variant<?>> info) throws DBusException {
                super(path, info);
                this.info = info;
            }
        }

        /**
         * Signal emitted when an update becomes available.
         */
        class UpdateAvailable extends DBusSignal {
            public final Map<String, Variant<?>> update_info;
            public UpdateAvailable(String path, Map<String, Variant<?>> update_info) throws DBusException {
                super(path, update_info);
                this.update_info = update_info;
            }
        }

        /**
         * Asks to install an update of the calling app.
         *
         * @param parentWindow The window identifier for dialogs.
         * @param options      A dictionary of update-related options.
         */
        void Update(String parentWindow, Map<String, Variant<?>> options);

        /**
         * Ends the update monitoring and cancels any ongoing installation.
         */
        @MethodNoReply
        void Close();

    }
}

