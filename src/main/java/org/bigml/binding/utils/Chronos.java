package org.bigml.binding.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Chronos {

    /**
     * Logging
     */
    static Logger logger = LoggerFactory.getLogger(
    	Chronos.class.getName());


    static HashMap<String, String> BASIC_FORMAT_PATTERNS = new HashMap<String, String>();
    static {
    	BASIC_FORMAT_PATTERNS.put("basic-date-time", "yyyyMMdd'T'HHmmss.SSSXX");
    	BASIC_FORMAT_PATTERNS.put("basic-date-time-no-ms", "yyyyMMdd'T'HHmmssXX");
    	BASIC_FORMAT_PATTERNS.put("basic-ordinal-date-time", "yyyyDDD'T'HHmmss.SSSXX");
    	BASIC_FORMAT_PATTERNS.put("basic-ordinal-date-time-no-ms", "yyyyDDD'T'HHmmssXX");
    	BASIC_FORMAT_PATTERNS.put("basic-time", "HHmmss.SSSXX");
    	BASIC_FORMAT_PATTERNS.put("basic-time-no-ms", "HHmmssXX");
    	BASIC_FORMAT_PATTERNS.put("basic-t-time", "'T'HHmmss.SSSXX");
    	BASIC_FORMAT_PATTERNS.put("basic-t-time-no-ms", "'T'HHmmssXX");
    	BASIC_FORMAT_PATTERNS.put("basic-week-date", "xxxx'W'wwe");
    	BASIC_FORMAT_PATTERNS.put("iso-week-date", "xxxx-'W'ww-ez");
    	BASIC_FORMAT_PATTERNS.put("basic-week-date-time", "xxxx'W'wwe'T'Hmmss.SSSXX");
    	BASIC_FORMAT_PATTERNS.put("basic-week-date-time-no-ms", "xxxx'W'wwe'T'HmmssXXX");
    	BASIC_FORMAT_PATTERNS.put("date", "yyyy-MM-dd");
    	BASIC_FORMAT_PATTERNS.put("date-hour", "yyyy-MM-dd'T'H");
    	BASIC_FORMAT_PATTERNS.put("date-hour-minute", "yyyy-MM-dd'T'H:mm");
    	BASIC_FORMAT_PATTERNS.put("date-hour-minute-second", "yyyy-MM-dd'T'H:mm:ss");
    	BASIC_FORMAT_PATTERNS.put("date-hour-minute-second-fraction", "yyyy-MM-dd'T'H:mm:ss.SSS");
    	BASIC_FORMAT_PATTERNS.put("date-hour-minute-second-ms", "yyyy-MM-dd'T'HH:mm:ss.SSS");
    	BASIC_FORMAT_PATTERNS.put("date-time", "yyyy-MM-dd'T'H:mm:ss.SSSXXX");
    	BASIC_FORMAT_PATTERNS.put("date-time-no-ms", "yyyy-MM-dd'T'H:mm:ssXXX");
    	BASIC_FORMAT_PATTERNS.put("hour-minute", "H:mm");
    	BASIC_FORMAT_PATTERNS.put("hour-minute-second", "H:mm:ss");
    	BASIC_FORMAT_PATTERNS.put("hour-minute-second-fraction", "H:mm:ss.SSS");
    	BASIC_FORMAT_PATTERNS.put("hour-minute-second-ms", "H:mm:ss.SSS");
    	BASIC_FORMAT_PATTERNS.put("ordinal-date-time", "yyyy-DDD'T'H:mm:ss.SSSXXX");
    	BASIC_FORMAT_PATTERNS.put("ordinal-date-time-no-ms", "yyyy-DDD'T'H:mm:ssXXX");
    	BASIC_FORMAT_PATTERNS.put("time", "H:mm:ss.SSSXXX");
    	BASIC_FORMAT_PATTERNS.put("time-no-ms", "H:mm:ssXXX");
    	BASIC_FORMAT_PATTERNS.put("t-time", "'T'H:mm:ss.SSSXXX");
    	BASIC_FORMAT_PATTERNS.put("t-time-no-ms", "'T'H:mm:ssXXX");
    	BASIC_FORMAT_PATTERNS.put("week-date", "xxxx-'W'ww-e");
    	BASIC_FORMAT_PATTERNS.put("week-date-time", "xxxx-'W'ww-e'T'H:mm:ss.SSSXXX");
    	BASIC_FORMAT_PATTERNS.put("week-date-time-no-ms", "xxxx-'W'ww-e'T'H:mm:ssXXX");
    	BASIC_FORMAT_PATTERNS.put("weekyear-week", "xxxx-'W'ww");
    	BASIC_FORMAT_PATTERNS.put("weekyear-week-day", "xxxx-'W'ww-e");
    	BASIC_FORMAT_PATTERNS.put("year-month", "yyyy-MM");
    	BASIC_FORMAT_PATTERNS.put("year-month-day", "yyyy-MM-dd");
    	BASIC_FORMAT_PATTERNS.put("rfc822", "EEE, dd MMM yyyy HH:mm:ss Z");
    	BASIC_FORMAT_PATTERNS.put("mysql", "yyy-MM-dd H:mm:ss");
    }

    static HashMap<String, String> CUSTOM_FORMAT_PATTERNS = new HashMap<String, String>();
    static {
    	CUSTOM_FORMAT_PATTERNS.put("no-t-date-hour-minute", "y-M-d H:m");
    	CUSTOM_FORMAT_PATTERNS.put("twitter-time", "E MMM d H:m:s Z y");
    	CUSTOM_FORMAT_PATTERNS.put("twitter-time-alt", "y-M-d H:m:s Z");
    	CUSTOM_FORMAT_PATTERNS.put("twitter-time-alt-2", "y-M-d H:m Z");
    	CUSTOM_FORMAT_PATTERNS.put("twitter-time-alt-3", "E MMM d H:m Z y");
    	CUSTOM_FORMAT_PATTERNS.put("us-date", "M/d/y");
    	CUSTOM_FORMAT_PATTERNS.put("us-date-minute", "M/d/y H:m");
    	CUSTOM_FORMAT_PATTERNS.put("us-date-second", "M/d/y H:m:s");
    	CUSTOM_FORMAT_PATTERNS.put("us-date-millisecond", "M/d/y H:m:s.SSS");
    	CUSTOM_FORMAT_PATTERNS.put("us-date-clock-minute", "M/d/y h:m a");
    	CUSTOM_FORMAT_PATTERNS.put("us-date-clock-second", "M/d/y h:m:s a");
    	CUSTOM_FORMAT_PATTERNS.put("us-date-clock-minute-nospace", "M/d/y h:ma");
    	CUSTOM_FORMAT_PATTERNS.put("us-date-clock-second-nospace", "M/d/y h:m:sa");
    	CUSTOM_FORMAT_PATTERNS.put("eu-date", "d/M/y");
    	CUSTOM_FORMAT_PATTERNS.put("eu-date-minute", "d/M/y H:m");
    	CUSTOM_FORMAT_PATTERNS.put("eu-date-second", "d/M/y H:m:s");
    	CUSTOM_FORMAT_PATTERNS.put("eu-date-millisecond", "d/M/y H:m:s.SSS");
    	CUSTOM_FORMAT_PATTERNS.put("eu-date-clock-minute", "d/M/y h:m a");
    	CUSTOM_FORMAT_PATTERNS.put("eu-date-clock-second", "d/M/y h:m:s a");
    	CUSTOM_FORMAT_PATTERNS.put("eu-date-clock-minute-nospace", "d/M/y h:ma");
    	CUSTOM_FORMAT_PATTERNS.put("eu-date-clock-second-nospace", "d/M/y h:m:sa");
    	CUSTOM_FORMAT_PATTERNS.put("date-with-solidus", "yyyy/MM/dd");
    	CUSTOM_FORMAT_PATTERNS.put("date-hour-with-solidus", "yyyy/MM/dd'T'H");
    	CUSTOM_FORMAT_PATTERNS.put("date-hour-minute-with-solidus", "yyyy/MM/dd'T'H:mm");
    	CUSTOM_FORMAT_PATTERNS.put("date-hour-minute-second-with-solidus", "yyyy/MM/dd'T'H:mm:ss");
    	CUSTOM_FORMAT_PATTERNS.put("date-hour-minute-second-fraction-with-solidus", "yyyy/MM/dd'T'H:mm:ss.SSS");
    	CUSTOM_FORMAT_PATTERNS.put("date-hour-minute-second-ms-with-solidus", "yyyy/MM/dd'T'HH:mm:ss.SSS");
    	CUSTOM_FORMAT_PATTERNS.put("date-time-with-solidus", "yyyy/MM/dd'T'H:mm:ss.SSSXXX");
    	CUSTOM_FORMAT_PATTERNS.put("date-time-no-ms-with-solidus", "yyyy/MM/dd'T'H:mm:ssXXX");
    	CUSTOM_FORMAT_PATTERNS.put("us-sdate", "M-d-y");
    	CUSTOM_FORMAT_PATTERNS.put("us-sdate-minute", "M-d-y H:m");
    	CUSTOM_FORMAT_PATTERNS.put("us-sdate-second", "M-d-y H:m:s");
    	CUSTOM_FORMAT_PATTERNS.put("us-sdate-millisecond", "M-d-y H:m:s.SSS");
    	CUSTOM_FORMAT_PATTERNS.put("us-sdate-clock-minute", "M-d-y h:m a");
    	CUSTOM_FORMAT_PATTERNS.put("us-sdate-clock-second", "M-d-y h:m:s a");
    	CUSTOM_FORMAT_PATTERNS.put("us-sdate-clock-minute-nospace", "M-d-y h:ma");
    	CUSTOM_FORMAT_PATTERNS.put("us-sdate-clock-second-nospace", "M-d-y h:m:sa");
    	CUSTOM_FORMAT_PATTERNS.put("eu-sdate", "d-M-y");
    	CUSTOM_FORMAT_PATTERNS.put("eu-sdate-minute", "d-M-y H:m");
    	CUSTOM_FORMAT_PATTERNS.put("eu-sdate-second", "d-M-y H:m:s");
    	CUSTOM_FORMAT_PATTERNS.put("eu-sdate-millisecond", "d-M-y H:m:s.SSS");
    	CUSTOM_FORMAT_PATTERNS.put("eu-sdate-clock-minute", "d-M-y h:m a");
    	CUSTOM_FORMAT_PATTERNS.put("eu-sdate-clock-second", "d-M-y h:m:s a");
    	CUSTOM_FORMAT_PATTERNS.put("eu-sdate-clock-minute-nospace", "d-M-y h:ma");
    	CUSTOM_FORMAT_PATTERNS.put("eu-sdate-clock-second-nospace", "d-M-y h:m:sa");
    	CUSTOM_FORMAT_PATTERNS.put("eu-ddate", "d.M.y");
    	CUSTOM_FORMAT_PATTERNS.put("eu-ddate-minute", "d.M.y H:m");
    	CUSTOM_FORMAT_PATTERNS.put("eu-ddate-second", "d.M.y H:m:");
    	CUSTOM_FORMAT_PATTERNS.put("eu-ddate-millisecond", "d.M.y H:m:s.SSS");
    	CUSTOM_FORMAT_PATTERNS.put("eu-ddate-clock-minute", "d.M.y h:m a");
    	CUSTOM_FORMAT_PATTERNS.put("eu-ddate-clock-second", "d.M.y h:m:s a");
    	CUSTOM_FORMAT_PATTERNS.put("eu-ddate-clock-minute-nospace", "d.M.y h:ma");
    	CUSTOM_FORMAT_PATTERNS.put("eu-ddate-clock-second-nospace", "d.M.y h:m:sa");
    	CUSTOM_FORMAT_PATTERNS.put("clock-minute", "h:m a");
    	CUSTOM_FORMAT_PATTERNS.put("clock-second", "h:m:s a");
    	CUSTOM_FORMAT_PATTERNS.put("clock-minute-nospace", "h:ma");
    	CUSTOM_FORMAT_PATTERNS.put("clock-second-nospace", "h:m:sa");
    }
    
    /**
     * Creates a Date object from a string representing a date and a
     * format name
     * 
     * @param date
     * 			the date to parse as a string
     * @param formats
     * 			list of date formats to check during parsing
     * 
     * @return the parsed date
     * @throws Exception a generic exception
     */
    public static Date parse(String date, JSONArray formats) throws Exception {
    	for (Object formatName : formats) {
    		String format = BASIC_FORMAT_PATTERNS.get((String) formatName);
    		if (format == null) {
    			format = CUSTOM_FORMAT_PATTERNS.get((String) formatName);
    		}
    		if (format == null) {
    			throw new Exception("Timeformat specified is not valid");
    		}

    		try {
    			SimpleDateFormat sdf = new SimpleDateFormat(format);
    			return sdf.parse(date);
    		} catch (ParseException e) {
    			continue;
    		}
    	}

    	return null;
    }

}
