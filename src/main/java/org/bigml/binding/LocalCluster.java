package org.bigml.binding;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.bigml.binding.localmodel.Predicate;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A local Predictive Cluster.
 *
 * This module defines a Cluster to make predictions (centroids) locally or
 * embedded into your application without needing to send requests to
 * BigML.io.
 *
 * This module cannot only save you a few credits, but also enormously
 * reduce the latency for each prediction and let you use your models
 * offline.
 *
 * Example usage (assuming that you have previously set up the BIGML_USERNAME
 * and BIGML_API_KEY environment variables and that you own the model/id below):
 *
 * import org.bigml.binding.LocalCluster;
 *
 * JSONObject clusterData = BigMLClient.getInstance().
 *                                getCluster("cluster/5026965515526876630001b2");
 * LocalCluster cluster = LocalCluster(clusterData)
 *
 * JSONObject predictors = JSONValue.parse("{\"petal length\": 3,
 *                                \"petal width\": 1,
 *                                \"sepal length\": 1,
 *                                \"sepal width\": 0.5}");
 *
 * cluster.predict(predictors)
 */
public class LocalCluster extends ModelFields {

    /**
     * Logging
     */
    static Logger logger = LoggerFactory.getLogger(LocalCluster.class.getName());

    protected static final String[] OPTIONAL_FIELDS = { "categorical", "text" };

    protected static final String[] CSV_STATISTICS = {"minimum", "mean", "median", "maximum", "standard_deviation",
            "sum", "sum_squares", "variance" };


    private String clusterId;

    private JSONObject cluster;

    private JSONArray clusters;
    private List<LocalCentroid> centroids;

    private JSONObject scales;
    private JSONObject termForms;
    private Map<String, Map<String, Integer>> tagClouds;
    private JSONObject termAnalysis;

    private JSONArray summaryFields;

    public LocalCluster(JSONObject clusterData) throws Exception {
        super((JSONObject) Utils.getJSONObject(clusterData, "clusters.fields"));

        if (clusterData.get("resource") == null) {
            throw new Exception(
                    "Cannot create the Cluster instance. Could not find the 'cluster' key in the resource");
        }

        if (!BigMLClient.getInstance().clusterIsReady(clusterData)) {
            throw new Exception("The cluster isn't finished yet");
        }

        clusterId = (String) clusterData.get("resource");

        cluster = clusterData;

        if( cluster.containsKey("clusters") ) {
            clusters = (JSONArray) Utils.getJSONObject(cluster, "clusters.clusters");
            centroids = new JSONArray();

            Iterator<JSONObject> clustersIterator = clusters.iterator();
            while(clustersIterator.hasNext()) {
                JSONObject childCluster = clustersIterator.next();
                centroids.add(new LocalCentroid(childCluster));
            }

            scales = new JSONObject();
            scales.putAll((JSONObject) cluster.get("scales"));

            termForms = new JSONObject();
            tagClouds = new HashMap<String, Map<String, Integer>>();
            termAnalysis = new JSONObject();

            summaryFields = (JSONArray) Utils.getJSONObject(cluster,"summary_fields");

            for (Object summaryField : summaryFields) {
                fields.remove(summaryField);
            }

            for (Object fieldId : fields.keySet()) {
                JSONObject field = (JSONObject) fields.get(fieldId);
                if( "text".equals(field.get("optype")) ) {
                    termForms.put(fieldId, Utils.getJSONObject(field, "summary.term_forms", new JSONObject()));

                    // Convert the Map of JSONArrays to a Map of Maps.
                    Map<String, Integer> tagsCountMap = new HashMap<String, Integer>();
                    JSONArray tags = (JSONArray) Utils.getJSONObject(field, "summary.tag_cloud", new JSONArray());
                    for (Object tag : tags) {
                        JSONArray tagArr = (JSONArray) tag;
                        // [0] -> term , [1] -> Number of occurrences of the term
                        tagsCountMap.put(tagArr.get(0).toString(), ((Number) tagArr.get(1)).intValue());
                    }
                    tagClouds.put(fieldId.toString(), tagsCountMap);


                    termAnalysis.put(fieldId, Utils.getJSONObject(field, "term_analysis", new JSONObject()));
                }
            }

            Set<String> fieldsId = scales.keySet();
            for (String fieldId : fieldsId) {
                if( !fields.containsKey(fieldId) ) {
                    throw new Exception("Some fields are missing" +
                            " to generate a local cluster." +
                            " Please, provide a cluster with" +
                            " the complete list of fields.");
                }
            }

        } else {
            throw new Exception(String.format("Cannot create the Cluster instance. Could not" +
                    " find the 'clusters' key in the resource:\n\n%s",
                            cluster));
        }

    }

    /**
     * Returns the id of the nearest centroid
     *
     * Returns the nearest centroid as a JSONObject with the following properties:
     *
     *   centroid_id
     *   centroid_name
     *   distance
     *
     */
    public JSONObject calculateCentroid(JSONObject inputData, Boolean byName) {
        if(byName == null) {
            byName = true;
        }

        // Checks and cleans input_data leaving the fields used in the model
        inputData = filterInputData(inputData, byName);

        // Checks that all numeric fields are present in input data
        for (Object fieldId : fields.keySet()) {
            JSONObject field = (JSONObject) fields.get(fieldId);
            if( Arrays.binarySearch(OPTIONAL_FIELDS, field.get("optype")) == -1 &&
                !inputData.containsKey(fieldId) ) {
                 throw new IllegalArgumentException("Failed to predict a centroid. Input" +
                                     " data must contain values for all " +
                                     "numeric fields to find a centroid.");
            }
        }

        // Strips affixes for numeric values and casts to the final field type
        Utils.cast(inputData, fields);
        inputData = new JSONObject(inputData);

        Map<String, Object> uniqueTerms = getUniqueTerms(inputData);

        JSONObject nearest = new JSONObject();
        nearest.put("centroid_id", null);
        nearest.put("centroid_name", null);
        nearest.put("distance", Double.POSITIVE_INFINITY);

        for (LocalCentroid centroid : centroids) {
            Double distance2 = centroid.distance2(inputData,
                    uniqueTerms, scales, (Double) nearest.get("distance"));

            if( distance2 != null) {
                nearest.put("centroid_id", centroid.getCentroidId());
                nearest.put("centroid_name", centroid.getName());
                nearest.put("distance", distance2);
            }
        }

        nearest.put("distance", Math.sqrt((Double) nearest.get("distance")));
        return nearest;
    }

    /**
     * Parses the input data to find the list of unique terms in the
     * tag cloud
     */
    protected Map<String, Object> getUniqueTerms(Map<String, Object> inputData) {
        Map<String, Object> uniqueTerms = new HashMap<String, Object>();
        for (Object fieldId : termForms.keySet()) {

            if( inputData.containsKey(fieldId.toString()) ) {
                Object inputDataField = inputData.get(fieldId.toString());
                inputDataField = (inputDataField != null ? inputDataField : "");

                if( inputDataField instanceof String ) {
                    boolean caseSensitive = (Boolean) Utils.getJSONObject(termAnalysis,
                            fieldId + ".case_sensitive", Boolean.TRUE);
                    String tokenMode = (String) Utils.getJSONObject(termAnalysis,
                            fieldId + ".token_mode", "all");

                    List<String> terms = new ArrayList<String>();
                    if( !Predicate.TM_FULL_TERM.equals(tokenMode) ) {
                        terms = parseTerms(inputDataField.toString(), caseSensitive);
                    }

                    if( !Predicate.TM_TOKENS.equals(tokenMode) ) {
                        terms.add((caseSensitive ? inputDataField.toString() :
                                ((String) inputDataField).toLowerCase()));
                    }

                    uniqueTerms.put(fieldId.toString(), getUniqueTerms(terms,
                            (JSONObject) termForms.get(fieldId),
                            tagClouds.get(fieldId.toString())) );
                } else {
                    uniqueTerms.put(fieldId.toString(), inputDataField);
                }

                inputData.remove(fieldId.toString());
            }
        }

        return uniqueTerms;
    }

    /**
     * Extracts the unique terms that occur in one of the alternative forms in
     * term_forms or in the tag cloud.
     */
    protected List<String> getUniqueTerms(List<String> terms, JSONObject termForms, Map<String, Integer> tagClouds) {

        Map<String, String> extendForms = new HashMap<String, String>();

        for (Object term : termForms.keySet()) {
            JSONArray forms = (JSONArray) termForms.get(term);
            for (Object form : forms) {
                extendForms.put(form.toString(), term.toString());
            }
            extendForms.put(term.toString(), term.toString());
        }

        Set<String> termsSet = new TreeSet<String>();
        for (Object term : terms) {
            if( tagClouds.containsKey(term.toString()) ) {
                termsSet.add(term.toString());
            } else if( extendForms.containsKey(term.toString()) ) {
                termsSet.add(extendForms.get(term.toString()));
            }
        }

        return new ArrayList<String>(termsSet);
    }


    /**
     * Returns the list of parsed terms
     */
    protected List<String> parseTerms(String text, Boolean caseSensitive) {
        if( caseSensitive == null ) {
            caseSensitive = Boolean.TRUE;
        }

        List<String> terms = new ArrayList<String>();

        String expression = "(\\b|_)([^\b_\\s]+?)(\\b|_)";

        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(text);
        // check all occurrence
        while (matcher.find()) {
            String term = matcher.group();
            terms.add( (caseSensitive ? term : term.toLowerCase()) );
        }

        return terms;
    }


    /**
     * Statistic distance information from the given centroid
     * to the rest of centroids in the cluster
     */
    protected Map<String, Double> getCentroidDistances(LocalCentroid centroid) {
        Map<String, Double> intercentroidDistances = new HashMap<String, Double>();

        Map<String, Object> uniqueTerms = getUniqueTerms(centroid.getCenter());
        List<Double> distances = new ArrayList<Double>();
        for (LocalCentroid localCentroid : centroids) {
            if( !localCentroid.getCentroidId().equals(centroid.getCentroidId()) ) {
                distances.add(Math.sqrt(
                        centroid.distance2(centroid.getCenter(),
                                uniqueTerms, scales, null)));
            }
        }

        intercentroidDistances.put("Minimum", Collections.min(distances));
        intercentroidDistances.put("Mean", Utils.meanOfValues(distances));
        intercentroidDistances.put("Maximum", Collections.max(distances));

        return intercentroidDistances;
    }


    /**
     * Returns features defining the centroid according to the list
     * of common field ids that define the centroids.
     */
    protected String[] getCentroidFeatures(LocalCentroid centroid, List<String> fieldIds) {
        List features = new ArrayList();
        for (String fieldId : fieldIds) {
            features.add(centroid.getCenter().get(fieldId));
        }

        return (String[]) features.toArray(new String[fieldIds.size()]);
    }

    /**
     * Returns training data distribution
     */
    protected JSONArray getDataDistribution() {

        JSONArray distribution = new JSONArray();

        for (LocalCentroid centroid : centroids) {
            JSONArray centroidData = new JSONArray();
            centroidData.add(centroid.getName());
            centroidData.add(centroid.getCount());

            distribution.add(centroidData);
        }

        Collections.sort(distribution, new Comparator<JSONArray>() {
            @Override
            public int compare(JSONArray o1, JSONArray o2) {
                return o1.get(0).toString().compareTo(o2.toString());
            }
        });

        return distribution;
    }

    /**
     * Clusters statistic information in CSV format
     */
    public void exportStatistics(String outputFilePath) throws IOException {

        Writer statisticsFile = null;
        try {
            statisticsFile = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outputFilePath), "UTF-8"));
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Cannot find %s directory.", outputFilePath));
        }


        List<String> headers = new ArrayList<String>();
        headers.add("centroid_name");
        headers.addAll(fieldsName);
//        headers.add("centroid_features");
        headers.add("Instances");

        List<String> csvStatistics = new ArrayList<String>(Arrays.asList(CSV_STATISTICS));

        boolean intercentroids = false;
        boolean headerComplete = false;

        List rows = new ArrayList();
        for (LocalCentroid centroid : centroids) {
            List<Object> values = new ArrayList<Object>(headers.size());

            values.add(centroid.getName());
            values.addAll(Arrays.asList(
                    getCentroidFeatures(centroid, fieldsId)));
            values.add(centroid.getCount());

            if( centroids.size()>  1 ) {
                Map<String, Double> distanceMeasures = getCentroidDistances(centroid);
                for (String measureName : distanceMeasures.keySet()) {
                    if( !intercentroids ) {
                        headers.add(String.format("Intercentroids %s", measureName.toLowerCase()));
                    }
                    values.add(distanceMeasures.get(measureName));
                }
                intercentroids = true;

                JSONObject distanceInfo = (JSONObject) centroid.getCenter().get("distance");
                for (Object measureName : distanceInfo.keySet()) {
                    Object result = distanceInfo.get(measureName);

                    if( csvStatistics.contains( measureName.toString() ) ) {
                        if( !headerComplete ) {
                            headers.add(String.format("Data %s",
                                    measureName.toString().toLowerCase().replace('_',' ')));
                        }
                        values.add(result);
                    }

                }
                headerComplete = true;
            }

            rows.add(values);
        }

        final CSVPrinter printer = CSVFormat.DEFAULT.withHeader((String[])
                headers.toArray(new String[headers.size()])).print(statisticsFile);

        try {
            printer.printRecords(rows);
        } catch (Exception e) {
            throw new IOException("Error generating the CSV !!!");
        }

        try {
            statisticsFile.flush();
            statisticsFile.close();
        } catch (IOException e) {
            throw new IOException("Error while flushing/closing fileWriter !!!");
        }

    }

    /**
     * Prints a summary of the cluster info
     */
    public StringBuilder summarize() {
        StringBuilder summary = new StringBuilder();

        summary.append(String.format("Cluster of %s centroids\n\n", centroids.size())).
                append("Data distribution:\n").
                append(Utils.printDistribution(getDataDistribution())).
                append("\n\n");

        List<LocalCentroid> sortedCentroids = new ArrayList<LocalCentroid>(centroids);
        Collections.sort(sortedCentroids, new Comparator<LocalCentroid>() {
            @Override
            public int compare(LocalCentroid o1, LocalCentroid o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        summary.append("Centroids features:\n");
        for (LocalCentroid sortedCentroid : sortedCentroids) {
            summary.append(String.format("\n%s: ", sortedCentroid.getName()));
            String separator = "";

            for (Object fieldId : sortedCentroid.getCenter().entrySet()) {
                Object value = sortedCentroid.getCenter().get(fieldId);

                if( value instanceof String ) {
                    String.format("\"%s\"", value);
                }

                summary.append(String.format("%s%s: %s",
                        separator,
                        fieldsNameById.get(fieldId.toString()),
                        value));

                separator = ", ";
            }
        }

        summary.append("\n\n")
                .append("Data distance statistics:\n\n");

        for (LocalCentroid sortedCentroid : sortedCentroids) {
            summary.append(sortedCentroid.printStatistics());
        }

        if( centroids.size() > 1 ) {
            summary.append("Intercentroids distance:\n\n");
            for (LocalCentroid sortedCentroid : sortedCentroids) {
                summary.append(String.format("To centroid: %s\n", sortedCentroid.getName()));

                Map<String, Double> centoridMeasures = getCentroidDistances(sortedCentroid);
                for (String measure : centoridMeasures.keySet()) {
                    Double result = centoridMeasures.get(measure);

                    summary.append(String.format("\t%s: %s\n",
                            measure, result));
                }
                summary.append("\n\n");
            }
        }

        return summary;
    }

    public String getClusterId() {
        return clusterId;
    }

}
