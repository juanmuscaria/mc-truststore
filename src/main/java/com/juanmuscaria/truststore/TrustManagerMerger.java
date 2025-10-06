package com.juanmuscaria.truststore;

import javax.net.ssl.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TrustManagerMerger {

    public static void main(String[] args) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
        System.out.println( Integer.MIN_VALUE);
        Logger logger = Logger.getLogger(TrustManagerMerger.class.getName());
        AtomicReference<Throwable> e = new AtomicReference<>();
        if (!mergeTrustManagers(loadKeystoreFromResources(TrustManagerMerger.class, "/jdk-25+36-jre/cacerts", "jks"), e)) {
            logger.log(Level.WARNING, "Load failed", e.get());
        }
        if (checkUrl("https://google.com", e)) {
            logger.log(Level.WARNING, "URL test failed", e.get());
        }
        if (checkUrl("https://helloworld.letsencrypt.org", e)) {
            logger.log(Level.WARNING, "URL test failed", e.get());
        }
        if (checkUrl("https://sessionserver.mojang.com", e)) {
            logger.log(Level.WARNING, "URL test failed", e.get());
        }
    }

    public static KeyStore loadKeystoreFromResources(Class<?> clazz, String resourceName, String keystoreType) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        KeyStore ks = KeyStore.getInstance(keystoreType);
        InputStream is = clazz.getResourceAsStream(resourceName);
        if (is == null) {
            throw new IOException("No such resource " + resourceName);
        }
        ks.load(new BufferedInputStream(is), null);

        return ks;
    }

    public static boolean mergeTrustManagers(KeyStore newKeystore, AtomicReference<Throwable> exceptionRef) {
        try {
            if (!Objects.equals(SSLContext.getDefault().getProtocol(), "Default")) {
                throw new UnsupportedOperationException("Default SSLContext was already replaced!");
            }
            // Here we assume we are on hotspot using JSSE with all default configurations, and abort early if that's not the case
            if (!Objects.equals(TrustManagerFactory.getDefaultAlgorithm(), "PKIX")) {
                throw new UnsupportedOperationException("Default trust manager is not PKIX, likely SSL configurations where changed or we are running on a VM that is not using JSSE");
            }
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
            tmf.init((KeyStore) null);// Load from java's default keystore
            X509ExtendedTrustManager defaultTrustManager = findFirstX509ExtendedTrustManager(tmf.getTrustManagers());
            tmf.init(newKeystore); // Load from our keystore
            X509ExtendedTrustManager newTrustManager = findFirstX509ExtendedTrustManager(tmf.getTrustManagers());

            SSLContext sslContext = SSLContext.getInstance("SSL");
            // Merge both TrustManagers
            sslContext.init(null, new TrustManager[]{new MergedX509ExtendedTrustManager(defaultTrustManager, newTrustManager)}, null);
            SSLContext.setDefault(sslContext);
            return true;
        } catch (Throwable e) {
            if (exceptionRef != null) {
                exceptionRef.set(e);
            }
            return false;
        }
    }

    public static boolean checkUrl(String urlString, AtomicReference<Throwable> exceptionRef) {
       try {
           URL url = new URL(urlString);
           URLConnection conn = url.openConnection();
           conn.setConnectTimeout(10000);
           conn.setReadTimeout(10000);
           conn.connect();
           return false;
       } catch (Throwable e) {
           if (exceptionRef != null) {
               exceptionRef.set(new Exception("Failed to test URL: " + urlString, e));
           }
           return true;
       }
    }

    public static X509ExtendedTrustManager findFirstX509ExtendedTrustManager(TrustManager[] managers) {
        for (TrustManager manager : managers) {
            if (manager instanceof X509ExtendedTrustManager) {
                return (X509ExtendedTrustManager) manager;
            }
        }
        throw new NoSuchElementException("No X509ExtendedTrustManager found.");
    }

    public static <T> T[] concact(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    private static class MergedX509ExtendedTrustManager extends X509ExtendedTrustManager {
        private final X509ExtendedTrustManager defaultTrustManager;
        private final X509ExtendedTrustManager newTrustManager;

        public MergedX509ExtendedTrustManager(X509ExtendedTrustManager defaultTrustManager, X509ExtendedTrustManager newTrustManager) {
            this.defaultTrustManager = defaultTrustManager;
            this.newTrustManager = newTrustManager;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try {
                defaultTrustManager.checkClientTrusted(chain, authType);
            } catch (CertificateException e) {
                try {
                    newTrustManager.checkClientTrusted(chain, authType);
                } catch (CertificateException ex) {
                    ex.addSuppressed(e);
                    throw ex;
                }
            }
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try {
                defaultTrustManager.checkServerTrusted(chain, authType);
            } catch (CertificateException e) {
                try {
                    newTrustManager.checkServerTrusted(chain, authType);
                } catch (CertificateException ex) {
                    ex.addSuppressed(e);
                    throw ex;
                }
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return concact(defaultTrustManager.getAcceptedIssuers(), newTrustManager.getAcceptedIssuers());
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
            try {
                defaultTrustManager.checkClientTrusted(chain, authType, socket);
            } catch (CertificateException e) {
                try {
                    newTrustManager.checkClientTrusted(chain, authType, socket);
                } catch (CertificateException ex) {
                    ex.addSuppressed(e);
                    throw ex;
                }
            }
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
            try {
                defaultTrustManager.checkServerTrusted(chain, authType, socket);
            } catch (CertificateException e) {
                try {
                    newTrustManager.checkServerTrusted(chain, authType, socket);
                } catch (CertificateException ex) {
                    ex.addSuppressed(e);
                    throw ex;
                }
            }
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
            try {
                defaultTrustManager.checkClientTrusted(chain, authType, engine);
            } catch (CertificateException e) {
                try {
                    newTrustManager.checkClientTrusted(chain, authType, engine);
                } catch (CertificateException ex) {
                    ex.addSuppressed(e);
                    throw ex;
                }
            }
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
            try {
                defaultTrustManager.checkServerTrusted(chain, authType, engine);
            } catch (CertificateException e) {
                try {
                    newTrustManager.checkServerTrusted(chain, authType, engine);
                } catch (CertificateException ex) {
                    ex.addSuppressed(e);
                    throw ex;
                }
            }
        }
    }
}
