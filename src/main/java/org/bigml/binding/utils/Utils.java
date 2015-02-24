package org.bigml.binding.utils;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.localmodel.Tree;
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
import java.text.MessageFormat;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.*;
import java.util.regex.Pattern;

public class Utils {

    // Headers
    static String JSON = "application/json; charset=utf-8";

    private static SSLSocketFactory sslSocketFactory;

    // Map operator str to its corresponding java operator
    static HashMap<String, Class> JAVA_TYPE_MAP = new HashMap<String, Class>();
    static {
        JAVA_TYPE_MAP.put("categorical", String.class);
        JAVA_TYPE_MAP.put("numeric", Double.class);
        JAVA_TYPE_MAP.put("text", String.class);
    }


    private static final Map<String, String[][]> LOCALE_SYNONYMS = Collections.unmodifiableMap(
            new HashMap<String, String[][]>() {{
                put("en", new String[][] {
                        { "en_US", "en-US", "en_US.UTF8", "en_US.UTF-8", "English_United States.1252",
                                "en-us", "en_us", "en_US.utf8"},
                        { "en_GB", "en-GB", "en_GB.UTF8", "en_GB.UTF-8", "English_United Kingdom.1252",
                                "en-gb", "en_gb", "en_GB.utf8"}
                });

                put("es", new String[][] {
                        { "es_ES", "es-ES", "es_ES.UTF8", "es_ES.UTF-8",
                                "Spanish_Spain.1252", "es-es", "es_es",
                                "es_ES.utf8" }
                });

                put("es", new String[][] {
                        { "es_ES", "es-ES", "es_ES.UTF8", "es_ES.UTF-8",
                                "Spanish_Spain.1252", "es-es", "es_es",
                                "es_ES.utf8"  }
                });

                put("es", new String[][] {
                        { "fr_FR", "fr-FR", "fr_BE", "fr_CH", "fr-BE",
                                "fr-CH", "fr_FR.UTF8", "fr_CH.UTF8",
                                "fr_BE.UTF8", "fr_FR.UTF-8", "fr_CH.UTF-8",
                                "fr_BE.UTF-8", "French_France.1252", "fr-fr",
                                "fr_fr", "fr-be", "fr_be", "fr-ch", "fr_ch",
                                "fr_FR.utf8", "fr_BE.utf8", "fr_CH.utf8" },
                        {"fr_CA", "fr-CA", "fr_CA.UTF8", "fr_CA.UTF-8",
                                "French_Canada.1252", "fr-ca", "fr_ca",
                                "fr_CA.utf8"}
                });

                put("de", new String[][] {
                        { "de_DE", "de-DE", "de_DE.UTF8", "de_DE.UTF-8",
                                "German_Germany.1252", "de-de", "de_de",
                                "de_DE.utf8"  }
                });

                put("ge", new String[][] {
                        { "de_DE", "de-DE", "de_DE.UTF8", "de_DE.UTF-8",
                                "German_Germany.1252", "de-de", "de_de",
                                "de_DE.utf8"  }
                });

                put("it", new String[][] {
                        { "it_IT", "it-IT", "it_IT.UTF8", "it_IT.UTF-8",
                                "Italian_Italy.1252", "it-it", "it_it",
                                "it_IT.utf8"  }
                });

                put("ca", new String[][] {
                        { "ca_ES", "ca-ES", "ca_ES.UTF8", "ca_ES.UTF-8",
                                "Catalan_Spain.1252", "ca-es", "ca_es",
                                "ca_ES.utf8"  }
                });
            }});

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
            OutputStreamWriter output = new OutputStreamWriter(connection.getOutputStream());
            output.write(body);
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
     * Returns JSONObject child.
     *
     */
    public static Object getJSONObject(JSONObject json, String path, Object defaultValue) {
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
            return defaultValue;
        }

        if (path.indexOf(".") == -1) {
            return json;
        }

        path = path.substring(path.indexOf(".") + 1, path.length());
        if (path.length() > 0) {
            return getJSONObject(json, path);
        }

        return defaultValue;
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

    /**
     * Inverts a dictionary changing keys per values using the
     * propertyName argument as the field to be used as keys
     *
     * @param json
     *            the json object to invert
     * @param propertyName
     *            the property in the json to use as the key field
     * @return the dictionary inverting keys per values
     */
    public static JSONObject invertDictionary(JSONObject json, String propertyName) {
        JSONObject invertedObject = new JSONObject();
        Iterator iter = json.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            Object propertyValue = Utils
                    .getJSONObject(json, key + "." + propertyName);
            JSONObject jsonObj = (JSONObject) json.get(key);
            jsonObj.put("fieldID", key);
            invertedObject.put(propertyValue, jsonObj);
        }
        return invertedObject;
    }


    /**
     * Returns the field that is used by the node to make a decision.
     *
     * @param children
     * @return
     */
    public static String split(List<Tree> children) {
        Set<String> fields = new HashSet<String>();
        for (Tree child : children) {
            if( !child.isPredicate() ) {
                String fieldName = (String) ((JSONObject) child.listFields().get(child.getPredicate().getField())).get("name");
                fields.add(fieldName);
            }
        }

        return fields.size() > 0 ? fields.toArray(new String[fields.size()])[0] : null;
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

    /**
     * Strips prefixes and suffixes if present
     *
     * @param value
     * @param field
     * @return
     */
    public static String stripAffixes(String value, JSONObject field) {

        if( field.containsKey("prefix") && value.startsWith((String) field.get("prefix")) ) {
            value = value.substring( ((String) field.get("prefix")).length(), value.length());
        }

        if( field.containsKey("suffix") && value.endsWith((String) field.get("suffix")) ) {
            value = value.substring( 0, ((String) field.get("prefix")).length());
        }

        return value;
    }

    /**
     * Checks expected type in input data values, strips affixes and casts
     *
     * @param inputData
     * @param fields
     * @return
     */
    public static void cast(JSONObject inputData, JSONObject fields) {

        JSONObject invertedFields = Utils.invertDictionary(fields);

        for (Object key : inputData.keySet()) {
            Object value = inputData.get(key);

            String optType = ((JSONObject) invertedFields.get(key)).get("optype").toString();

            if( ("numeric".equals(optType) && value instanceof String) ||
                (!"numeric".equals(optType) && !(value instanceof String)) ) {

                try {

                    if( "numeric".equals(optType) ) {
                        value = stripAffixes(value.toString(), (JSONObject) invertedFields.get(key));
                    }

                    if( "numeric".equals(optType) ) {
                        value = Double.parseDouble(value.toString());
                    } else {
                        value = value.toString();
                    }

                    inputData.put(key, value);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(
                            MessageFormat.format("Mismatch input data type in field \"%s\" for value %s.",
                                    getJSONObject(invertedFields, key + ".name"), value));
                }
            }
            
        }
    }

    /**
     * Maps a BigML type to equivalent Java types.
     *
     * @param optype BigML Type
     */
    public static Class getJavaType(String optype) {
        return (JAVA_TYPE_MAP.containsKey(optype) ? JAVA_TYPE_MAP.get(optype) : String.class);
    }


    public static int[] getRange(int start, int stop, int step) {
        int maxItems = ((stop - start) / step);
        int[] items = new int[maxItems];
        int pos = 0;
        for(int index = start; index < stop; index += step) {
            items[pos] = index;
            pos++;
        }

        return items;
    }

    /**
     * Looks for the given locale or the closest alternatives
     *
     * @param dataLocale
     * @param verbose
     * @return
     */
    public static Locale findLocale(String dataLocale, Boolean verbose) {
        Locale newLocale = null;

        if( dataLocale == null ) {
            newLocale = BigMLClient.DEFAUL_LOCALE;
        } else {
            try {
                newLocale = toLocale(dataLocale);
            } catch (Exception e) {
                // Not loaded!!
            }
        }

        // Lets find synonyms
        if( newLocale == null ) {
            String[][] localeAliasesArr = LOCALE_SYNONYMS.get(dataLocale.substring(0,2));
            if( localeAliasesArr != null ) {
                for (String[] localeAliasArr : localeAliasesArr) {
                    Locale localeAlias = toLocale(localeAliasArr[0]);
                    for (int iLocale = 1; iLocale < localeAliasArr.length; iLocale++) {
                        if( dataLocale.equals(localeAliasArr[iLocale]) ) {
                            newLocale = localeAlias;
                            break;
                        }

                    }

                    if( newLocale != null ) {
                        break;
                    }
                }
            }
        }

        return newLocale;

    }

    //-----------------------------------------------------------------------
    /**
     * <p>Converts a String to a Locale.</p>
     *
     * <p>This method takes the string format of a locale and creates the
     * locale object from it.</p>
     *
     * <pre>
     *   LocaleUtils.toLocale("en")         = new Locale("en", "")
     *   LocaleUtils.toLocale("en_GB")      = new Locale("en", "GB")
     *   LocaleUtils.toLocale("en_GB_xxx")  = new Locale("en", "GB", "xxx")   (#)
     * </pre>
     *
     * <p>(#) The behaviour of the JDK variant constructor changed between JDK1.3 and JDK1.4.
     * In JDK1.3, the constructor upper cases the variant, in JDK1.4, it doesn't.
     * Thus, the result from getVariant() may vary depending on your JDK.</p>
     *
     * <p>This method validates the input strictly.
     * The language code must be lowercase.
     * The country code must be uppercase.
     * The separator must be an underscore.
     * The length must be correct.
     * </p>
     *
     * @param str  the locale String to convert, null returns null
     * @return a Locale, null if null input
     * @throws IllegalArgumentException if the string is an invalid format
     */
    public static Locale toLocale(String str) {
        if (str == null) {
            return null;
        }
        int len = str.length();
        if (len != 2 && len != 5 && len < 7) {
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        char ch0 = str.charAt(0);
        char ch1 = str.charAt(1);
        if (ch0 < 'a' || ch0 > 'z' || ch1 < 'a' || ch1 > 'z') {
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        if (len == 2) {
            return new Locale(str, "");
        } else {
            if (str.charAt(2) != '_') {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            char ch3 = str.charAt(3);
            if (ch3 == '_') {
                return new Locale(str.substring(0, 2), "", str.substring(4));
            }
            char ch4 = str.charAt(4);
            if (ch3 < 'A' || ch3 > 'Z' || ch4 < 'A' || ch4 > 'Z') {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            if (len == 5) {
                return new Locale(str.substring(0, 2), str.substring(3, 5));
            } else {
                if (str.charAt(5) != '_') {
                    throw new IllegalArgumentException("Invalid locale format: " + str);
                }
                return new Locale(str.substring(0, 2), str.substring(3, 5), str.substring(6));
            }
        }
    }
}