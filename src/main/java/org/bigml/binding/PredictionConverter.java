package org.bigml.binding;

import java.util.Locale;

/**
 * Any prediction resource must implement this interface to allow
 * conversion of prediction values from string to its correct data type
 *
 * User: xalperte
 * Date: 3/2/15
 * Time: 3:46 PM
 */
public interface PredictionConverter {

    /**
     * Given a prediction string, returns its value in the required type
     *
     * @param valueAsString the value to be converted to the Objective data type
     * @param locale the current locale to use to parse values
     * @return the prediction in its data type
     */
    Object toPrediction(String valueAsString, Locale locale);

}
