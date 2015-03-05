package org.bigml.binding;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Repository
public class ContextRepository {

    int status;
    String location = null;
    JSONObject source = null;
    JSONObject dataset = null;
    JSONObject model = null;
    JSONObject anomaly = null;
    JSONObject anomalyScore = null;
    JSONArray  anomalyScores = null;
    JSONObject batchAnomalyScore = null;
    JSONObject prediction = null;
    JSONObject evaluation = null;
    JSONObject ensemble = null;
    JSONArray models = null;
    JSONArray anomalies = null;
    JSONArray datasets = null;
    JSONArray sources = null;
    JSONObject batchPrediction = null;
    JSONObject cluster = null;
    JSONObject centroid = null;
    JSONObject batchCentroid = null;
    MultiModel multiModel = null;
    List<MultiVote> votes = null;

    Map<String, Long> datasetErrorCounts;
    Map<String, Long> datasetMissingCounts;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public JSONObject getSource() {
        return source;
    }

    public void setSource(JSONObject source) {
        this.source = source;
    }

    public JSONObject getDataset() {
        return dataset;
    }

    public void setDataset(JSONObject dataset) {
        this.dataset = dataset;
    }

    public JSONObject getModel() {
        return model;
    }

    public void setModel(JSONObject model) {
        this.model = model;
    }

    public JSONObject getAnomaly() {
        return anomaly;
    }

    public void setAnomaly(JSONObject anomaly) {
        this.anomaly = anomaly;
    }

    public JSONObject getAnomalyScore() {
        return anomalyScore;
    }

    public void setAnomalyScore(JSONObject anomalyScore) {
        this.anomalyScore = anomalyScore;
    }

    public JSONArray getAnomalyScores() {
        return anomalyScores;
    }

    public void setAnomalyScores(JSONArray anomalyScores) {
        this.anomalyScores = anomalyScores;
    }

    public JSONObject getBatchAnomalyScore() {
        return batchAnomalyScore;
    }

    public void setBatchAnomalyScore(JSONObject batchAnomalyScore) {
        this.batchAnomalyScore = batchAnomalyScore;
    }

    public JSONObject getPrediction() {
        return prediction;
    }

    public void setPrediction(JSONObject prediction) {
        this.prediction = prediction;
    }

    public JSONObject getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(JSONObject evaluation) {
        this.evaluation = evaluation;
    }

    public JSONObject getEnsemble() {
        return ensemble;
    }

    public void setEnsemble(JSONObject ensemble) {
        this.ensemble = ensemble;
    }

    public JSONArray getSources() {
        return sources;
    }

    public void setSources(JSONArray sources) {
        this.sources = sources;
    }

    public JSONArray getDatasets() {
        return datasets;
    }

    public void setDatasets(JSONArray datasets) {
        this.datasets = datasets;
    }

    public JSONArray getModels() {
        return models;
    }

    public void setModels(JSONArray models) {
        this.models = models;
    }

    public JSONArray getAnomalies() {
        return anomalies;
    }

    public void setAnomalies(JSONArray anomalies) {
        this.anomalies = anomalies;
    }

    public JSONObject getBatchPrediction() {
        return batchPrediction;
    }

    public void setBatchPrediction(JSONObject batchPrediction) {
        this.batchPrediction = batchPrediction;
    }

    public JSONObject getCluster() {
        return cluster;
    }

    public void setCluster(JSONObject cluster) {
        this.cluster = cluster;
    }

    public JSONObject getCentroid() {
        return centroid;
    }

    public void setCentroid(JSONObject centroid) {
        this.centroid = centroid;
    }

    public JSONObject getBatchCentroid() {
        return batchCentroid;
    }

    public void setBatchCentroid(JSONObject batchCentroid) {
        this.batchCentroid = batchCentroid;
    }

    public Map<String, Long> getDatasetErrorCounts() {
        return datasetErrorCounts;
    }

    public void setDatasetErrorCounts(Map<String, Long> datasetErrorCounts) {
        this.datasetErrorCounts = datasetErrorCounts;
    }

    public Map<String, Long> getDatasetMissingCounts() {
        return datasetMissingCounts;
    }

    public void setDatasetMissingCounts(Map<String, Long> datasetMissingCounts) {
        this.datasetMissingCounts = datasetMissingCounts;
    }

    public MultiModel getMultiModel() {
        return multiModel;
    }

    public void setMultiModel(MultiModel multiModel) {
        this.multiModel = multiModel;
    }
}