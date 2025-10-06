package com.juanmuscaria.truststore;

import com.juanmuscaria.truststore.launch.Platform;
import com.juanmuscaria.truststore.logger.LoggerAdapter;

import java.security.KeyStore;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import static com.juanmuscaria.truststore.TrustManagerMerger.*;

public class TrustStorePatchMod {
    public static final String LOGGER_NAME = "TrustStorePatch";
    public static void patch(Platform platform) {
        LoggerAdapter logger = platform.logger();
        logger.info("Trust Store Patch loaded from " + platform.getClass().getSimpleName());
        KeyStore keystore;
        try {
            keystore = loadKeystoreFromResources(TrustManagerMerger.class, "/jdk-25+36-jre/cacerts", "jks");
        } catch (Throwable e) {
            logger.warn("Unable to load embedded keystore! No changes will be made.", e);
            return;
        }

        AtomicReference<Throwable> exception = new AtomicReference<>();
        if (!mergeTrustManagers(keystore, exception)) {
            logger.warn("Trust store merge failed! No changes where made.", exception.get());
        } else {
            ForkJoinPool.commonPool().submit(() -> {
                if (checkUrl("https://google.com", exception)) {
                    logger.warn("URL test failed", exception.get());
                }
                if (checkUrl("https://helloworld.letsencrypt.org", exception)) {
                    logger.warn("URL test failed", exception.get());
                }
                if (checkUrl("https://sessionserver.mojang.com", exception)) {
                    logger.warn("URL test failed", exception.get());
                }
                if (exception.get() != null) {
                    logger.error("One or more URL tests failed, perhaps the patch failed.");
                } else {
                    logger.info("All URL tests passed!");
                }
            });
        }
    }
}
