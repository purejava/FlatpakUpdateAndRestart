package org.purejava.portal;

import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.exceptions.DBusException;
import org.purejava.portal.freedesktop.dbus.handlers.Messaging;
import org.freedesktop.dbus.interfaces.DBus;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

// Todo SignalHandler ergänzen
// Todo testen, dass die übergebenen Parametertypen korrekt sind
public class UpdatePortal extends Messaging implements Flatpak {

    private static final Logger LOG = LoggerFactory.getLogger(UpdatePortal.class);
    private static final String BUS_NAME = "org.freedesktop.portal.Flatpak";
    private static final String DBUS_PATH = "/org/freedesktop/portal/desktop";
    private static DBusConnection connection;

    public UpdatePortal() {
        try {
            connection = DBusConnectionBuilder.forSessionBus().withShared(false).build();
            connection.getRemoteObject("org.freedesktop.DBus",
                    "/org/freedesktop/DBus", DBus.class);
        } catch (DBusException e) {
            LOG.error(e.toString(), e.getCause());
        }
        super(connection, BUS_NAME, DBUS_PATH, BUS_NAME);
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

    @Override
    public UInt32 getVersion() {
        var response = send("getVersion");
        return null == response ? new UInt32(-1) : (UInt32) response[0];
    }

    @Override
    public UInt32 getSupports() {
        var response = send("getSupports");
        return null == response ? new UInt32(-1) : (UInt32) response[0];
    }

    @Override
    public String CreateUpdateMonitor(Map<String, Variant<?>> options) {
        return "";
    }

    @Override
    public UInt32 Spawn(List<byte[]> cwdPath, List<List<byte[]>> argv, Map<UInt32, Integer> fds, Map<String, String> envs, UInt32 flags, Map<String, Variant<?>> options) {
        return null;
    }

    @Override
    public void SpawnSignal(UInt32 pid, UInt32 signal, boolean toProcessGroup) {

    }

    @Override
    public String getDBusPath() {
        return DBUS_PATH;
    }

    @Override
    public String getObjectPath() {
        return DBUS_PATH;
    }

    public void close() {
        try {
            if (null != connection && connection.isConnected()) connection.disconnect();
        } catch (Exception e) {
            LOG.error(e.toString(), e.getCause());
        }
    }

    private List<String> contentOrEmptyList(Object[] o) {
        return null == o ? List.of() : (List<String>) o[0];
    }


    private Map<String, Variant> contentOrEmptyMap(Object[] o) {
        return null == o ? Map.of() : (Map<String, Variant>) o[0];
    }
}
