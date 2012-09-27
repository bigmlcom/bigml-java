package org.bigml.binding.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

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
	
}
