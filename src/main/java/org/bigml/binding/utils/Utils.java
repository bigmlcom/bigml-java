package org.bigml.binding.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.bigml.binding.BigMLClient;
import org.bigml.binding.Constants;
import org.bigml.binding.localmodel.Tree;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

    /**
     * Logging
     */
    static Logger logger = LoggerFactory.getLogger(
    	Utils.class.getName());

    // Headers
    static String JSON = "application/json; charset=utf-8";

    private static SSLSocketFactory sslSocketFactory;

    private static Random random = new Random(System.currentTimeMillis());

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

    protected static HttpURLConnection processHttpRequest(String urlString, String methodName, String body)
    		throws NoSuchAlgorithmException, KeyManagementException, IOException {
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
            OutputStreamWriter output = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            output.write(Utils.unescapeJSONString(body));
            output.flush();
            output.close();
        }

        return connection;
    }

    public static HttpURLConnection openConnection(URL url) throws NoSuchAlgorithmException, KeyManagementException, IOException {
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
     * Reads the content of a file
     */
    public static String readFile(String filename) {
        StringBuilder content = new StringBuilder();
        try {
            File fileDir = new File(filename);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(fileDir), "UTF8"));

            String str;

            while ((str = in.readLine()) != null) {
                content.append(str).append("\n");
            }

            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }

//    public static String readFile(String filename) {
//        String content = null;
//        File file = new File(filename);
//        try {
//            FileReader reader = new FileReader(file);
//            char[] chars = new char[(int) file.length()];
//            reader.read(chars);
//            content = new String(chars, Charset.forName("UTF-8"));
//            reader.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return content;
//    }
//
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
        if (json.get(field) instanceof Integer) {
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

    private static <T1, T2> T1 cast(T2 o, T1 d) {

        if (o instanceof Double && d instanceof Long) {
            return (T1)((Long)((Number)o).longValue());
        }
        if (o instanceof Long && d instanceof Double) {
            return (T1)((Double)((Number)o).doubleValue());
        }
        if (o instanceof Double && d instanceof Float) {
            return (T1)((Float)((Number)o).floatValue());
        }
        if (o instanceof Float && d instanceof Double) {
            return (T1)((Double)((Number)o).doubleValue());
        }
        return (T1)o;
    }

    public static <T> T getFromJSONOr(JSONObject json,
                                      String key,
                                      T def) {
      T result = def;
      if (json.containsKey(key)) {
        Object obj = getJSONObject(json, key);
        if (obj != null) {
          result = cast(obj, def);
        }
      }
      return result;
    }

    public static JSONObject getFromJSONOr(JSONObject json,
                                           String key) {

      return getFromJSONOr(json, key, new JSONObject());
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
            return getJSONObject(json, path, defaultValue);
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
     */
    public static String split(List<Tree> children) {
        Set<String> fields = new HashSet<String>();
        for (Tree child : children) {
            if( !child.isPredicate() ) {
                fields.add(child.getPredicate().getField());
            }
        }

        return fields.size() > 0 ? fields.toArray(new String[fields.size()])[0] : null;
    }

    private static final Pattern NONLATIN = Pattern.compile("[^\\w]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    /**
     * Translates a field name into a variable name.
     */
    public static String slugify(String input, List<String> reservedKeywords, String prefix) {
        if( prefix == null || prefix.trim().length() == 0 ) {
            prefix = "";
        }

        String normalized = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD);
        String accentsgone = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String slug  = NONLATIN.matcher(accentsgone).replaceAll("_");

        slug = slug.toLowerCase(Locale.ENGLISH) ;

        if( !Character.isLetter(slug.charAt(0)) ) {
            slug = "fields_" + slug;
        }

        if( reservedKeywords != null && reservedKeywords.contains(slug) ) {
            slug = prefix + slug;
        }

        return slug;
    }

    /**
     * Strips prefixes and suffixes if present
     *
     * @param value
     * @param field
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
     */
    public static void cast(JSONObject inputData, JSONObject fields) {

        for (Object fieldId : inputData.keySet()) {
            Object value = inputData.get(fieldId);

            JSONObject field = (JSONObject) fields.get(fieldId);

            String optType = (String) Utils.getJSONObject(field, "optype");

            if( ("numeric".equals(optType) && value instanceof String) ||
                    (!"numeric".equals(optType) && !(value instanceof String)) ) {

                try {

                    if( "numeric".equals(optType) ) {
                        value = stripAffixes(value.toString(), field);
                    }

                    if ("numeric".equals(optType)) {
                        value = Double.parseDouble(value.toString());
                    } else {
                        value = value.toString();
                    }
                } catch (Exception e) {
                    throw new IllegalStateException(
                            String.format("Mismatch input data type in field " +
                                    "\"%s\" for value %s.", field.get("name"), value.toString()));
                }
            }
        }

//        return inputData;

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
        int maxItems = ((stop - start + 1) / step);
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
            String[][] localeAliasesArr = LOCALE_SYNONYMS.get(dataLocale.substring(0, 2));
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

    public static String join(Collection list, String delim) {

        StringBuilder sb = new StringBuilder();

        String loopDelim = "";

        for(Object s : list) {

            sb.append(loopDelim);
            sb.append(s);

            loopDelim = delim;
        }

        return sb.toString();
    }


    /**
     * Joins all the string items in the list using the conjunction text
     *
     * @param list
     * @param conjunction
     */
    public static String join(List<String> list, String conjunction) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String item : list) {
            if(first)
                first = false;
            else
                sb.append(conjunction);
            sb.append(item);
        }
        return sb.toString();
    }



    public static long getExponentialWait(long waitTime, int retryCount) {
        double delta = Math.pow(retryCount,2) * waitTime / 2;
        double expFactor = retryCount > 1 ? delta : 0;
        return (long) (waitTime + Math.floor(random.nextLong() * expFactor));
    }

    /**
     * Computes the mean of a distribution in the [[point, instances]] syntax
     *
     * @param distribution
     */
    public static double meanOfDistribution(List<JSONArray> distribution) {
        double addition = 0.0f;
        long count = 0;

        for (JSONArray bin : distribution) {
            double point = ((Number) bin.get(0)).doubleValue();
            long instances = ((Number) bin.get(1)).longValue();

            addition += point * instances;
            count += instances;
        }

        if( count > 0 ) {
            return addition / count;
        }

        return Double.NaN;
    }

    /**
     * Computes the mean of a list of double values
     */
    public static double meanOfValues(List<Double> values) {
        double addition = 0.0f;
        long count = values.size();

        for (Double value : values) {
            addition += value;
        }

        return addition / count;
    }


    /**
     * Prints distribution data
     */
    public static StringBuilder printDistribution(JSONArray distribution) {
        StringBuilder distributionStr = new StringBuilder();

        int total = 0;
        for (Object binInfo : distribution) {
            JSONArray binInfoArr = (JSONArray) binInfo;
            total += ((Number) binInfoArr.get(1)).intValue();
        }

        for (Object binInfo : distribution) {
            JSONArray binInfoArr = (JSONArray) binInfo;
            distributionStr.append(String.format("    %s: %.2f%% (%d instance%s)\n",
                    binInfoArr.get(0),
                    Utils.roundOff((float) (((Number) binInfoArr.get(1)).intValue() * 1.0 / total), 4) * 100,
                    binInfoArr.get(1),
                    (((Number) binInfoArr.get(1)).intValue() == 1 ? "" : "s")
                    ));
        }


        return distributionStr;
    }

    /**
     * Adds up a new distribution structure to a map formatted distribution
     *
     * @param distribution
     * @param newDistribution
     */
    public static Map<Object, Number> mergeDistributions(Map<Object, Number> distribution, Map<Object, Number> newDistribution) {
        if (newDistribution != null) {
            for (Object value : newDistribution.keySet()) {
                if( !distribution.containsKey(value) ) {
                    distribution.put(value, 0);
                }
                distribution.put(value, distribution.get(value).intValue() + newDistribution.get(value).intValue());
            }
        }
        return distribution;
    }


    /**
     * We switch the Array to a Map structure in order to be more easily manipulated
     *
     * @param distribution current distribution as an JSONArray instance
     * @return the distribution as a Map instance
     */

    public static Map<Object, Number> convertDistributionArrayToMap(JSONArray distribution) {
        Map<Object, Number> newDistribution = new HashMap<Object, Number>();
        if (distribution != null) {
	        for (Object distValueObj : distribution) {
	            JSONArray distValueArr = (JSONArray) distValueObj;
	            newDistribution.put(distValueArr.get(0), (Number) distValueArr.get(1));
	        }
        }

        return newDistribution;
    }


    /**
     * Round a float number x to n decimal places
     */
    public static float roundOff(float x, int n)  {
        BigDecimal bd = new BigDecimal(x).setScale(n, RoundingMode.HALF_EVEN);
        return bd.floatValue();
    }

    /**
     * Round a double number x to n decimal places
     */
    public static double roundOff(double x, int n)  {
        BigDecimal bd = new BigDecimal(x).setScale(n, RoundingMode.HALF_EVEN);
        return bd.doubleValue();
    }

    /**
     * We switch the Array to a Map structure in order to be more easily manipulated
     *
     * @param distribution current distribution as an JSONArray instance
     * @return the distribution as a Map instance
     */

    public static JSONArray convertDistributionMapToSortedArray(Map<Object, Number> distribution) {
        JSONArray newDistribution = new JSONArray();

        String opType = Constants.OPTYPE_NUMERIC;

        if (distribution != null) {
            for (Object key : distribution.keySet()) {
                JSONArray element = new JSONArray();
                element.add(key);
                element.add(distribution.get(key));
                newDistribution.add(element);

                if( key instanceof Number ) {
                    opType = Constants.OPTYPE_NUMERIC;
                } else if( key instanceof String ) {
                    opType = Constants.OPTYPE_TEXT;
                }
            }
        }

        if( distribution != null && !distribution.isEmpty() ) {
            final String finalOpType = opType;

            Collections.sort(newDistribution, new Comparator<JSONArray>() {
                @Override
                public int compare(JSONArray jsonArray1, JSONArray jsonArray2) {
                    if( Constants.OPTYPE_NUMERIC.equals(finalOpType) ) {
                        return Double.compare( ((Number) jsonArray1.get(0)).doubleValue(),
                                ((Number) jsonArray2.get(0)).doubleValue());
                    } else if( Constants.OPTYPE_TEXT.equals(finalOpType) ) {
                        return ((String) jsonArray1.get(0)).compareTo( (String) jsonArray2.get(0));
                    } else { // OPTYPE_DATETIME
                        // TODO: implement this
                        throw new UnsupportedOperationException();
                    }
                }
            });
        }

        return newDistribution;
    }

    /**
     * Merges the bins of a regression distribution to the given limit number
     */
    public static JSONArray mergeBins(JSONArray distribution, int limit) {
        int length = distribution.size();
        if( limit < 1 || length <= limit || length < 2 ) {
            return distribution;
        }

        int indexToMerge = 2;
        double shortest = Double.MAX_VALUE;
        for(int index = 1; index < length; index++) {
            double distance = ((Number) ((JSONArray) distribution.get(index)).get(0)).doubleValue() -
                    ((Number) ((JSONArray) distribution.get(index - 1)).get(0)).doubleValue();

            if( distance < shortest ) {
                shortest = distance;
                indexToMerge = index;
            }
        }

        JSONArray newDistribution = new JSONArray();
        newDistribution.addAll(distribution.subList(0, indexToMerge - 1));

        JSONArray left = (JSONArray) distribution.get(indexToMerge - 1);

        JSONArray right = (JSONArray) distribution.get(indexToMerge);

        JSONArray newBin = new JSONArray();
        newBin.add(0, ( ((((Number) left.get(0)).doubleValue() * ((Number) left.get(1)).doubleValue()) +
                (((Number) right.get(0)).doubleValue() * ((Number) right.get(1)).doubleValue())) /
                (((Number) left.get(1)).doubleValue() + ((Number) right.get(1)).doubleValue()) ) );
        newBin.add(1, ((Number) left.get(1)).longValue() + ((Number) right.get(1)).longValue());


        newDistribution.add(newBin);

        if( indexToMerge < (length - 1) ) {
            newDistribution.addAll(distribution.subList(indexToMerge + 1, distribution.size()));
        }

        return mergeBins(newDistribution, limit);
    }

    /**
     * Merges the bins of a regression distribution to the given limit number
     */
    public static Map<Object, Number> mergeBins(Map<Object, Number> distribution, int limit) {
        JSONArray mergedDist = mergeBins(convertDistributionMapToSortedArray(distribution), limit);
        return convertDistributionArrayToMap(mergedDist);
    }

    /**
     * Determines if the given collection contain the same value.
     *
     * We will use the contains method of the list to check if the value is inside
     *
     * @param collection the list of elements
     * @param value the value to check.
     * @return true if all the elements are equals to value or false in otherwise
     */
    public static boolean sameElement(List collection, Object value) {
        if( collection == null || collection.isEmpty() ) {
            return false;
        }

        // Iterate over the elements of the collection.
        for (Object collectionValue : collection) {
            // Check if the element in the collection is the same of value
            if (!collectionValue.equals(value)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Pluralizer: adds "s" at the end of a string if a given number is > 1
     */
    public static String plural(String text, int num) {
        return String.format("%s%s", text, (num == 1 ? "" : "s"));
    }

    /**
     * JSON library Hack - unescape forward slashes from already escaped JSON string
     */
    public static String unescapeJSONString(String jsonString) {
        return jsonString.replaceAll("\\\\/", "/");
    }



    public static Pattern FULL_TERM_PATTERN_RE = Pattern.compile("^.+\\b.+$", Pattern.UNICODE_CASE);
    public static String TM_TOKENS = "tokens_only";
    public static String TM_FULL_TERM = "full_terms_only";
    public static String TM_ALL = "all";



    public static int termMatches(String text, List<String> formsList, JSONObject options) {

        // Checking Full Terms Only
        String tokenMode = (String) Utils.getJSONObject(options, "token_mode", Utils.TM_TOKENS);
        Boolean caseSensitive = (Boolean) Utils.getJSONObject(options, "case_sensitive", Boolean.TRUE);

        String firstTerm = formsList.get(0);

        if (tokenMode.equals(Utils.TM_FULL_TERM)) {
            return Utils.fullTermMatch(text, firstTerm, caseSensitive);
        }

        // In token_mode='all' we will match full terms using equals and
        // tokens using contains
        if ( Utils.TM_ALL.equals(tokenMode) && formsList.size() == 1 ) {
            if( Utils.FULL_TERM_PATTERN_RE.matcher(firstTerm).find() ) {
                return Utils.fullTermMatch(text, firstTerm, caseSensitive);
            }
        }

        return Utils.termMatchesTokens(text, formsList, caseSensitive);
    }

    /**
     * Counts the match for full terms according to the case_sensitive option
     *
     * @param text
     * @param fullTerm
     * @param caseSensitive
     */
    public static int fullTermMatch(String text, String fullTerm, boolean caseSensitive) {
        return (caseSensitive ? (text.equals(fullTerm) ? 1 : 0) : (text.equalsIgnoreCase(fullTerm) ? 1 : 0));
    }

    /**
     * Counts the number of occurences of the words in forms_list in the text
     *
     * @param text
     * @param formsList
     * @param caseSensitive
     */
    public static int termMatchesTokens(String text, List<String> formsList, boolean caseSensitive) {
        List<String> quotedFormsList = new ArrayList<String>();
        for (String s : formsList) {
            quotedFormsList.add(Pattern.quote(s));
        }
        String expression = String.format("(\\b|_)%s(\\b|_)",
                                          Utils.join(quotedFormsList, "(\\b|_)|(\\b|_)"));
        Pattern pattern = Pattern.compile(expression, (caseSensitive ? Pattern.UNICODE_CASE :
                (Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)));
        Matcher matcher = pattern.matcher(text);
        return (matcher.find() ? matcher.groupCount() : 0);
    }


    /**
     * Checks the operating point contents and extracts and array with
     * the three defined variables
     */
    public static Object[] parseOperatingPoint(JSONObject operatingPoint,
    		String[] operatingKinds, List<String> classNames) {

    	String kind, positiveClass;
    	Double threshold;

    	if (!operatingPoint.containsKey("kind")) {
    		throw new IllegalArgumentException(
           		 	"Failed to find the kind of operating point.");
    	} else {
    		kind = (String) operatingPoint.get("kind");
    		if (!Arrays.asList(operatingKinds).contains(kind)) {
    			throw new IllegalArgumentException(
    				String.format("Unexpected operating point kind. " +
    						"Allowed values are: %s",
    						StringUtils.join(operatingKinds,",")));
    		}
    	}

    	if (!operatingPoint.containsKey("threshold")) {
    		throw new IllegalArgumentException(
           		 	"Failed to find the threshold of the operating point.");
    	}
    	threshold = ((Number) operatingPoint.get("threshold")).doubleValue();
    	if (threshold > 1 || threshold < 0) {
    		throw new IllegalArgumentException(
           		 	"The threshold value should be in the 0 to 1 range.");
    	}

    	if (!operatingPoint.containsKey("positive_class")) {
    		throw new IllegalArgumentException(
           		 	"The operating point needs to have a positive_class" +
    				" attribute");
    	} else {
    		positiveClass = (String) operatingPoint.get("positive_class");
    		if (!classNames.contains(positiveClass)) {
    			throw new IllegalArgumentException(
        				String.format("The positive class must be one of the " +
        						"objective field classes: %s",
        						StringUtils.join(classNames,",")));
    		}
    	}

        return new Object[] {kind, threshold, positiveClass};
    }

    /**
     * Checks whether some numeric fields are missing in the input data
     */
    public static void checkNoMissingNumerics(
    		JSONObject inputData, JSONObject fields, String weightField) {

    	for (Object fieldId : fields.keySet()) {
            JSONObject field = (JSONObject) fields.get(fieldId);
            String optype = (String) Utils.getJSONObject(field, "optype");
            if ("numeric".equals(optype) &&
            		!inputData.containsKey((String) fieldId) &&
            		(weightField == null || !weightField.equals((String) fieldId))) {
            	throw new IllegalArgumentException(
               		 	"Failed to predict. Input data must contain values" +
        				" for all numeric fields to get a prediction.");
            }
    	}
    }


    /**
     * Checks whether some input fields are missing in the input data
     * while not training data has no missings in that field
     */
    public static void checkNoTrainingMissings(
    		JSONObject inputData, JSONObject fields, String weightField,
    		String objectiveFieldId) {

    	for (Object fieldId : fields.keySet()) {
            JSONObject field = (JSONObject) fields.get(fieldId);

            Integer missingCount = ((Number) Utils.getJSONObject(
					(JSONObject) field, "summary.missing_count", 0)).intValue();

            if (!inputData.containsKey((String) fieldId) &&
            		missingCount.intValue() == 0 &&
            		(weightField == null || !weightField.equals((String) fieldId)) &&
            		(objectiveFieldId == null || !objectiveFieldId.equals((String) fieldId))) {
            	throw new IllegalArgumentException(
               		 	"Failed to predict. Input data must contain values" +
        				" for field " + field.get("name") + " to get a prediction.");
            }
    	}
    }


    /**
     * Calculates matrix inverse
     */
    public static JSONArray inverseMatrix(JSONArray matrix) {
    	int n = matrix.size();
		int m = ((JSONArray) matrix.get(0)).size();
		double[][] matrixArray = new double[n][m];

		for(int i = 0; i < n; ++i) {
			JSONArray row = (JSONArray) matrix.get(i);
			for(int j = 0; j < m; ++j) {
				matrixArray[i][j] = ((Number) row.get(j)).doubleValue();
			}
		}

		RealMatrix invMatrix = MatrixUtils.createRealMatrix(matrixArray);
		invMatrix = MatrixUtils.inverse(invMatrix);
		//System.out.println(Arrays.toString(invMatrix.getData()));

		JSONArray result = new JSONArray();

		double[][] data = invMatrix.getData();
		for (int i=0; i < data.length; i++) {
			double[] row = (double[]) data[i];
			ArrayList<Double> rowlList = new ArrayList<Double>();
			for (int j=0; j<row.length; j++) {
				rowlList.add(data[i][j]);
			}
			result.add(rowlList);
	    }

		return result;
	}


    /**
	  * Sorts list of predictions
	  *
	  */
    public static void sortPredictions(JSONArray predictions,
			 final String primaryKey, final String secondaryKey) {

		Collections.sort(predictions, new Comparator<JSONObject>() {
           @Override
           public int compare(JSONObject o1, JSONObject o2) {
           	Double o1p = (Double) o1.get(primaryKey);
           	Double o2p = (Double) o2.get(primaryKey);

           	if (o1p.doubleValue() == o2p.doubleValue()) {
           		return ((String) o1.get(secondaryKey)).
                   		compareTo(((String) o2.get(secondaryKey)));
           	}

               return o2p.compareTo(o1p);
           }
       });
	}


    public static List flattenList(List inList) {
    	List newList = new LinkedList();
    	for (Object i : inList) {
            // If it's not a list, just add it to the return list.
            if (!(i instanceof List)) {
                    newList.add(i);
            } else {
                    // It's a list, so add each item to the return list.
                    newList.addAll(flattenList((List)i));
            }
    	}

        return newList;
    }

}
