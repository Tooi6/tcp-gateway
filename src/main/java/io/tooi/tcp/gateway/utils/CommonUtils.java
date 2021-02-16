package io.tooi.tcp.gateway.utils;

import java.util.UUID;

/**
 * @author Tooi
 * @since 2021-02-14 12:40:35
 */
public class CommonUtils {

    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
