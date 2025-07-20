package org.purejava.portal;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.FileDescriptor;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBus;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.Variant;
import org.purejava.portal.freedesktop.dbus.handlers.Messaging;
import org.purejava.portal.rest.UpdateCheckerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdatePortal extends Messaging implements Flatpak {

    public static final Map<String, Variant<?>> OPTIONS_DUMMY = new HashMap<>();
    private static final Logger LOG = LoggerFactory.getLogger(UpdatePortal.class);
    private static final String BUS_NAME = "org.freedesktop.portal.Flatpak";
    private static final String DBUS_PATH = "/org/freedesktop/portal/Flatpak";
    private static final String PORTAL_NOT_AVAILABLE = "Flatpak portal not available on DBus";
    private static DBusConnection connection;

    private Flatpak flatpak = null;
    private UpdateCheckerTask task;

    static {
        try {
            connection = DBusConnectionBuilder.forSessionBus().withShared(false).build();
            connection.getRemoteObject("org.freedesktop.DBus",
                    "/org/freedesktop/DBus", DBus.class);
        } catch (DBusException e) {
            LOG.error(e.toString(), e.getCause());
        }
    }

    public UpdatePortal() {
        super(connection, BUS_NAME, DBUS_PATH, BUS_NAME);
        if (null != connection) {
            try {
                this.flatpak = connection.getRemoteObject(BUS_NAME, DBUS_PATH, Flatpak.class);
            } catch (DBusException e) {
                LOG.error(e.toString(), e.getCause());
            }
        } else {
            LOG.error("Dbus not available");
        }
    }

    public boolean isAvailable() {
        try {
            connection.getRemoteObject(BUS_NAME, DBUS_PATH, Flatpak.class);
            return true;
        } catch (DBusException e) {
            LOG.error(e.toString(), e.getCause());
            return false;
        }
    }

    private boolean isUsable() {
        return null != flatpak;
    }

    public boolean areFlagsValid(UInt32 flags) {
        int allValidFlags = 0;

        // Combine all enum values to build a mask of valid bits
        for (FlatpakSpawnFlag flag : FlatpakSpawnFlag.values()) {
            allValidFlags |= flag.getValue();
        }

        // Extract the primitive int from UInt32
        int providedFlags = flags.intValue();

        // If flags contain only valid bits, (flags & ~allValidFlags) should be zero
        return (providedFlags & ~allValidFlags) == 0;
    }

    @Override
    public UInt32 version() {
        if (!isUsable()) {
            LOG.error(PORTAL_NOT_AVAILABLE);
            return null;
        }
        var response = getProperty("version");
        return null == response ? null : (UInt32) response.getValue();
    }

    /**
     * Read-only property "version"
     *
     * @return version of the Flatpak portal
     */
    public Long getVersion() {
        var v = version();
        return null == v ? null : v.longValue();
    }

    @Override
    public UInt32 supports() {
        if (!isUsable()) {
            LOG.error(PORTAL_NOT_AVAILABLE);
            return null;
        }
        var response = getProperty("supports");
        return null == response ? null : (UInt32) response.getValue();
    }

    /**
     * Read-only property "supports"
     *
     * @return indicator, what features are supported
     */
    public Long getSupports() {
        var s = supports();
        return null == s ? null : s.longValue();
    }

    /**
     * Creates an update monitor object that will emit signals when an update for the caller becomes available,
     * and can be used to install it.
     *
     * @param options Vardict with optional further information.
     * @return Object path for the org.freedesktop.portal.Flatpak.UpdateMonitor object.
     * @see Flatpak.UpdateMonitor
     */
    @Override
    public DBusPath CreateUpdateMonitor(Map<String, Variant<?>> options) {
        if (isUsable()) {
            return flatpak.CreateUpdateMonitor(options);
        }
        LOG.error(PORTAL_NOT_AVAILABLE);
        return null;
    }

    public UpdateMonitor getUpdateMonitor(String dbusPath) {
        if (Util.varIsEmpty(dbusPath)) {
            LOG.error("Cannot retrieve UpdateMonitor as required DBusPath is missing");
            return null;
        }
        if (null == connection) {
            LOG.error("Cannot retrieve UpdateMonitor as required DBus connection is missing");
            return null;
        }
        try {
            return connection.getRemoteObject(BUS_NAME, dbusPath, UpdateMonitor.class);
        } catch (DBusException e) {
            LOG.error(e.toString(), e.getCause());
            return null;
        }
    }

    /**
     * Ends the update monitoring and cancels any ongoing installation.
     *
     * @param monitor The UpdateMonitor to be ended.
     */
    public void cancelUpdateMonitor(UpdateMonitor monitor) {
        if (null == monitor) {
            LOG.error("Cannot cancel UpdateMonitor as none was provided");
            return;
        }
        monitor.Close();
    }

    /**
     * Asks to install an update of the calling app.
     *
     * @param parentWindow The window identifier for dialogs.
     * @param monitor      The UpdateMonitor to trigger the update.
     * @param options      A dictionary of update-related options.
     */
    public void updateApp(String parentWindow, UpdateMonitor monitor, Map<String, Variant<?>> options) {
        if (Util.varIsEmpty(parentWindow)) {
            LOG.error("Cannot update Application as required parentWindow is missing");
            return;
        }
        if (null == monitor) {
            LOG.error("Cannot update Application as required UpdateMonitor is missing");
            return;
        }
        if (null == options) {
            LOG.error("Cannot update Application as required options are missing");
            return;
        }
        monitor.Update(parentWindow, options);
    }

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
    @Override
    public UInt32 Spawn(List<Byte> cwdPath, List<List<Byte>> argv, Map<UInt32, FileDescriptor> fds, Map<String, String> envs, UInt32 flags, Map<String, Variant<?>> options) {
        if (!isUsable()) {
            LOG.error(PORTAL_NOT_AVAILABLE);
            return null;
        }
        if (cwdPath.isEmpty()) {
            LOG.error("Cannot start a new instance of the application as required cwdPath is missing");
            return null;
        }
        if (argv.isEmpty()) {
            LOG.error("Cannot start a new instance of the application as required argv are missing");
            return null;
        }
        if (!areFlagsValid(flags)) {
            LOG.error("Cannot start a new instance of the application as invalid flags were provided: {}", flags);
            return null;
        }
        if (null == options) {
            LOG.error("Cannot start a new instance of the application as required options are missing");
            return null;
        }
        return flatpak.Spawn(cwdPath, argv, fds, envs, flags, options);
    }

    @Override
    public void SpawnSignal(UInt32 pid, UInt32 signal, boolean toProcessGroup) {
        if (isUsable()) {
            flatpak.SpawnSignal(pid, signal, toProcessGroup);
        }
        LOG.error(PORTAL_NOT_AVAILABLE);
    }

    public void setUpdateCheckerTaskFor(String appName) {
        task = new UpdateCheckerTask(appName);
    }

    public String getAppId(String appName) {
        if (Util.varIsEmpty(appName)) {
            LOG.error("Cannot get appName of task as required appName is missing'");
            return null;
        }
        return task.getAppId();
    }

    public boolean isAppId(String appName) {
        if (Util.varIsEmpty(appName)) {
            LOG.error("Cannot check appName of task as required appName is missing'");
            return false;
        }
        return appName.equals(getAppId(appName));
    }

    public UpdateCheckerTask getUpdateCheckerTaskFor(String appName) {
        if (Util.varIsEmpty(appName)) {
            LOG.error("Cannot lookup UpdateCheckerTask as required appName is missing'");
            return null;
        }
        if (null == task) {
            LOG.error("Cannot lookup UpdateCheckerTask as task wasn't set before, use 'setUpdateCheckerTaskFor'");
            return null;
        }
        if (isAppId(appName)) {
            return task;
        } else {
            LOG.error("No UpdateCheckerTask found for appName: {}", appName);
            return null;
        }
    }

    @Override
    public String getDBusPath() {
        return DBUS_PATH;
    }

    @Override
    public String getObjectPath() {
        return DBUS_PATH;
    }

    public DBusConnection getDBusConnection() {
        return connection;
    }

    public void close() {
        try {
            if (null != connection && connection.isConnected()) connection.disconnect();
        } catch (Exception e) {
            LOG.error(e.toString(), e.getCause());
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> contentOrEmptyList(Object[] o) {
        return null == o ? List.of() : (List<String>) o[0];
    }


    @SuppressWarnings("unchecked")
    private Map<String, Variant> contentOrEmptyMap(Object[] o) {
        return null == o ? Map.of() : (Map<String, Variant>) o[0];
    }
}
