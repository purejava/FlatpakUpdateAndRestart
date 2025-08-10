package org.purejava.portal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Util {
    private static final Logger LOG = LoggerFactory.getLogger(Util.class);

    public static List<Byte> stringToByteList(String str) {
        if (varIsEmpty(str)) {
            LOG.error("Cannot stringToByteList as required str is missing");
            return null;
        }
        return IntStream.range(0, str.getBytes(StandardCharsets.UTF_8).length)
                .mapToObj(i -> str.getBytes(StandardCharsets.UTF_8)[i])
                .collect(Collectors.toList());
    }

    public static boolean varIsEmpty(String v) {
        return v == null || v.isBlank();
    }
}
