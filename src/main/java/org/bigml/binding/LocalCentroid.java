package org.bigml.binding;

import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Centroid structure for the BigML local Cluster
 *
 * This module defines an auxiliary Centroid predicate structure that is used
 * in the cluster.
 */
public class LocalCentroid implements Serializable {

    private static final long serialVersionUID = 1L;

    protected static final String[] STATISTIC_MEASURES = {
            "Minimum", "Mean", "Median", "Maximum", "Standard deviation" };

    private JSONObject center;
    private int count;
    private String centroidId;
    private String name;
    private JSONObject distance;


    public LocalCentroid(JSONObject centroidData) {
        center = (JSONObject) Utils.getJSONObject(centroidData, "center", new JSONObject());
        count = ((Number) Utils.getJSONObject(centroidData, "count", new Integer(0))).intValue();
        centroidId = (String) Utils.getJSONObject(centroidData, "id", null);
        name = (String) Utils.getJSONObject(centroidData, "name", null);
        distance = (JSONObject) Utils.getJSONObject(centroidData, "distance", new JSONObject());
    }


    public String getCentroidId() {
        return centroidId;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public JSONObject getCenter() {
        return center;
    }

    /**
     * Squared Distance from the given input data to the centroid
     */
    public Double distance2(Map<String, Object> inputData, Map<String, Object> termSets,
                               JSONObject scales, Double stopDistance2) {

        double distance2 = 0.0;
        for (Object centerKey : center.keySet()) {
            String fieldId = (String) centerKey;
            Object value = center.get(fieldId);

            if (value instanceof JSONArray) {
                // We are talking about a TEXT field (list of terms)
                List<String> terms = termSets.containsKey(fieldId) ?
                        (ArrayList<String>) termSets.get(fieldId.toString()) : new ArrayList<String>();
                distance2 += cosineDistance2(terms, (JSONArray) value,
                        ((Number) scales.get(fieldId)).doubleValue());
            } else if (value instanceof String) {
                if (!inputData.containsKey(fieldId) || !value.equals(inputData.get(fieldId))) {
                    distance2 += 1 * Math.pow(((Number) scales.get(fieldId)).doubleValue(), 2);
                }
            } else {
                // Delta Value = (InputData Value - Centroid Value) * Scale of the Field
                // Delta Value ^ 2
                distance2 += Math.pow((((Number) inputData.get(fieldId)).doubleValue() - ((Number) value).doubleValue())
                        * ((Number) scales.get(fieldId)).doubleValue(), 2);
            }

            if (stopDistance2 != null && distance2 >= stopDistance2) {
                return null;
            }
        }

        return distance2;
    }


    /**
     * Print the statistics for the training data clustered around the centroid
     */
    public StringBuilder printStatistics() {
        StringBuilder text = new StringBuilder(String.format("%s:\n", name));

        for (String measureTitle : STATISTIC_MEASURES) {
            String measure = measureTitle.toLowerCase().replace(' ', '_');
            text.append(String.format("\t%s: %s\n", measureTitle,
                    distance.get(measure).toString()));
        }

        text.append("\n");
        return text;
    }


    /**
     * Returns the distance defined by cosine similarity
     */
    protected double cosineDistance2(List<String> terms, JSONArray centroidTerms,
                                     double scale) {
        // Centroid values for the field can be an empty list.
        // Then the distance for an empty input is 1
        // (before applying the scale factor).
        if( terms == null) {
            terms = new ArrayList<String>();
        }

        if( centroidTerms == null) {
            centroidTerms = new JSONArray();
        }


        if( centroidTerms.isEmpty() && terms.isEmpty() ) {
            return 0;
        }

        if( centroidTerms.isEmpty() || terms.isEmpty() ) {
            return Math.pow(scale, 2);
        }

        int inputCount = 0;
        Iterator centroidTermsItr = centroidTerms.iterator();
        while(centroidTermsItr.hasNext()) {
            String centroidTerm = (String) centroidTermsItr.next();
            if( terms.contains(centroidTerm) ) {
                inputCount++;
            }
        }

        double cosineSimilarity = inputCount /
                Math.sqrt(terms.size() * centroidTerms.size());

        double similarityDistance = scale * (1 - cosineSimilarity);

        return Math.pow(similarityDistance, 2);
    }
}
