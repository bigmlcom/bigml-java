package org.bigml.binding.utils;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;

public class MockSSLSocketFactory extends SSLSocketFactory {

    public MockSSLSocketFactory() throws NoSuchAlgorithmException,
            KeyManagementException, KeyStoreException,
            UnrecoverableKeyException {
        super(trustStrategy, hostnameVerifier);
    }

    private static final X509HostnameVerifier hostnameVerifier = new X509HostnameVerifier() {

        public void verify(String host, SSLSocket ssl) throws IOException {
            // Do nothing
        }

        public void verify(String host, X509Certificate cert)
                throws SSLException {
            // Do nothing
        }

        public void verify(String host, String[] cns, String[] subjectAlts)
                throws SSLException {
            // Do nothing
        }

        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    };

    private static final TrustStrategy trustStrategy = new TrustStrategy() {
        public boolean isTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            return true;
        }
    };

}
