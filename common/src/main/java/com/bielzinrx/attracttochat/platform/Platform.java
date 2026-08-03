package com.bielzinrx.attracttochat.platform;

import java.util.ServiceLoader;

public final class Platform {

    private static final IPlatformHelper HELPER =
        ServiceLoader.load(IPlatformHelper.class)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "[ATC] No IPlatformHelper found! Is the platform module on the classpath?"));

    private Platform() {}

    public static IPlatformHelper getHelper() {
        return HELPER;
    }
}
