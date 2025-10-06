package com.juanmuscaria.truststore.launch;

import com.juanmuscaria.truststore.TrustStorePatchMod;
import com.juanmuscaria.truststore.logger.Log4jLogger;
import com.juanmuscaria.truststore.logger.LoggerAdapter;
import com.juanmuscaria.truststore.logger.OutStreamLogger;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.apache.logging.log4j.LogManager;

import java.util.logging.Level;

public class FabricEntrypoint implements PreLaunchEntrypoint, Platform{
    private final LoggerAdapter logger = makeTheLogger();

    private static LoggerAdapter makeTheLogger() {
        try {
            return new Log4jLogger(LogManager.getLogger(TrustStorePatchMod.LOGGER_NAME));
        } catch (Throwable ignored) {
            // Either a really old minecraft version or log4j is plain missing?
            return new OutStreamLogger(System.out, TrustStorePatchMod.LOGGER_NAME, Level.INFO);
        }
    }

    @Override
    public LoggerAdapter logger() {
        return this.logger;
    }

    @Override
    public void onPreLaunch() {
        if (register()) {
            TrustStorePatchMod.patch(this);
        }
    }
}
