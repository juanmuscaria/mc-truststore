package com.juanmuscaria.truststore.launch;

import com.google.auto.service.AutoService;
import com.juanmuscaria.truststore.TrustStorePatchMod;
import com.juanmuscaria.truststore.logger.Log4jLogger;
import com.juanmuscaria.truststore.logger.LoggerAdapter;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import org.apache.logging.log4j.LogManager;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@AutoService(ITransformationService.class)
public class ModLauncherEntrypoint implements ITransformationService, Platform {
    private final LoggerAdapter logger = new Log4jLogger(LogManager.getLogger(TrustStorePatchMod.LOGGER_NAME));

    @Override
    public String name() {
        return TrustStorePatchMod.LOGGER_NAME;
    }

    @Override
    public void initialize(IEnvironment env) {
        if (register()) {
            TrustStorePatchMod.patch(this);
        }
    }

    @Override
    public void beginScanning(IEnvironment env) {

    }

    @Override
    public void onLoad(IEnvironment iEnvironment, Set<String> set) {
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<ITransformer> transformers() {
        return Collections.emptyList();
    }

    @Override
    public LoggerAdapter logger() {
        return this.logger;
    }
}
