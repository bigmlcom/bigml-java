package org.bigml.binding;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
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
    LocalCluster localCluster = null;
    JSONObject  localCentroid = null;
    JSONObject centroid = null;
    JSONObject batchCentroid = null;
    JSONObject project = null;
    JSONArray projects = null;
    JSONObject sample = null;
    JSONArray samples = null;
    MultiModel multiModel = null;
    List<MultiVote> votes = null;
    JSONObject correlation = null;
    JSONArray correlations = null;
    JSONObject statisticaltest = null;
    JSONArray statisticaltests = null;
    JSONObject logisticregression = null;
    JSONArray logisticregressions = null;
    JSONObject script = null;
    JSONArray scripts = null;
    JSONObject execution = null;
    JSONArray executions = null;
    JSONObject library = null;
    JSONArray libraries = null;
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

    Map<String, Long> datasetErrorCounts;
    Map<String, Long> datasetMissingCounts;

    Map<String, Long> sampleErrorCounts;
    Map<String, Long> sampleMissingCounts;

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

    public JSONObject getProject() {
        return project;
    }

    public void setProject(JSONObject project) {
        this.project = project;
    }

    public JSONArray getProjects() {
        return projects;
    }

    public void setProject(JSONArray projects) {
        this.projects = projects;
    }

    public JSONObject getSample() {
        return sample;
    }

    public void setSample(JSONObject sample) {
        this.sample = sample;
    }

    public JSONArray getSamples() {
        return samples;
    }

    public void setSamples(JSONArray samples) {
        this.samples = samples;
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

    public Map<String, Long> getSampleErrorCounts() {
        return sampleErrorCounts;
    }

    public void setSampleErrorCounts(Map<String, Long> sampleErrorCounts) {
        this.sampleErrorCounts = sampleErrorCounts;
    }

    public Map<String, Long> getSampleMissingCounts() {
        return sampleMissingCounts;
    }

    public void setSampleMissingCounts(Map<String, Long> sampleMissingCounts) {
        this.sampleMissingCounts = sampleMissingCounts;
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

    public JSONArray getCorrelations() {
        return correlations;
    }

    public void setCorrelations(JSONArray correlations) {
        this.correlations = correlations;
    }

    public JSONObject getStatisticaltest() {
        return statisticaltest;
    }

    public void setStatisticaltest(JSONObject statisticaltest) {
        this.statisticaltest = statisticaltest;
    }

    public JSONArray getStatisticaltests() {
        return statisticaltests;
    }

    public void setStatisticaltests(JSONArray statisticaltests) {
        this.statisticaltests = statisticaltests;
    }

    public JSONObject getLogisticregression() {
        return logisticregression;
    }

    public void setLogisticregression(JSONObject logisticregression) {
        this.logisticregression = logisticregression;
    }

    public JSONArray getLogisticregressions() {
        return logisticregressions;
    }

    public void setLogisticregressions(JSONArray logisticregressions) {
        this.logisticregressions = logisticregressions;
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

    public JSONArray getExecutions() {
        return executions;
    }

    public void setExecutions(JSONArray executions) {
        this.executions = executions;
    }

    public JSONObject getLibrary() {
        return library;
    }

    public void setLibrary(JSONObject library) {
        this.library = library;
    }

    public JSONArray getLibraries() {
        return libraries;
    }

    public void setLibraries(JSONArray libraries) {
        this.libraries = libraries;
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
}
