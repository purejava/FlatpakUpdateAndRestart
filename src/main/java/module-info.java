module org.purejava.portal {
    requires java.desktop;
    requires org.freedesktop.dbus;
    requires org.slf4j;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    exports org.purejava.portal;
    exports org.purejava.portal.rest;
    exports org.purejava.portal.freedesktop.dbus.handlers;
}