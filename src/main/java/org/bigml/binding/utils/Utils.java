package org.bigml.binding.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;



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
  
	
}
