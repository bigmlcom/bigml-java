package org.bigml.binding;

import org.bigml.binding.localmodel.Prediction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Repository
public class ContextRepository {
	
	BigMLClient api = new BigMLClient();
	
    int status;
    String location = null;
    
    String testProject = null;
    
    JSONObject source = null;
    JSONObject dataset = null;
    JSONObject model = null;
    JSONObject anomaly = null;
    JSONObject anomalyScore = null;
    JSONObject batchAnomalyScore = null;
    JSONObject prediction = null;
    JSONObject evaluation = null;
    JSONObject ensemble = null;
    JSONObject batchPrediction = null;
    JSONObject cluster = null;
    JSONObject localCentroid = null;
    JSONObject centroid = null;
    JSONObject batchCentroid = null;
    JSONObject project = null;
    JSONObject sample = null;
    JSONObject correlation = null;
    JSONObject statisticalTest = null;
    JSONObject logisticRegression = null;
    JSONObject linearRegression = null;
    JSONObject script = null;
    JSONObject execution = null;
    JSONObject library = null;
    JSONObject association = null;
    JSONObject topicModel = null;
    JSONObject topicDistribution = null;
    JSONObject batchTopicDistribution = null;
    JSONObject configuration = null;
    JSONObject timeSeries = null;
    JSONObject forecast = null;
    JSONObject deepnet = null;
    JSONObject optiML = null;
    JSONObject fusion = null;
    JSONObject pca = null;
    JSONObject projection = null;
    JSONObject batchProjection = null;
    
    JSONArray models = null;
    JSONArray anomalies = null;
    JSONArray datasets = null;
    JSONArray scripts = null;
    
    HashMap<String, Object> localPrediction = null;
    Prediction localModelPrediction = null;
    JSONObject localForecast = null;
    JSONObject localProjection = null;
    
    LocalPredictiveModel localModel = null;
    LocalEnsemble localEnsemble = null;
    LocalCluster localCluster = null;
    LocalLogisticRegression localLogisticRegression = null;
    LocalLinearRegression localLinearRegression = null;
    LocalDeepnet localDeepnet = null;
    LocalTimeseries localTimeSeries = null;
    LocalFusion localFusion = null;

    MultiModel multiModel = null;
    List<MultiVote> votes = null;
    
    Map<String, Long> datasetErrorCounts;
    Map<String, Long> datasetMissingCounts;

    ArrayList<String> scriptsIds = new ArrayList<String>();

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

    public JSONObject getProject() {
        return project;
    }

    public void setProject(JSONObject project) {
        this.project = project;
    }

    public JSONObject getSample() {
        return sample;
    }

    public void setSample(JSONObject sample) {
        this.sample = sample;
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

    public JSONObject getCorrelation() {
        return correlation;
    }

    public void setCorrelation(JSONObject correlation) {
        this.correlation = correlation;
    }

    public JSONObject getStatisticalTest() {
        return statisticalTest;
    }

    public void setStatisticalTest(JSONObject statisticalTest) {
        this.statisticalTest = statisticalTest;
    }

    public JSONObject getLogisticRegression() {
        return logisticRegression;
    }

    public void setLogisticRegression(JSONObject logisticRegression) {
        this.logisticRegression = logisticRegression;
    }
    
    public JSONObject getLinearRegression() {
        return linearRegression;
    }

    public void setLinearRegression(JSONObject linearRegression) {
        this.linearRegression = linearRegression;
    }

    public JSONObject getScript() {
        return script;
    }

    public void setScript(JSONObject script) {
        this.script = script;
    }

    public JSONArray getScripts() {
        return scripts;
    }

    public void setScripts(JSONArray scripts) {
        this.scripts = scripts;
    }

    public JSONObject getExecution() {
        return execution;
    }

    public void setExecution(JSONObject execution) {
        this.execution = execution;
    }

    public JSONObject getLibrary() {
        return library;
    }

    public void setLibrary(JSONObject library) {
        this.library = library;
    }

    public JSONObject getAssociation() {
        return association;
    }

    public void setAssociation(JSONObject association) {
        this.association = association;
    }

    public JSONObject getTopicModel() {
        return topicModel;
    }

    public void setTopicModel(JSONObject topicModel) {
        this.topicModel = topicModel;
    }

    public JSONObject getTopicDistribution() {
        return topicDistribution;
    }

    public void setTopicDistribution(JSONObject topicDistribution) {
        this.topicDistribution = topicDistribution;
    }

    public JSONObject getBatchTopicDistribution() {
        return batchTopicDistribution;
    }

    public void setBatchTopicDistribution(JSONObject batchTopicDistribution) {
        this.batchTopicDistribution = batchTopicDistribution;
    }

    public JSONObject getConfiguration() {
        return configuration;
    }

    public void setConfiguration(JSONObject configuration) {
        this.configuration = configuration;
    }

    public JSONObject getTimeSeries() {
        return timeSeries;
    }

    public void setTimeSeries(JSONObject timeSeries) {
        this.timeSeries = timeSeries;
    }

    public JSONObject getForecast() {
        return forecast;
    }

    public void setForecast(JSONObject forecast) {
        this.forecast = forecast;
    }

    public JSONObject getDeepnet() {
        return deepnet;
    }

    public void setDeepnet(JSONObject deepnet) {
        this.deepnet = deepnet;
    }
    
    public JSONObject getOptiML() {
        return optiML;
    }

    public void setOptiML(JSONObject optiML) {
        this.optiML = optiML;
    }
    
    public JSONObject getFusion() {
        return fusion;
    }

    public void setFusion(JSONObject fusion) {
        this.fusion = fusion;
    }
    
    public JSONObject getPca() {
        return pca;
    }

    public void setPca(JSONObject pca) {
        this.pca = pca;
    }

    public JSONObject getProjection() {
        return projection;
    }

    public void setProjection(JSONObject projection) {
        this.projection = projection;
    }

    public JSONObject getBatchProjection() {
        return batchProjection;
    }

    public void setBatchProjection(JSONObject batchProjection) {
        this.batchProjection = batchProjection;
    }
}
