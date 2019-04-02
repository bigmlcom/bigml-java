package org.bigml.binding;

import org.junit.runner.RunWith;

import cucumber.junit.Cucumber;

@RunWith(Cucumber.class)
// @Cucumber.Options(format = {"pretty", "html:target/cucumber-html-report"})
//@Cucumber.Options(format = { "pretty", "html:target/cucumber-html-report" }, features = {
//        "src/test/resources/delete_all_test_data.feature" })
@Cucumber.Options(format = { "pretty", "html:target/cucumber-html-report" },
  glue = {"org.bigml.binding"},
  features = {
        "src/test/resources/test_anomaly.feature",
        "src/test/resources/test_association.feature",
        "src/test/resources/test_batchpredictions.feature",
        "src/test/resources/test_cluster.feature",
        "src/test/resources/test_configurations.feature",
        "src/test/resources/test_correlation.feature",
        "src/test/resources/test_dataset.feature",
        "src/test/resources/test_deepnet.feature",
        "src/test/resources/test_ensemble.feature",
        "src/test/resources/test_evaluation.feature",
        "src/test/resources/test_linearregression.feature",
        "src/test/resources/test_logisticregression.feature",
        "src/test/resources/test_model.feature",
        "src/test/resources/test_optiml_fusion.feature",
        //"src/test/resources/test_organization.feature",
        "src/test/resources/test_pca.feature",
        "src/test/resources/test_project.feature",
        "src/test/resources/test_sample_dataset.feature",
        "src/test/resources/test_statisticaltest.feature",
        "src/test/resources/test_timeseries.feature",
        "src/test/resources/test_topicmodel.feature",
        "src/test/resources/test_whizzml.feature",
        "src/test/resources/delete_all_test_data.feature" })
public class RunCukesTest {
}
