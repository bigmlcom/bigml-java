package org.bigml.binding;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.text.StringEscapeUtils;
import org.bigml.binding.resources.AbstractResource;
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
 * This module defines a Cluster to make predictions (centroids) locally 
 * or embedded into your application without needing to send requests to
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
 * // API client
 * BigMLClient api = new BigMLClient();
 * 
 * JSONObject clusterData = api.
 * 		getCluster("cluster/5026965515526876630001b2");
 * 
 * LocalCluster cluster = new LocalCluster(clusterData);
 * cluster.centroid({"petal length": 3, "petal width": 1,
 *                   "sepal length": 1, "sepal width": 0.5});
 */
public class LocalCluster extends ModelFields {
	
	private static final long serialVersionUID = 1L;
	
	private static String CLUSTER_RE = "^cluster/[a-f,0-9]{24}$";
	
	protected static final String[] OPTIONAL_FIELDS = { 
    		"categorical", "text", "items", "datetime" };

    protected static final String[] CSV_STATISTICS = {
    		"minimum", "mean", "median", "maximum", "standard_deviation",
            "sum", "sum_squares", "variance" };
    
    protected static final String GLOBAL_CLUSTER_LABEL = "Global";
    
	
    /**
     * Logging
     */
    static Logger logger = LoggerFactory.getLogger(
    		LocalCluster.class.getName());

    
    private String clusterId;
    private List<LocalCentroid> centroids;
    private JSONArray clusters;
    private LocalCentroid clusterGlobal;
    private Double totalSS = null; 
    private Double withinSS = null; 
    private Double betweenSS = null; 
    private Double ratioSS = null; 
    private Long criticalValue = null;
    private String defaultNumericValue;
    private Integer k;
    private JSONArray summaryFields;
    private JSONObject scales;
    private JSONObject termForms = new JSONObject();
    private Map<String, Map<String, Integer>> tagClouds = 
    		new HashMap<String, Map<String, Integer>>();
    private JSONObject termAnalysis = new JSONObject();
    private JSONObject itemAnalysis = new JSONObject();
    private Map<String, Map<String, Integer>> items = 
    		new HashMap<String, Map<String, Integer>>();
    private JSONObject datasets;
    
    
    public LocalCluster(JSONObject cluster) throws Exception {
        this(null, cluster);
    }
    
    public LocalCluster(BigMLClient bigmlClient, JSONObject cluster) 
    		throws Exception {
        
    	super(bigmlClient, cluster);
    	cluster = this.model;
    	
        clusterId = (String) cluster.get("resource");
        
        if (cluster.containsKey("clusters")) {
        	JSONObject model = (JSONObject) cluster.get("clusters");
        	super.initialize(
        		(JSONObject) Utils.getJSONObject(cluster, "clusters.fields"),
        		null, null, null);
        	
            JSONObject status = (JSONObject) Utils.getJSONObject(cluster, "status");
            if( status != null &&
                    status.containsKey("code") &&
                    AbstractResource.FINISHED == ((Number) status.get("code")).intValue() ) {

            	defaultNumericValue = (String) cluster.get("default_numeric_value");
            	summaryFields = (JSONArray) cluster.get("summary_fields");
            	datasets = (JSONObject) cluster.get("cluster_datasets");
            	clusters = (JSONArray) Utils.getJSONObject(cluster, "clusters.clusters");
            	
            	centroids = new ArrayList<LocalCentroid>();
            	Iterator<JSONObject> clustersIterator = clusters.iterator();
                while (clustersIterator.hasNext()) {
                    JSONObject childCluster = clustersIterator.next();
                    centroids.add(new LocalCentroid(childCluster));
                }
                
                JSONObject clGlobal = (JSONObject) Utils.getJSONObject(cluster, "clusters.global");
                if (clGlobal != null) {
                	clusterGlobal = new LocalCentroid(clGlobal);
                    // "global" has no "name" and "count" then we set them
                	clusterGlobal.setName(GLOBAL_CLUSTER_LABEL);
                	JSONObject distance = (JSONObject) clusterGlobal.getDistance();
                	clusterGlobal.setCount(((Long) distance.get("population")).intValue());
                }
                
                totalSS = ((Number) Utils.getJSONObject(cluster, "clusters.total_ss")).doubleValue();
                withinSS = ((Number) Utils.getJSONObject(cluster, "clusters.within_ss")).doubleValue();
                if (this.withinSS == null) {
                	withinSS = 0.0;
	                for (LocalCentroid centroid: centroids) {
	                	JSONObject distance = (JSONObject) centroid.getDistance();
	                	withinSS += (Double) distance.get("sum_squares");
	                }
                }
                betweenSS = ((Number) Utils.getJSONObject(cluster, "clusters.between_ss")).doubleValue();
                ratioSS = ((Number) Utils.getJSONObject(cluster, "clusters.ratio_ss")).doubleValue();
                
                if (Utils.getJSONObject(cluster, "critical_value") != null) {
                	criticalValue = ((Number) Utils.getJSONObject(
                			cluster, "critical_value")).longValue();
                }

                k = ((Long) cluster.get("k")).intValue();
                scales = (JSONObject) cluster.get("scales");
                
                summaryFields = (JSONArray) Utils.getJSONObject(cluster, "summary_fields");
                for (Object summaryField : summaryFields) {
                    fields.remove(summaryField);
                }

                for (Object fieldId : fields.keySet()) {
                    JSONObject field = (JSONObject) fields.get(fieldId);
                    if ("text".equals(field.get("optype"))) {
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
                    
                    if ("items".equals(field.get("optype"))) {
                    	// Convert the Map of JSONArrays to a Map of Maps.
                        Map<String, Integer> itemsCountMap = new HashMap<String, Integer>();
                        JSONArray itemsArray = (JSONArray) Utils.getJSONObject(field, "summary.items", new JSONArray());
                        for (Object item : itemsArray) {
                        	JSONArray itemArr = (JSONArray) item;
                        	// [0] -> term , [1] -> Number of occurrences of the term
                        	itemsCountMap.put(itemArr.get(0).toString(), ((Number) itemArr.get(1)).intValue());
                        }
                        items.put(fieldId.toString(), itemsCountMap);
                    	
                    	itemAnalysis.put(fieldId, Utils.getJSONObject(field, "item_analysis", new JSONObject()));
                    }
                }
                
                Set<String> fieldsId = scales.keySet();
                for (String fieldId : fieldsId) {
                    if (!fields.containsKey(fieldId)) {
                        throw new Exception("Some fields are missing" +
                                " to generate a local cluster." +
                                " Please, provide a cluster with" +
                                " the complete list of fields.");
                    }
                }
            } else {
                throw new Exception("The cluster isn't finished yet");
            }
        } else {
            throw new Exception(String.format("Cannot create the Cluster instance. Could not" +
                            " find the 'clusters' key in the resource:\n\n%s",
                    cluster));
        }
    }
    
    /**
	 * Returns reg expre for model Id.
	 */
	public String getModelIdRe() {
		return CLUSTER_RE;
	}
    
	/**
	 * Returns bigml resource JSONObject.
	 */
    public JSONObject getBigMLModel(String modelId) {
		return (JSONObject) this.bigmlClient.getCluster(modelId);
	}
    
    /**
     * Prepares the fields to be able to compute the distance2
     */
    private JSONObject prepareForDistance(JSONObject inputData) {
    	// Checks and cleans input_data leaving the fields used in the model
        inputData = filterInputData(inputData);
        
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
        return inputData;
    }
    
    /**
     * Returns the nearest centroid as a JSONObject with the following properties:
     *
     * @param inputData
     * 				an object with field's id/value pairs representing the
	 *              instance you want to get the nearest centroid
     *   centroid_id
     *   centroid_name
     *   distance
     *
     * @return the nearest centroid for input data
     */
    public JSONObject centroid(JSONObject inputData) {
        inputData = prepareForDistance(inputData);

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
     * 
     * @param inputData
     * 				an object with field's id/value pairs representing the
	 *              instance you want to get the unique terms for
	 *  
	 * @return	a map with occurrences per unique term
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
                    if( !Utils.TM_FULL_TERM.equals(tokenMode) ) {
                        terms = parseTerms(inputDataField.toString(), caseSensitive);
                    }

                    if( !Utils.TM_TOKENS.equals(tokenMode) ) {
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

        //the same for items fields
        for (Object fieldId : itemAnalysis.keySet()) {
        	if( inputData.containsKey(fieldId.toString()) ) {
                Object inputDataField = inputData.get(fieldId.toString());
                inputDataField = (inputDataField != null ? inputDataField : "");
                
                if (inputDataField instanceof String) {
                	String separator = (String) Utils.getJSONObject(
                			itemAnalysis, fieldId + ".separator", " ");
                	String regexp = (String) Utils.getJSONObject(
                			itemAnalysis, fieldId + ".separator_regexp", null);
                	if (regexp == null) {
                		regexp = StringEscapeUtils.escapeJava(separator);
                	}
                    if ("$".equals(regexp)) {
                        regexp = "\\$";
                    }

                	List<String> terms = parseItems(
                			inputDataField.toString(), regexp);
                	uniqueTerms.put(fieldId.toString(), 
                			getUniqueTerms(terms,
                						   new JSONObject(),
                						   items.get(fieldId.toString())) );
                	
                } else {
                    uniqueTerms.put(fieldId.toString(), inputDataField);
                }
                
                inputData.remove(fieldId.toString());
        	}
        }

        return uniqueTerms;
    }
    
    /**
     * Extracts the unique terms that occur in one of the alternative 
     * forms in term forms or in the tag cloud.
     * 
     * @param terms			the list of terms to extract the info for
	 * @param termForms		the term forms
	 * @param tagClouds		list of tag cloud
	 * 
	 * @return a map with occurrences per unique term
     */
    protected List<String> getUniqueTerms(List<String> terms, 
    		JSONObject termForms, Map<String, Integer> tagClouds) {

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
     * Returns the list of parsed items
     */
    protected List<String> parseItems(String text, String regexp) {
    	List<String> terms = new ArrayList<String>();
    	if (text != null) {
            Pattern pattern = Pattern.compile(regexp, Pattern.UNICODE_CASE);
            terms = (List<String>) Arrays.asList(pattern.split(text));
    	}
    	
    	return terms;
    }
    
    /**
     * Statistic distance information from the given centroid
     * to the rest of centroids in the cluster
     * 
     * @param centroid	the local centroid
     * 
     * @return a map with statistic distance information
     */
    protected Map<String, Double> centroidDistances(LocalCentroid centroid) {
        Map<String, Double> intercentroidDistances = new HashMap<String, Double>();
        Map<String, Object> uniqueTerms = getUniqueTerms(centroid.getCenter());
        
        List<Double> distances = new ArrayList<Double>();
        for (LocalCentroid localCentroid : centroids) {
            if( !localCentroid.getCentroidId().equals(centroid.getCentroidId()) ) {
            	distances.add(Math.sqrt(
                		localCentroid.distance2(centroid.getCenter(),
                                uniqueTerms, scales, null)));
            }
        }

        intercentroidDistances.put("Minimum", Collections.min(distances));
        intercentroidDistances.put("Mean", Utils.meanOfValues(distances));
        intercentroidDistances.put("Maximum", Collections.max(distances));

        return intercentroidDistances;
    }
    
    /**
     * Computes the cluster square of the distance to an arbitrary
     * reference point for a list of points.
     * 
     * @param referencePoint (dict) The field values for the point used as
     *                           reference
     * @param listPoints Centroid The field values or a Centroid object
     *                                   which contains these values
     */
    private List<JSONObject> distances2ToPoint(JSONObject referencePoint,
    										   List<LocalCentroid> listPoints) {
    	
    	// Checks and cleans input_data leaving the fields used in the model
    	referencePoint = prepareForDistance(referencePoint);
    	
    	// mimic centroid structure to use it in distance computation
    	JSONObject pointInfo = new JSONObject();
    	pointInfo.put("center", referencePoint);
    	LocalCentroid reference = new LocalCentroid(pointInfo);
    	
    	List<JSONObject> distances = new ArrayList<JSONObject>();
    	for (Object pointObj: listPoints) {
    		String centroidId = null;
    		JSONObject point = null;
    		JSONObject cleanPoint = null;
    		if (pointObj instanceof LocalCentroid) {
    			LocalCentroid localCentroid = (LocalCentroid) pointObj;
    			centroidId = localCentroid.getCentroidId();
    			point = localCentroid.getCenter();
    			cleanPoint = prepareForDistance(point);
    		} else {
    			point = (JSONObject) pointObj;
    			cleanPoint = prepareForDistance(point);
    		}
    		
    		Map<String, Object> uniqueTerms = getUniqueTerms(cleanPoint);
    		
    		if ( cleanPoint != referencePoint) {
    			Map<String, Object> inputData = new HashMap<String, Object>();
    			JSONObject data = new JSONObject();
    			
    			Iterator it = cleanPoint.entrySet().iterator();
                while (it.hasNext()) {
                	Map.Entry fieldId = (Map.Entry) it.next();
                	
                	String field = (String) fieldId.getKey();
                	if( fieldsNameById.containsKey(fieldId.getKey()) ) {
                		field = fieldsNameById.get(fieldId.getKey());
                    }
                	
                	data.put(field, cleanPoint.get(fieldId.getKey()));
                	inputData.put((String) fieldId.getKey(), 
                				  cleanPoint.get(fieldId.getKey()));
                }
                
    			JSONObject result = new JSONObject();
    			result.put("data", data);
    			result.put("distance", 
    					reference.distance2(inputData, uniqueTerms, scales, null));
    			if (centroidId != null) {
    				result.put("centroid_id", centroidId);
    			}
    			distances.add(result);
    		}
    	}
    	return distances;
    }
    
    /**
     * Returns the list of data points that fall in one cluster.
     */
    private JSONArray pointsInCluster(String centroidId) 
    		throws Exception {
    	
    	JSONArray points = new JSONArray();
    	
    	String centroidDataset = (String) datasets.get(centroidId);
    	
    	BigMLClient api = new BigMLClient();
    	JSONObject dataset = null;
    	
    	if (centroidDataset == null || centroidDataset.length() == 0) {
    		// Check if dataset exists for cluster snd centroid
    		JSONObject datasets = api.listDatasets("cluster=" + clusterId);
    		
    		if (((Integer) datasets.get("code")).intValue() == AbstractResource.HTTP_OK) {
    			JSONArray objects = (JSONArray) datasets.get("objects");
    			for (int i=0; i<objects.size(); i++) {
    				JSONObject datasetObj = (JSONObject) objects.get(i);
    				if (centroidId.equals((String) datasetObj.get("centroid"))) {
    					dataset = api.getDataset((String) datasetObj.get("resource"));
    					break;
    				}
    			}
    		}
    		
    		if (dataset == null) {
    			JSONObject args = new JSONObject();
    			args.put("centroid", centroidId);
    			dataset = api.createDataset(clusterId, args, null, null);
    		}
    	} else {
    		dataset = api.getDataset("dataset/" + centroidDataset);
    	}
    	while (!api.datasetIsReady(dataset)) {
    		try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {}
    	}
    	
    	// download dataset to compute local predictions
    	JSONObject downloadedData = api.downloadDataset(
    			(String) dataset.get("resource"), null);
    	
    	String[] lines = ((String) downloadedData.get("csv")).split("\n");
    	String[] headers = lines[0].split(",");
    	for (int i=1; i<lines.length; i++) {
    		String[] lineInfo = lines[i].split(",");
    		JSONObject line = new JSONObject();
    		for (int j=0; j<headers.length; j++) {
    			try {
    				line.put(headers[j], Double.parseDouble(lineInfo[j]));
    			} catch (Exception e) {
    				line.put(headers[j], lineInfo[j]);
				}
    		}
    		points.add(line);
    	}
    	return points;
    }

    /**
     * Computes the list of data points closer to a reference point.
     * If no centroid_id information is provided, the points are chosen
     * from the same cluster as the reference point.
     * The points are returned in a list, sorted according
     * to their distance to the reference point. The number_of_points
     * parameter can be set to truncate the list to a maximum number of
     * results. The response is a dictionary that contains the
     * centroid id of the cluster plus the list of points
     * 
     * @param referencePoint	a reference point
     * @param numberOfPoints	number of data points to calculate
     * @param centroidId		the id of the centroid
     * 
     * @return the list of data points closer to a reference point
     * @throws Exception a generic exception
     */
    public JSONObject closestInCluster(JSONObject referencePoint, 
    		Integer numberOfPoints, String centroidId) 
    		throws Exception {
    	
    	JSONObject closest = new JSONObject();
    	
    	if (centroidId!=null) {
    		boolean existCentroid = false;
    		for (LocalCentroid centroid: centroids) {
    			if (centroid.getCentroidId() == centroidId) {
    				existCentroid = true;
    				break;
    			}
    		}
    		if (!existCentroid) {
        		throw new Exception(
                    "Failed to find the provided centroid_id: " + centroidId);
        	}
    	}
    	
    	if (centroidId == null) {
    		// finding the reference point cluster's centroid
    		JSONObject centroidInfo = centroid(referencePoint);
    		centroidId = (String) centroidInfo.get("centroid_id");
    	}
    	
    	// reading the points that fall in the same cluster
    	JSONArray pointsInCluster = pointsInCluster(centroidId);
    	
        // computing distance to reference point
 		List<JSONObject> points = distances2ToPoint(
     			referencePoint, pointsInCluster);
        
	    Collections.sort(points, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject o1, JSONObject o2) {
                return ((Double) o1.get("distance")).
                		compareTo(((Double) o2.get("distance")));
            }
        });
	    
	    if (numberOfPoints != null) {
            points = points.subList(0, numberOfPoints);
		}
	    
	    for (JSONObject point: points) {
    		point.put("distance", Math.sqrt((Double) point.get("distance")));
    	}
	    
	    JSONArray pointsArray = new JSONArray();
	    for (JSONObject point: points) {
	    	pointsArray.add(point);
	    }
	    
	    if (centroidId != null) {
	    	closest.put("centroid_id", centroidId);
	    }
    	closest.put("reference", referencePoint);
    	closest.put("closest", pointsArray);
    	
    	return closest;
    }
    
    
    /**
     *  Gives the list of centroids sorted according to its distance to
     *  an arbitrary reference point.
     *  
     *  @param referencePoint	the arbitrary reference point
     *  
     *  @return the list of sorted centroids
     */
    public JSONObject sortedCentroids(JSONObject referencePoint) {
    	JSONObject sortedCentroids = new JSONObject();
    	
    	List<JSONObject> closeCentroids = distances2ToPoint(
    			referencePoint, centroids);
    	
    	for (JSONObject centroid: closeCentroids) {
    		centroid.put("distance", Math.sqrt((Double) centroid.get("distance")));
    		centroid.put("center", centroid.get("data"));
    		centroid.remove("data");
    	}
    	
    	Collections.sort(closeCentroids, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject o1, JSONObject o2) {
                return ((Double) o1.get("distance")).
                		compareTo(((Double) o2.get("distance")));
            }
        });
    	sortedCentroids.put("centroids", closeCentroids);
    	sortedCentroids.put("reference", referencePoint);
    	
    	return sortedCentroids;
    }

    /**
     * Returns features defining the centroid according to the list
     * of common field ids that define the centroids.
     * 
     * @param centroid	the local centroid
     * @param fieldIds	the list of field ids
     * 
     * @return	a list of features defining the centroid
     */
    protected List centroidFeatures(LocalCentroid centroid, List<String> fieldIds) {
        List features = new ArrayList();
        for (String fieldId : fieldIds) {
            features.add(centroid.getCenter().get(fieldId));
        }
        return features;
    }
    
    /**
     * Returns training data distribution
     * 
     * @return a list with training data distribution
     */
    protected JSONArray getDataDistribution() {

        JSONArray distribution = new JSONArray();

        for (LocalCentroid centroid : centroids) {
            JSONArray centroidData = new JSONArray();
            centroidData.add(centroid.getName());
            centroidData.add(centroid.getCount());

            distribution.add(centroidData);
        }
        
        return distribution;
    }
    
    /**
     * Clusters statistic information in CSV format
     * 
     * @param outputFilePath	path of the output file
     * 
     * @throws IOException	an IO exception
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
        headers.add("Centroid_name");
        headers.addAll(fieldsName);
        headers.add("Instances");
        
        boolean intercentroids = false;
        boolean headerComplete = false;
        
        List<String> csvStatistics = new ArrayList<String>(Arrays.asList(CSV_STATISTICS));

        List rows = new ArrayList();
        for (LocalCentroid centroid : centroids) {
            List<Object> values = new ArrayList<Object>(headers.size());

            values.add(centroid.getName());
            values.addAll(centroidFeatures(centroid, fieldsId));
            values.add(centroid.getCount());
            
            if (centroids.size() > 1) {
                Map<String, Double> distanceMeasures = centroidDistances(centroid);
                for (String measureName : distanceMeasures.keySet()) {
                    if( !intercentroids ) {
                        headers.add(String.format("%s intercentroid distance", measureName.toLowerCase()));
                    }
                    values.add(distanceMeasures.get(measureName));
                }
                intercentroids = true;
                
                JSONObject distanceInfo = (JSONObject) centroid.getDistance();
                for (Object measureName : distanceInfo.keySet()) {
                    Object result = distanceInfo.get(measureName);

                    if( csvStatistics.contains( measureName.toString() ) ) {
                        if( !headerComplete ) {
                            headers.add(String.format("Distance %s",
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
     * 
     * @return  a summary of the cluster info
     */
    public StringBuilder summarize() {
    	StringBuilder summary = new StringBuilder();
        
        if (criticalValue != null) {
        	summary.append(String.format("G-means Cluster (critical_value=%d)", criticalValue));
        } else {
        	summary.append(String.format("K-means Cluster (k=%d)", centroids.size()));
        }
        summary.append(String.format(" with %s centroids\n\n", centroids.size()));
        
        summary.append("Data distribution:\n");
        if (clusterGlobal != null) {
        	summary.append(String.format("    %s: 100%% (%d instances)\n", 
        			clusterGlobal.getName(), clusterGlobal.getCount()));
        }
        summary.append(Utils.printDistribution(getDataDistribution())).
                append("\n\n");

        summary.append("Cluster metrics:\n");
        summary.append(String.format("    total_ss (Total sum of squares): %s\n", totalSS));
        summary.append(String.format("    within_ss (Total within-cluster sum of the sum of squares): %s\n", withinSS));
        summary.append(String.format("    between_ss (Between sum of squares): %s\n", betweenSS));
        summary.append(String.format("    ratio_ss (Ratio of sum of squares): %s\n", ratioSS));
        summary.append("\n\n");
        
        List<LocalCentroid> sortedCentroids = new ArrayList<LocalCentroid>(centroids);
        Collections.sort(sortedCentroids, new Comparator<LocalCentroid>() {
            @Override
            public int compare(LocalCentroid o1, LocalCentroid o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        
        summary.append("Centroids:\n");
        
        List<LocalCentroid> centroidsList = new ArrayList<LocalCentroid>(centroids);
        if (clusterGlobal != null) {
        	centroidsList.add(0, clusterGlobal);
        }
        
        for (LocalCentroid sortedCentroid : centroidsList) {
            summary.append(String.format("\n%s: ", sortedCentroid.getName()));
            String separator = "";
            
            Iterator it = sortedCentroid.getCenter().entrySet().iterator();
            while (it.hasNext()) {
            	Map.Entry fieldId = (Map.Entry) it.next();
                Object value = sortedCentroid.getCenter().get(fieldId.getKey());
            	
                if( value instanceof String ) {
                    String.format("\"%s\"", value);
                }

                summary.append(String.format("%s%s: %s",
                        separator,
                        fieldsNameById.get(fieldId.getKey().toString()),
                        value));

                separator = ", ";
            }
        }
        summary.append("\n\n");
        
        summary.append("Distance distribution:\n\n");
        for (LocalCentroid sortedCentroid : centroidsList) {
            summary.append(sortedCentroid.printStatistics());
        }
        
        if( centroids.size() > 1 ) {
            summary.append("Intercentroids distance:\n\n");
            for (LocalCentroid sortedCentroid : sortedCentroids) {
                summary.append(String.format("To centroid: %s\n", 
                							 sortedCentroid.getName()));
                Map<String, Double> centoridMeasures = 
                		centroidDistances(sortedCentroid);
                
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


    /**
	 * Checks whether input data is missing a numeric field and fills it with
	 * the average quantity set in default_numeric_value
	 */
	public JSONObject fillNumericDefaults(JSONObject inputData) {
		for (Object fieldId : fields.keySet()) {
			JSONObject field = (JSONObject) fields.get(fieldId);

			String optype = (String) Utils.getJSONObject(this.fields, fieldId + ".optype");
			if ((summaryFields == null || !summaryFields.contains(fieldId)) &&
				"numeric".equals(optype) &&
				inputData.get(fieldId) == null) {

				double defaultValue = 0;
				if (!"zero".equals(this.defaultNumericValue)) {
					defaultValue = ((Number) Utils.getJSONObject(field,
						"summary." + this.defaultNumericValue, 0)).doubleValue();
				}
				inputData.put(fieldId, defaultValue);
			}
		}
		return inputData;
	}

}
