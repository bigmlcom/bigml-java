package org.bigml.binding.utils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Create all-trusting host name verifier
 */
public class MockHostnameVerifier implements HostnameVerifier {
    public boolean verify(String s, SSLSession sslSession) {
        return true;
    }
}