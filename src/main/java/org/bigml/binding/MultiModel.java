/*
 A Multiple Local Predictive Model.

This module defines a Multiple Model to make predictions locally using multiple
local models.

This module cannot only save you a few credits, but also enormously
reduce the latency for each prediction and let you use your models
offline.

import org.bigml.binding.BigMLClient;
import org.bigml.binding.MultiModel;

BigMLClient bigmlClient = BigMLCliente.getInstance();
JSONObject models = bigmlClient.listModels("my_tag");
MultiModel model = new MultiModel(models);
model.predict({"petal length": 3, "petal width": 1})

 */
package org.bigml.binding;

import java.io.*;
import java.util.*;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.bigml.binding.localmodel.Prediction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Logging
     */
    static Logger logger = LoggerFactory.getLogger(MultiModel.class.getName());

    private static String PREDICTIONS_FILE_SUFFIX = "_predictions.csv";

    private JSONArray models;
    private MultiVote votes;

    /**
     * Constructor
     * 
     * @param models
     *              the json representation for the remote models
     */
    public MultiModel(Object models) throws Exception {
        super();

        if (models instanceof JSONArray) {
            this.models = (JSONArray) models;
        } else if( models instanceof List ) {
            this.models = new JSONArray();
            this.models.addAll((List) models);
        } else {
            this.models = new JSONArray();
            this.models.add(models);
        }
    }

    /**
     * Lists all the model/ids that compound the multi model.
     */
    public JSONArray listModels() {
        return this.models;
    }

    /**
     * Makes a prediction based on the prediction made by every model.
     * 
     * The method parameter is a numeric key to the following combination
     * methods in classifications/regressions: 0 - majority vote (plurality)/
     * average: PLURALITY_CODE 1 - confidence weighted majority vote / error
     * weighted: CONFIDENCE_CODE 2 - probability weighted majority vote /
     * average: PROBABILITY_CODE
     */
    public HashMap<Object, Object> predict(final JSONObject inputData,
            Boolean byName, PredictionMethod method, Boolean withConfidence)
            throws Exception {
        if (method == null) {
            method = PredictionMethod.PLURALITY;
        }
        if (byName == null) {
            byName = true;
        }
        if (withConfidence == null) {
            withConfidence = false;
        }

        votes = this.generateVotes(inputData, byName, null, null);

        return votes.combine(method, withConfidence, null, null, null, null, null);
    }

    /**
     * Makes a prediction based on the prediction made by every model.
     *
     * The method parameter is a numeric key to the following combination
     *   methods in classifications/regressions:
     *      0 - majority vote (plurality)/ average: PLURALITY_CODE
     *      1 - confidence weighted majority vote / error weighted:
     *              CONFIDENCE_CODE
     *      2 - probability weighted majority vote / average:
     *              PROBABILITY_CODE
     *      3 - threshold filtered vote / doesn't apply:
     *              THRESHOLD_COD
     */
    public HashMap<Object, Object> predict(final JSONObject inputData,
            Boolean byName, PredictionMethod method, Boolean withConfidence,
            Map options, MissingStrategy strategy, Boolean addConfidence,
            Boolean addDistribution, Boolean addCount, Boolean addMedian)
            throws Exception {

        if (method == null) {
            method = PredictionMethod.PLURALITY;
        }
        if (byName == null) {
            byName = true;
        }
        if (withConfidence == null) {
            withConfidence = false;
        }

        votes = this.generateVotes(inputData, byName, strategy, addMedian);

        return votes.combine(method, withConfidence, addConfidence,
                addDistribution, addCount, addMedian, options);
    }

    /**
     * Makes predictions for a list of input data.
     *
     * The predictions generated for each model are stored in an output
     * file. The name of the file will use the following syntax:
     *
     *      model_[id of the model]__predictions.csv
     *
     * For instance, when using model/50c0de043b563519830001c2 to predict,
     * the output file name will be
     *
     *      model_50c0de043b563519830001c2__predictions.csv
     *
     * The method parameter is a numeric key to the following combination
     * methods in classifications/regressions: 0 - majority vote (plurality)/
     * average: PLURALITY_CODE 1 - confidence weighted majority vote / error
     * weighted: CONFIDENCE_CODE 2 - probability weighted majority vote /
     * average: PROBABILITY_CODE
     */
    public void batchPredict(final JSONArray inputDataList,
                             String outputFilePath)

            throws Exception {

        batchPredict(inputDataList, outputFilePath, null, null, null, null, null, null);
    }

        /**
         * Makes predictions for a list of input data.
         *
         * The predictions generated for each model are stored in an output
         * file. The name of the file will use the following syntax:
         *
         *      model_[id of the model]__predictions.csv
         *
         * For instance, when using model/50c0de043b563519830001c2 to predict,
         * the output file name will be
         *
         *      model_50c0de043b563519830001c2__predictions.csv
         *
         * The method parameter is a numeric key to the following combination
         * methods in classifications/regressions: 0 - majority vote (plurality)/
         * average: PLURALITY_CODE 1 - confidence weighted majority vote / error
         * weighted: CONFIDENCE_CODE 2 - probability weighted majority vote /
         * average: PROBABILITY_CODE
         */
    public List<MultiVote> batchPredict(final JSONArray inputDataList,
            String outputFilePath, Boolean byName, Boolean reuse,
            MissingStrategy strategy, Set<String> headers, Boolean toFile,
                             Boolean useMedian)

            throws Exception {
        if (strategy == null) {
            strategy = MissingStrategy.LAST_PREDICTION;
        }
        if (byName == null) {
            byName = true;
        }
        if (reuse == null) {
            reuse = false;
        }

        if (toFile == null) {
            toFile = true;
        }

        if (useMedian == null) {
            useMedian = false;
        }

        List<MultiVote> votes = new ArrayList<MultiVote>();
        int order = 0;

        for (Object model : models) {
            JSONObject modelObj = (JSONObject) model;

            order += 1;

            List<Prediction> predictions =
                    new ArrayList<Prediction>(models.size());

            Set availableHeaders = new TreeSet();

            try {
                int index = 0;
                for (Object inputData : inputDataList) {
                    LocalPredictiveModel localModel = new LocalPredictiveModel(modelObj);

                    Prediction prediction =
                            localModel.predict((JSONObject) inputData, byName, strategy);

                    // if median is to be used, we just place it as prediction
                    //  starting the list
                    if( useMedian && localModel.isRegression() ) {
                        prediction.setPrediction(prediction.getMedian());
                    }

                    predictions.add(prediction);

                    availableHeaders.addAll(prediction.keySet());

                    // Prediction is a row that contains prediction, confidence,
                    // distribution, instances
                    Prediction predictionWithOrder = new Prediction();
                    predictionWithOrder.putAll(prediction);

                    if( votes.size() <= index ) {
                        votes.add(new MultiVote());
                    }

                    votes.get(index).append(predictionWithOrder);

                    index++;
                }
            } catch (Exception e) {
                throw new Exception("Error generating the CSV !!!", e);
            }

            if( toFile ) {
                List headersList = new ArrayList();
                if( headers != null && !headers.isEmpty() ) {
                    for (Object availableHeader : availableHeaders) {
                        if( headers.contains(availableHeader) ) {
                            headersList.add(availableHeader);
                        }
                    }
                } else {
                    headersList.addAll(availableHeaders);
                }

                String ouputFile = getPredictionsFileName(modelObj.get("resource").toString(),
                        outputFilePath);

                Writer predictionsFile = null;
                try {
                    if (reuse) {
                        predictionsFile = new BufferedWriter(new OutputStreamWriter(
                                new FileOutputStream(ouputFile, true), "UTF-8"));
                    } else {
                        predictionsFile = new BufferedWriter(new OutputStreamWriter(
                                new FileOutputStream(ouputFile), "UTF-8"));
                    }
                } catch (IOException e) {
                    throw new Exception(String.format("Cannot find %s directory.", outputFilePath));
                }

                final CSVPrinter printer = CSVFormat.DEFAULT.withHeader((String[])
                        headersList.toArray(new String[headersList.size()])).print(predictionsFile);

                try {
                    for (HashMap<Object, Object> prediction : predictions) {
                        Object[] values = new Object[headersList.size()];
                        for (int iHeader = 0; iHeader < headersList.size(); iHeader++) {
                            values[iHeader] = prediction.get(headersList.get(iHeader));
                        }
                        printer.printRecord(values);
                    }
                } catch (Exception e) {
                    throw new Exception("Error generating the CSV !!!");
                }

                try {
                    predictionsFile.flush();
                    predictionsFile.close();
                } catch (IOException e) {
                    throw new Exception("Error while flushing/closing fileWriter !!!");
                }
            }

        }

        return votes;
    }

    /**
     * Generates a MultiVote object that contains the predictions made by each
     * of the models.
     */
    public MultiVote generateVotes(final JSONObject inputData, Boolean byName,
            MissingStrategy strategy, Boolean addMedian) throws Exception {
        if (byName == null) {
            byName = true;
        }
        if (strategy == null) {
            strategy = MissingStrategy.LAST_PREDICTION;
        }
        if (addMedian == null) {
            addMedian = false;
        }

        MultiVote votes = new MultiVote();

        for (int i = 0; i < models.size(); i++) {
            JSONObject model = (JSONObject) models.get(i);
            LocalPredictiveModel localModel = new LocalPredictiveModel(model);

            Prediction predictionInfo = localModel.predict(inputData, byName, strategy);

            votes.append(predictionInfo);
        }

        return votes;
    }


    /**
     * Adds the votes for predictions generated by the models.
     *
     * Returns a list of MultiVote objects each of which contains a list
     * of predictions.
     *
     */
    public List<MultiVote> batchVotes(String predictionsFilePath, Locale dataLocale)
            throws Exception {
        List<String> votesFiles = new ArrayList<String>();

        for (Object model : models) {
            JSONObject modelObj = (JSONObject) model;

            votesFiles.add(getPredictionsFileName(modelObj.get("resource").toString(),
                    predictionsFilePath));
        }

        return readVotes(votesFiles, new LocalPredictiveModel((JSONObject) models.get(0)),
                dataLocale);
    }

    /**
     * Reads the votes found in the votes' files.
     *
     * Returns a list of MultiVote objects containing the list of predictions.
     * votes_files parameter should contain the path to the files where votes
     * are stored
     * In to_prediction parameter we expect the method of a local model object
     * that casts the string prediction values read from the file to their
     * real type. For instance
     *
     *      LocalPredictiveModel localModel = new LocalPredictiveModel(model)
     *      Object prediction = localModel .toPrediction("1")
     *      prediction instanceof Double
     *      True
     *      read_votes(["my_predictions_file"], localModel)
     *
     *  data_locale should contain the string identification for the locale
     *  used in numeric formatting.
     *
     */
    public List<MultiVote> readVotes(List<String> votesFiles, PredictionConverter converter, Locale locale)

            throws Exception {
        List<MultiVote> votes = new ArrayList();

        for ( int votesIndex = 0; votesIndex < votesFiles.size(); votesIndex++) {
            String voteFile = votesFiles.get(votesIndex);

            final Reader reader = new InputStreamReader(new FileInputStream(voteFile), "UTF-8");
            final CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());


            try {
                Map<String, Integer> headerMap = parser.getHeaderMap();

                int index = 0;
                for (final CSVRecord record : parser) {
                    if( index == votes.size() ) {
                        votes.add(new MultiVote());
                    }

                    HashMap<String, Object> predictionData = new HashMap<String, Object>(4);

                    for (String headerName : headerMap.keySet()) {
                        Object value = record.get(headerName);
                        if( value != null && value.toString().length() > 0 ) {
                            if( "prediction".equals(headerName) ) {
                                value = converter.toPrediction((String) value, locale);
                            } else if( "order".equals(headerName) ) {
                                value = Integer.parseInt(value.toString());
                            } else if( "distribution".equals(headerName) ) {
                                value = JSONValue.parse(value.toString());
                            } else if( "instances".equals(headerName) ) {
                                value = Long.parseLong(value.toString());
                            } else if( "confidence".equals(headerName) ) {
                                value = Double.parseDouble(value.toString());
                            }

                            predictionData.put(headerName, value);
                        }
                    }

                    List<String> predHeaders = new ArrayList<String>(predictionData.size());
                    List<Object> predValues = new ArrayList<Object>(predictionData.size());

                    for (String predictionName : predictionData.keySet()) {
                        predHeaders.add(predictionName);
                        predValues.add(predictionData.get(predictionName));
                    }

                    votes.get(index).appendRow(predValues, predHeaders);
                    index++;
                }
            } finally {
                parser.close();
                reader.close();
            }

        }

        return votes;
    }

    protected String getPredictionsFileName(JSONObject model, String path) {
        return getPredictionsFileName((String) model.get("resource"), path);
    }

    protected String getPredictionsFileName(String modelId, String path) {
        return String.format("%s%s%s%s", path,
                File.separator, modelId.replace('/','_'),
                PREDICTIONS_FILE_SUFFIX);
    }

}
