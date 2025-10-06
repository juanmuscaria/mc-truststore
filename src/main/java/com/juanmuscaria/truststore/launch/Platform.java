package com.juanmuscaria.truststore.launch;

import com.juanmuscaria.truststore.logger.LoggerAdapter;

import java.util.concurrent.atomic.AtomicReference;

public interface Platform {
    static Platform current() {
        Platform platform = REF.CURRENT.get();
        if (platform == null) {
            throw new IllegalStateException("Too early!");
        }
        return platform;
    }

    default boolean register() {
        return REF.CURRENT.compareAndSet(null, this);
    }

    LoggerAdapter logger();
}
class REF {
    static AtomicReference<Platform> CURRENT = new AtomicReference<>();
}
