package org.bigml.binding;

import org.bigml.binding.resources.AbstractResource;
import org.bigml.binding.utils.Utils;
import org.bigml.binding.timeseries.Forecasts;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;

/**
 * A local TimeSeries to create Forecasts.
 *
 * This module defines a TimeSeries to create forecasts locally or
 * embedded into your application without needing to send requests to
 * BigML.io.
 *
 * This module enormously reduces the latency for each prediction and
 * let you use your models offline.
 *
 * Example usage (assuming that you have previously set up the BIGML_USERNAME
 * and BIGML_API_KEY environment variables and that you own the model/id below):
 *
 * // API client
 * BigMLClient api = new BigMLClient();
 *
 * // Retrieve a remote timeseries by id
 * JSONObject jsonTimeSeries = api.
 * 		getTimeSeries("timeseries/551aa203af447f5484000ec0");
 *
 * // A lightweight wrapper around a Time Series
 * LocalTimeSeries localTimeSeries = new LocalTimeSeries(jsonTimeSeries);
 *
 * // Input data
 * JSONObject forecastData =
 *     (JSONObject) JSONValue.parse("{\"000000\": {
 *                                      \"horizon\": 30,
 *                                      \"ets_model\": {
 *                                         \"indices\": [0,1,2],
 *                                         \"nomes\": [\"AA,N\"],
 *                                         \"criterion\": \"bic\",
 *                                         \"limit\": 2
 *                                     } 
 *                                   }");
 *
 * // Calculate score
 * localTimeSeries.forecast(inputData);
 *
 */
public class LocalTimeseries extends ModelFields {
	
	private static final long serialVersionUID = 1L;
	
    private static final String RequiredInput = "horizon";
    
    public static final List<String> SubmodelKeys = 
        Collections.unmodifiableList(Arrays.asList("indices",
                                                   "names",
                                                   "criterion",
                                                   "limit"));

    public static final JSONObject DefaultSubmodel = 
        new JSONObject() {{
            put("criterion", "aic");
            put("limit", 1);
        }};

    // Logging
    Logger logger = LoggerFactory.getLogger(LocalTimeseries.class);

    private JSONObject timeseries;
    private JSONObject forecast;

    private String timeseriesId;
    private BigMLClient bigmlClient;
    
    private JSONArray inputFields;
    private JSONArray objectiveFields;

    private Boolean allNumericObjectives;

    private Long period;
    private JSONObject etsModels;
    private JSONObject error;
    private JSONObject dampedTrend;
    private String seasonality;
    private String trend;
    private JSONObject timeRange;
    private JSONObject fieldParameters;
    
    public LocalTimeseries(JSONObject jsonData) throws Exception {
        this(null, jsonData);
    }
    
    public LocalTimeseries(BigMLClient bigmlClient, JSONObject jsonData) 
    		throws Exception {
		
    	super(Utils.getFromJSONOr(jsonData, "time_series.fields"));
    	
    	this.bigmlClient =
            (bigmlClient != null)
                ? bigmlClient
                : new BigMLClient(null, null, BigMLClient.STORAGE);
    	
		if (!checkModelFields(jsonData)) {
			timeseriesId = (String) jsonData.get("resource");
		}
		
		if (!(jsonData.containsKey("resource")
				&& jsonData.get("resource") != null)) {
			jsonData = this.bigmlClient.getTimeSeries(timeseriesId);
			
			if ((String)jsonData.get("resource") == null) {
				throw new Exception(
					timeseriesId + " is not a valid resource ID.");
			}
		}
		
		if (jsonData.containsKey("object") &&
				jsonData.get("object") instanceof JSONObject) {
			jsonData = (JSONObject)jsonData.get("object");
		}

        try {
            this.timeseriesId = (String) jsonData.get("resource");
            this.inputFields = (JSONArray) jsonData.get("input_fields");
            this.forecast = (JSONObject) jsonData.get("forecast");
            this.objectiveFields = Utils.getFromJSONOr(jsonData,
                                                       "objective_fields",
                                                       new JSONArray());
            
            String objectiveField =
                Utils.getFromJSONOr(jsonData, "objective_field", "");

            JSONObject status = Utils.getFromJSONOr(jsonData, "status");
            if (status != null &&
                status.containsKey("code") &&
                AbstractResource.FINISHED == ((Number) status.get("code")).intValue()) {

                JSONObject timeseriesInfo = Utils.getFromJSONOr(jsonData, "time_series");
                //-- object.model.fields???
                this.fields = Utils.getFromJSONOr(timeseriesInfo, "fields");
            
                if (this.inputFields == null || this.inputFields.size() == 0) {
                    this.inputFields = new JSONArray();
                    ArrayList<String> sortedFields = new ArrayList<String>(this.fields.keySet());

                    Collections.sort(sortedFields, new Comparator<String>() {
                            @SuppressWarnings("unchecked")
                            public int compare(String k1, String k2) {
                                Long v1 = Utils.getFromJSONOr(fields, k1 + ".column_number", 0l);
                                Long v2 = Utils.getFromJSONOr(fields, k2 + ".column_number", 0l);
                                return v1.compareTo(v2);
                            }
                        });

                    for (String c : sortedFields) {
                        inputFields.add(c);
                    }
                }
                this.allNumericObjectives = Utils.getFromJSONOr(timeseriesInfo,
                                                                "all_numeric_objectives",
                                                                false);
                this.period = Utils.getFromJSONOr(timeseriesInfo, "period", 1l);
                this.etsModels = Utils.getFromJSONOr(timeseriesInfo, "ets_models");
                this.error = Utils.getFromJSONOr(timeseriesInfo, "error");
                this.dampedTrend = Utils.getFromJSONOr(timeseriesInfo, "dampled_trend");
                this.seasonality = Utils.getFromJSONOr(timeseriesInfo, "seasonality", null);
                this.trend = Utils.getFromJSONOr(timeseriesInfo, "trend", null);
                this.timeRange = Utils.getFromJSONOr(timeseriesInfo, "time_range");
                this.fieldParameters = Utils.getFromJSONOr(timeseriesInfo, "field_parameters");
  
                //-- not used:            
                //              String objectiveId = objectiveFieldsget("id");

            } else { 
                logger.error("The model is not finished yet");
                throw new IllegalStateException("The model isn't finished yet: " + jsonData.toString());
            }

        } catch (Exception e) {
            logger.error("Invalid model structure", e);
            throw e;
//            throw new InvalidModelException();
        }
    }

    /* Filters the submodels available for the field in the time-series
       model according to the criteria provided in the prediction input data
       for the field.
    */
    private final ArrayList<JSONObject> filterSubmodels(final JSONArray submodels,
                                                        final JSONObject filterInfo) 
        throws Throwable {

        ArrayList<JSONObject> fieldSubmodels = new ArrayList<JSONObject>();
        ArrayList<String> submodelNames = new ArrayList<String>();
        JSONArray indices = Utils.getFromJSONOr(filterInfo, SubmodelKeys.get(0), new JSONArray());
        JSONArray names = Utils.getFromJSONOr(filterInfo, SubmodelKeys.get(1), new JSONArray());
      
        // adding all submodels by index if they are not also in the names list
        if (indices.size() > 0) {
            for (int i = 0; i < submodels.size(); i++) {
                if (indices.contains(i)) {
                    fieldSubmodels.add((JSONObject)submodels.get(i));
                }
            }
        }
      
        // union with filtered by names
        String pattern = "";
        if (names.size() > 0) {
            pattern = Utils.join(names, "|");
            // only adding the submodels if they have not been included by using
            // indices
            for (JSONObject o: fieldSubmodels) {
                submodelNames.add((String)o.get("name"));
            }
            ArrayList<JSONObject> namedSubmodels = new ArrayList<JSONObject>();
            for (Object sm: submodels) {
                JSONObject s = (JSONObject)sm;
                if (((String)s.get("name")).matches(pattern) &&
                    !submodelNames.contains(s.get("name"))) {
                    namedSubmodels.add(s);
                }
            }
            for (JSONObject s: namedSubmodels) {
                if (!fieldSubmodels.contains(s))
                    fieldSubmodels.add(s);
            }
        }

        if (indices.size() == 0 && names.size() == 0) {
            for (Object s: submodels) {
                fieldSubmodels.add((JSONObject)s);
            }
        }

        // filtering the resulting set by criterion and limit
        final String criterion = Utils.getFromJSONOr(filterInfo, SubmodelKeys.get(2), null);

//        Float f1 = getFromJSONOr(fieldSubmodels.get(0), criterion, Float.POSITIVE_INFINITY);


        if (criterion != null) {
            Collections.sort(fieldSubmodels, new Comparator<JSONObject>() {
                    public int compare(JSONObject o1, JSONObject o2) {
                        Float f1 = Utils.getFromJSONOr(o1, criterion, Float.POSITIVE_INFINITY);
                        Float f2 = Utils.getFromJSONOr(o2, criterion, Float.POSITIVE_INFINITY);
                        return f1 > f2 ? 1 : (f2 > f1 ? -1 : 0);
                    }
                });
            Object limit = Utils.getFromJSONOr(filterInfo, SubmodelKeys.get(3), null);
            if (limit != null) {
                int l = Math.min(((Number)limit).intValue(), fieldSubmodels.size());
                fieldSubmodels =
                    new ArrayList<JSONObject>(fieldSubmodels.subList(0, l));
            }
        }
        return fieldSubmodels;
    }

    /* Computes the forecasts for each of the models in the submodels
       array. The number of forecasts is set by horizon.
    */
    private final ArrayList<HashMap<String, Object>>
    computeForecast(final ArrayList<JSONObject> submodels,
                    final Long horizon) 
        throws Throwable {

        ArrayList<HashMap<String, Object>> forecasts = 
            new ArrayList<HashMap<String, Object>>();

        for (final JSONObject sm: submodels) {

            String name = (String)sm.get("name");
            String trend = name;
            String seasonality = null;            
            if (name.indexOf(",") >= 0) {
                String[] cs = name.split(",");
                trend = cs[1];
                seasonality = cs[2];
            }

            HashMap<String, Object> f = new HashMap<String, Object>();
            f.put("model", name);
            f.put("point_forecast", new Forecasts(sm).forecast(trend,
                                                               horizon,
                                                               seasonality));
            forecasts.add(f);
        }
        return forecasts;
    }

    /* Returns the class prediction and the confidence
       input_data: Input data to be predicted
    */
    public final HashMap<String, Object> forecast(final JSONObject inputData)
        throws Throwable {
        
        if (inputData == null || inputData.size() == 0) {
            return forecast();
        }

        /* Checks and cleans input_data leaving only the fields used as
           objective fields in the model */
        HashMap<String, JSONObject> filteredData = this.filterObjectives(inputData);

        /* filter submodels: filtering the submodels in the time-series
           model to be used in the prediction */
        HashMap<String, ArrayList<JSONObject>> filteredSubmodels =
            new HashMap<String, ArrayList<JSONObject>>();
        for (Object k: filteredData.keySet()) {
            JSONObject val = (JSONObject)filteredData.get(k);
            JSONObject filterInfo = (JSONObject)val.get("ets_models");

            if (filterInfo == null || filterInfo.size() == 0) {
                filterInfo = DefaultSubmodel;
            }
            ArrayList<JSONObject> subm =
                this.filterSubmodels((JSONArray)this.etsModels.get(k), filterInfo);
            filteredSubmodels.put((String)k, subm);
        }

        HashMap<String, Object> forecasts = new HashMap<String, Object>();
        for (Object k: filteredSubmodels.keySet()) {
            ArrayList<JSONObject> filterInfo = filteredSubmodels.get(k);
            forecasts.put((String)k,
                          this.computeForecast(filterInfo,
                                               ((Number)filteredData.get(k).get("horizon")).longValue()));
        }
        return forecasts;
    }

    public HashMap<String, Object> forecast()
        throws Exception {
        
        HashMap<String, Object> result = new HashMap<String, Object>();
        for (Object k: this.timeseries.keySet()) {
            JSONObject o = (JSONObject)this.timeseries.get(k);
            HashMap<String, Object> lf = new HashMap<String, Object>();
            lf.put("point_forecast", this.forecast.get("point_forecast"));
            lf.put("point_forecast_2", this.forecast.get("point_forecast"));
            lf.put("model", this.forecast.get("model"));
            result.put((String)k, lf);
        }
        return result;
    }

    /* Filters the keys given in input_data checking against the
       objective fields in the time-series model fields.
       If `full` is set to True, it also
       provides information about the fields that are not used.
    */
    public HashMap<String, JSONObject>
    filterObjectives(final JSONObject inputData)
        throws Exception {

        HashMap<String, JSONObject> newInput = new HashMap<String, JSONObject>();
        ArrayList<String> unusedFields = new ArrayList<String>();
        for (Object k: inputData.keySet()) {
            String fid = (String)k;
            JSONObject val = (JSONObject)inputData.get(fid);
            if (!this.fields.containsKey(fid)) {
                if (!this.invertedFields.containsKey(fid)) {
                    fid = (String)this.invertedFields.get(fid);
                }
            }
            if (this.inputFields.contains(fid)) {
                newInput.put(fid, val);                
            } else {
                unusedFields.add(fid);
            }
        }

        // Raise an error if no horizon is provided
        for (Object k: inputData.keySet()) {
            JSONObject value = this.normalize((JSONObject)inputData.get(k));
            if (!(value instanceof Map)) {
                logger.error("Bad input data");
                throw new
                    InputDataParseException("Each field input data needs to be specified " +
                                            "as a dictionary. Found " + 
                                            value.getClass().toString() +
                                            " for field " + k);
            }
            if (!value.containsKey("horizon")) {
                throw new
                    InputDataParseException("Each field in input data must contain at " +
                                            "least a \"horizon\" attribute." + value.toString());
            }
            JSONObject etsModels = Utils.getFromJSONOr(value, "ets_models");
            for (Object f: etsModels.keySet()) {
                if (!SubmodelKeys.contains(f)) {
                    throw new
                        InputDataParseException("Not allowed value for ets_models: " + f);
                }
            }
        }
        //-- it seems that unusedFields are not used anywhere, so ignoring them
        return newInput;
    }
}
