module org.purejava.portal {
        requires java.desktop;
        requires org.freedesktop.dbus;
        requires org.slf4j;

        exports org.purejava.portal;
        exports org.purejava.portal.freedesktop.dbus.handlers;
        }