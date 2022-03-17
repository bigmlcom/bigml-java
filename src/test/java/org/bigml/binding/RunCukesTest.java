package org.bigml.binding;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.cucumber.spring.CucumberContextConfiguration;

import org.springframework.test.context.ContextConfiguration;


@RunWith(Cucumber.class)
@CucumberContextConfiguration
@ContextConfiguration(classes = ContextRepository.class)
@CucumberOptions(
  plugin = { "pretty", "html:target/cucumber/report.html" },
  glue = {"org.bigml.binding"},
  features = {
        "src/test/resources/test_anomaly.feature",
        "src/test/resources/test_association.feature",
        "src/test/resources/test_batchprediction.feature",
        "src/test/resources/test_cluster.feature",
        "src/test/resources/test_configurations.feature",
        "src/test/resources/test_correlation.feature",
        "src/test/resources/test_dataset.feature",
        "src/test/resources/test_ensemble.feature",
        "src/test/resources/test_evaluation.feature",
        "src/test/resources/test_externalconnector.feature",
        "src/test/resources/test_linearregression.feature",
        "src/test/resources/test_local_anomaly.feature",
        "src/test/resources/test_local_association.feature",
        "src/test/resources/test_local_cluster.feature",
        "src/test/resources/test_local_deepnet.feature",
        "src/test/resources/test_local_ensemble.feature",
        "src/test/resources/test_local_fusion.feature",
        "src/test/resources/test_local_linearregression.feature",
        "src/test/resources/test_local_logisticregression.feature",
        "src/test/resources/test_local_model.feature",
        "src/test/resources/test_local_pca.feature",
        "src/test/resources/test_local_timeseries.feature",
        "src/test/resources/test_local_topicmodel.feature",
        "src/test/resources/test_logisticregression.feature",
        "src/test/resources/test_model.feature",
        "src/test/resources/test_multivote_prediction.feature",
        "src/test/resources/test_optiml_fusion.feature",
        "src/test/resources/test_organization.feature",
        "src/test/resources/test_pca.feature",
        "src/test/resources/test_prediction.feature",
        "src/test/resources/test_project.feature",
        "src/test/resources/test_statisticaltest.feature",
        "src/test/resources/test_timeseries.feature",
        "src/test/resources/test_topicmodel.feature",
        "src/test/resources/test_whizzml.feature" })
public class RunCukesTest {
	
	@BeforeClass
    public static void setup() {
		// Create project for tests running 
        BigMLClient api = new BigMLClient();
        
        Date date = Calendar.getInstance().getTime();  
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String projectName = "Test: java bindings " + dateFormat.format(date);
        
        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTestProject"));
        args.put("name", projectName);
        api.createProject(args);
    }

    @AfterClass
    public static void teardown() {
    	// Remove all projects created for tests running
        BigMLClient api = new BigMLClient();
        
        JSONObject listProjects = (JSONObject) api.listProjects(
        	";tags__in=unitTestProject");
		JSONArray projects = (JSONArray) listProjects.get("objects");
		for (int i = 0; i < projects.size(); i++) {
			JSONObject project = (JSONObject) projects.get(i);
			api.deleteProject(project);
		}
    }
    
}
