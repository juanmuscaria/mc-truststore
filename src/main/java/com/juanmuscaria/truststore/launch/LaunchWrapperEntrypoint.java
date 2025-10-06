package com.juanmuscaria.truststore.launch;

import com.juanmuscaria.truststore.TrustStorePatchMod;
import com.juanmuscaria.truststore.logger.Log4jLogger;
import com.juanmuscaria.truststore.logger.LoggerAdapter;
import com.juanmuscaria.truststore.logger.OutStreamLogger;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

public class LaunchWrapperEntrypoint implements ITweaker, Platform {
    private final LoggerAdapter logger = makeTheLogger();

    @Override
    public void acceptOptions(List<String> list, File file, File file1, String s) {
        if (register()) {
            TrustStorePatchMod.patch(this);
        }
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader launchClassLoader) {

    }

    @Override
    public String getLaunchTarget() {
        return "net.minecraft.client.main.Main";
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }

    @Override
    public LoggerAdapter logger() {
        return logger;
    }

    private static LoggerAdapter makeTheLogger() {
        try {
            return new Log4jLogger(LogManager.getLogger(TrustStorePatchMod.LOGGER_NAME));
        } catch (Throwable ignored) {
            // Either a really old minecraft version or log4j is plain missing?
            return new OutStreamLogger(System.out, TrustStorePatchMod.LOGGER_NAME, Level.INFO);
        }
    }
}
