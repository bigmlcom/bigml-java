package org.bigml.binding.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Utils {

  /**
   *
   */
  public static DefaultHttpClient httpClient() throws Exception {
	  SchemeRegistry schemeRegistry = new SchemeRegistry();
 	  schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
	  schemeRegistry.register(new Scheme("https", 443, new MockSSLSocketFactory()));
	  ClientConnectionManager cm = new SingleClientConnManager(schemeRegistry);
	  return new DefaultHttpClient(cm);
  }


  /**
   * Converts a InputStream to a String.
   *
   */
  public static String inputStreamAsString(InputStream stream)
          throws IOException {

    BufferedReader br = new BufferedReader(new InputStreamReader(stream));
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
    if (path.indexOf(".")!=-1) {
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

    if (json==null) {
      return null;
    }

    if (path.indexOf(".")==-1) {
      return json;
    }

    path = path.substring(path.indexOf(".")+1, path.length());
    if (path.length()>0) {
      return getJSONObject(json, path);
    }

    return null;
  }


  /**
   * Inverts a dictionary changing keys per values
   *
   * @param json	the json object to invert
   * @return
   */
  public static JSONObject invertDictionary(JSONObject json) {
	  JSONObject invertedObject = new JSONObject();
	  Iterator iter = json.keySet().iterator();
      while (iter.hasNext()) {
        String key = (String) iter.next();
        String fieldName = (String) Utils.getJSONObject(json, key+".name");
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