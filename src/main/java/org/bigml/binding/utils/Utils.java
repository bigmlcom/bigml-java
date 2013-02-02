package org.bigml.binding.utils;

import java.io.BufferedReader;
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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Utils {

	
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
      return (JSONArray) json.get(field);
    }
    if (json.get(field) instanceof String) {
      return (String) json.get(field);
    }
    if (json.get(field) instanceof Long) {
      return (Long) json.get(field);
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
   * Inverts a dictionary changin keys per values
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
        invertedObject.put(fieldName, json.get(key));
      }
      return invertedObject;
  }
  
  private static final Pattern NONLATIN = Pattern.compile("[^\\w]");  
  private static final Pattern WHITESPACE = Pattern.compile("[\\s]");  

  public static String slugify(String input) {  
    String nowhitespace = WHITESPACE.matcher(input).replaceAll("_");  
    String normalized = Normalizer.normalize(nowhitespace, Form.NFD);  
    String slug = NONLATIN.matcher(normalized).replaceAll("");  
    return slug.toLowerCase(Locale.ENGLISH);  
  } 

}
