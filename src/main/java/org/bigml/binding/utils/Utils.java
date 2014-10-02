package org.bigml.binding.utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Pattern;

public class Utils {

    // Headers
    static String JSON = "application/json; charset=utf-8";

    private static SSLSocketFactory sslSocketFactory;

    public static HttpURLConnection processPOST(String urlString, String json) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        return processHttpRequest(urlString, "POST", json);
    }

    public static HttpURLConnection processPUT(String urlString, String json) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        return processHttpRequest(urlString, "PUT", json);
    }


    public static HttpURLConnection processGET(String urlString) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        return processHttpRequest(urlString, "GET", null);
    }

    public static HttpURLConnection processDELETE(String urlString) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        return processHttpRequest(urlString, "DELETE", null);
    }

    protected static HttpURLConnection processHttpRequest(String urlString, String methodName, String body) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        HttpURLConnection connection = openConnection(new URL(urlString));

        if( methodName.equals("GET") ) {
            connection.addRequestProperty("Accept", JSON);

            connection.setRequestMethod("GET");

        } else if( methodName.equals("DELETE") ) {
            connection.addRequestProperty("Content-Type", JSON);

            connection.setRequestMethod("DELETE");

        } else if( methodName.equals("POST") || methodName.equals("PUT")) {
            connection.addRequestProperty("Content-Type", JSON);

            connection.setRequestMethod(methodName);

            // Let the run-time system (RTS) know that we want input.
            connection.setDoInput (true);

            // Let the RTS know that we want to do output.
            connection.setDoOutput (true);

            // No caching, we want the real thing.
            connection.setUseCaches (false);

            // Sending the body to the server
            DataOutputStream output = new DataOutputStream(connection.getOutputStream());
            output.writeBytes(body);
            output.flush();
            output.close();
        }

        return connection;
    }

    protected static HttpURLConnection openConnection(URL url) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        if( sslSocketFactory == null ) {
            // We need to disable the VERIFY of the certificate until we decide how to use it
            TrustManager[] trustAllCerts = new TrustManager[] { new MockX509TrustManager() };
            // Install the all-trusting trust manager
            final SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            sslSocketFactory = sc.getSocketFactory();
        }

        // Get a reference to the actual SSL socket factory before change it
        SSLSocketFactory oldSSLSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
        // Get a reference to the actual host verifier before change it
        HostnameVerifier oldHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();

        try {
            HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(new MockHostnameVerifier());

            return (HttpURLConnection) url.openConnection();
        } finally {
            // Install the old SSL socket factory
            HttpsURLConnection.setDefaultSSLSocketFactory(oldSSLSocketFactory);
            // Install the old host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(oldHostnameVerifier);
        }
    }


    /**
     * Converts a InputStream to a String.
     * 
     */
    public static String inputStreamAsString(InputStream stream, String encoding)
            throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(stream, encoding));
        StringBuilder sb = new StringBuilder();
        String line = null;

        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }

        br.close();
        return sb.toString();
    }

    /**
     * Reads the contect of a file
     */
    public static String readFile(String filename) {
        String content = null;
        File file = new File(filename);
        try {
            FileReader reader = new FileReader(file);
            char[] chars = new char[(int) file.length()];
            reader.read(chars);
            content = new String(chars);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    /**
     * Returns True if value is a valid URL.
     * 
     */
    public static boolean isUrl(String value) {
        try {
            new URL(value);
            return true;
        } catch (MalformedURLException malformedURLException) {
            return false;
        }
    }

    /**
     * Returns JSONObject child.
     * 
     */
    public static Object getJSONObject(JSONObject json, String path) {
        String field = path;
        if (path.indexOf(".") != -1) {
            field = path.substring(0, path.indexOf("."));
        }

        if (json.get(field) instanceof JSONArray) {
            return json.get(field);
        }
        if (json.get(field) instanceof String) {
            return json.get(field);
        }
        if (json.get(field) instanceof Long) {
            return json.get(field);
        }
        if (json.get(field) instanceof Double) {
            return json.get(field);
        }
        if (json.get(field) instanceof Boolean) {
            return json.get(field);
        }
        json = (JSONObject) json.get(field);

        if (json == null) {
            return null;
        }

        if (path.indexOf(".") == -1) {
            return json;
        }

        path = path.substring(path.indexOf(".") + 1, path.length());
        if (path.length() > 0) {
            return getJSONObject(json, path);
        }

        return null;
    }

    /**
     * Inverts a dictionary changing keys per values
     * 
     * @param json
     *            the json object to invert
     * @return the dictionary inverting keys per values
     */
    public static JSONObject invertDictionary(JSONObject json) {
        JSONObject invertedObject = new JSONObject();
        Iterator iter = json.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            String fieldName = (String) Utils
                    .getJSONObject(json, key + ".name");
            JSONObject jsonObj = (JSONObject) json.get(key);
            jsonObj.put("fieldID", key);
            invertedObject.put(fieldName, jsonObj);
        }
        return invertedObject;
    }

    private static final Pattern NONLATIN = Pattern.compile("[^\\w]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    /**
     * Translates a field name into a variable name.
     */
    public static String slugify(String input) {
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("_");
        String normalized = Normalizer.normalize(nowhitespace, Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }

}