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
        "src/test/resources/test_01_prediction.feature",
        "src/test/resources/test_03_local_prediction.feature",
        "src/test/resources/test_04_multivote_prediction.feature",
        "src/test/resources/test_05_compare_predictions.feature",
        "src/test/resources/test_06_batch_predictions.feature",
        "src/test/resources/test_07_multimodel_batch_predictions.feature",
        "src/test/resources/test_08_multimodel.feature",
        "src/test/resources/test_09_ensemble_prediction.feature",
        "src/test/resources/test_10_local_ensemble_prediction.feature",
        "src/test/resources/test_11_multimodel_prediction.feature",
        "src/test/resources/test_12_public_model_prediction.feature",
        "src/test/resources/test_13_public_dataset.feature",
        "src/test/resources/test_14_create_evaluations.feature",
        "src/test/resources/test_15_download_dataset.feature",
        "src/test/resources/test_16_sample_dataset.feature",
        "src/test/resources/test_17_split_dataset.feature",
        "src/test/resources/test_18_create_anomaly.feature",
        "src/test/resources/test_19_missing_and_errors.feature",
        "src/test/resources/test_20_rename_duplicated_names.feature",
        "src/test/resources/test_21_projects.feature",
        "src/test/resources/test_24_cluster_derived.feature",
        "src/test/resources/test_25_correlation.feature",
        "src/test/resources/test_26_statistical_test.feature",
        "src/test/resources/test_27_logistic_regression.feature",
        "src/test/resources/test_28_associations.feature",
        "src/test/resources/test_29_script.feature",
        "src/test/resources/test_30_execution.feature",
        "src/test/resources/test_31_library.feature",
        "src/test/resources/test_32_topic_model_prediction.feature",
        "src/test/resources/test_33_compare_predictions.feature",
        "src/test/resources/test_34_timeseries.feature",
        "src/test/resources/test_35_compare_predictions.feature",
        "src/test/resources/test_36_compare_predictions.feature",
        "src/test/resources/test_38_organization.feature",
        "src/test/resources/test_39_optiml_fusion.feature",
        "src/test/resources/test_42_pca.feature",
        "src/test/resources/test_43_linear_regression.feature",
        "src/test/resources/test_configurations.feature",
        "src/test/resources/delete_all_test_data.feature" })
public class RunCukesTest {
}
