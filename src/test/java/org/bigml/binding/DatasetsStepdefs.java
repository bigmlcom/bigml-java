package org.bigml.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import cucumber.annotation.en.Then;
import org.bigml.binding.resources.AbstractResource;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.When;

public class DatasetsStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(DatasetsStepdefs.class);

    CommonStepdefs commonSteps = new CommonStepdefs();

    long datasetOrigRows;
    long datasetRatedRows;

    @Autowired
    private ContextRepository context;

    @Given("^I create a dataset$")
    public void I_create_a_dataset() throws AuthenticationException {
        String sourceId = (String) context.source.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));

        JSONObject resource = BigMLClient.getInstance().createDataset(sourceId,
                args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.dataset = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I create a dataset with \"(.*)\"$")
    public void I_create_a_dataset_with_options(String args) throws Throwable {
        String sourceId = (String) context.source.get("resource");
        JSONObject argsJSON = args != null ? (JSONObject) JSONValue.parse(args)
                : null;

        if( argsJSON != null ) {
            argsJSON.put("tags", Arrays.asList("unitTest"));
        } else {
            argsJSON = new JSONObject();
            argsJSON.put("tags", Arrays.asList("unitTest"));
        }

        JSONObject resource = BigMLClient.getInstance().createDataset(sourceId,
                argsJSON, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.dataset = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I wait until the dataset status code is either (\\d) or (\\d) less than (\\d+)")
    public void I_wait_until_dataset_status_code_is(int code1, int code2,
            int secs) throws AuthenticationException {
        Long code = (Long) ((JSONObject) context.dataset.get("status"))
                .get("code");
        GregorianCalendar start = new GregorianCalendar();
        start.add(Calendar.SECOND, secs);
        Date end = start.getTime();
        while (code.intValue() != code1 && code.intValue() != code2) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            assertTrue("Time exceded ", end.after(new Date()));
            I_get_the_dataset((String) context.dataset.get("resource"));
            code = (Long) ((JSONObject) context.dataset.get("status"))
                    .get("code");
        }
        assertEquals(code1, code.intValue());
    }

    @Given("^I wait until the dataset is ready less than (\\d+) secs$")
    public void I_wait_until_the_dataset_is_ready_less_than_secs(int secs)
            throws AuthenticationException {
        I_wait_until_dataset_status_code_is(AbstractResource.FINISHED,
                AbstractResource.FAULTY, secs);
    }

    @Given("^I get the dataset \"(.*)\"")
    public void I_get_the_dataset(String datasetId)
            throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().getDataset(datasetId);
        Integer code = (Integer) resource.get("code");
        assertEquals(AbstractResource.HTTP_OK, code.intValue());
        context.dataset = (JSONObject) resource.get("object");
    }

    @Given("^I store the dataset id in a list$")
    public void I_store_the_dataset_id_in_a_list()
            throws AuthenticationException {
        if( null == context.datasets ) {
            context.datasets = new JSONArray();
        }

        assertNotNull("No dataset available in the context", context.datasets);
        context.datasets.add(context.dataset.get("resource"));
        context.dataset = null;
    }

    @Given("^I ask for the missing values counts in the fields$")
    public void I_ask_for_the_missing_values_counts_in_the_fields()
            throws AuthenticationException {
        Fields fields = new Fields((JSONObject) context.dataset.get("fields"));
        context.datasetMissingCounts = fields.getMissingCounts();
    }

    @Given("^I ask for the error counts in the fields$")
    public void I_ask_for_the_error_counts_in_the_fields()
            throws AuthenticationException {
        String datasetId = (String) context.dataset.get("resource");
        context.datasetErrorCounts = BigMLClient.getInstance().getErrorCounts(datasetId);
    }

    @Given("^the (missing values counts|error counts) dict is \"(.*)\"$")
    public void I_get_the_error_values(String missingOrErrors, String propertiesDict)
            throws AuthenticationException {
        JSONObject propertiesDictJSON = propertiesDict != null ? (JSONObject) JSONValue.parse(propertiesDict)
                : null;

        assertNotNull("No dataset available in the context", context.dataset);
        assertNotNull("The propertiesDict was not informed", propertiesDictJSON);


        if( missingOrErrors.equals("error counts") ) {
            for (String fieldId : context.datasetErrorCounts.keySet()) {
                assertEquals(context.datasetErrorCounts.get(fieldId), propertiesDictJSON.get(fieldId));
            }
        } else {
            for (String fieldId : context.datasetMissingCounts.keySet()) {
                assertEquals(context.datasetMissingCounts.get(fieldId), propertiesDictJSON.get(fieldId));
            }
        }
    }


    // ---------------------------------------------------------------------
    // split_dataset.feature
    // ---------------------------------------------------------------------

    @Given("^I create a dataset extracting a ([\\d,.]+) sample$")
    public void I_create_a_dataset_extracting_a_sample(double rate)
            throws Throwable {
        if( null == context.datasets ) {
            context.datasets = new JSONArray();
        }

        String datasetId = (String) context.dataset.get("resource");


        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));
        args.put("sample_rate", rate);

        datasetOrigRows = (Long) context.dataset.get("rows");

        JSONObject resource = BigMLClient.getInstance().createDataset(datasetId,
                args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.dataset = (JSONObject) resource.get("object");
        context.datasets.add(context.dataset.get("resource"));
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @When("^I compare the datasets\' instances$")
    public void I_compare_datasets_instances()
            throws Throwable {
        datasetRatedRows = (Long) context.dataset.get("rows");
    }

    @Then("^the proportion of instances between datasets is ([\\d,.]+)$")
    public void The_proportion_datasets_instances(double rate)
            throws Throwable {
        assertEquals( (long) (datasetOrigRows * rate), datasetRatedRows);
    }

    /*
     * world.origin_dataset = world.dataset resource =
     * world.api.create_dataset(world.dataset['resource'], {'sample_rate':
     * float(rate)}) world.status = resource['code'] assert world.status ==
     * HTTP_CREATED world.location = resource['location'] world.dataset =
     * resource['object'] world.datasets.append(resource['resource'])
     */

    /*
     * @When("^I compare the datasets' instances$") public void
     * I_compare_the_datasets_instances() throws Throwable { // Express the
     * Regexp above with the code you wish you had throw new PendingException();
     * }
     * 
     * @Then("^the proportion of instances between datasets is (\\d+).(\\d+)$")
     * public void the_proportion_of_instances_between_datasets_is_(int arg1,
     * int arg2) throws Throwable { // Express the Regexp above with the code
     * you wish you had throw new PendingException(); }
     */

    // ---------------------------------------------------------------------
    // create_public_dataset.feature
    // ---------------------------------------------------------------------

    @Given("^I make the dataset public$")
    public void I_make_the_dataset_public() throws Throwable {
        JSONObject changes = new JSONObject();
        changes.put("private", new Boolean(false));

        JSONObject resource = BigMLClient.getInstance().updateDataset(
                context.dataset, changes);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.dataset = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_updated_with_status(context.status);
    }

    @When("^I get the dataset status using the dataset's public url$")
    public void I_get_the_dataset_status_using_the_dataset_s_public_url()
            throws Throwable {
        String datasetId = (String) context.dataset.get("resource");
        JSONObject resource = BigMLClient.getInstance().getDataset(
                "public/" + datasetId);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.dataset = (JSONObject) resource.get("object");
    }

    @When("the dataset\'s status is FINISHED$")
    public void dataset_status_finished() {
        Long code = (Long) ((JSONObject) context.dataset.get("status"))
                .get("code");
        assertEquals(AbstractResource.FINISHED, code.intValue());
    }

    @Given("^I check that the dataset is created for the cluster and the centroid$")
    public void i_check_dataset_from_cluster_centroid() throws Throwable {
        JSONObject changes = new JSONObject();
        changes.put("private", new Boolean(false));

        JSONObject resource = BigMLClient.getInstance().getCluster(
                (JSONObject) context.cluster.get("resource"));
        context.status = (Integer) resource.get("code");

        assertEquals(AbstractResource.HTTP_OK, context.status);

        assertEquals(context.getDataset().get("resource"), String.format("dataset/%s",
                Utils.getJSONObject(resource,
                        String.format("object.cluster_datasets.%s", context.centroid.get("centroid_id")) )));

    }

    @When("^I download the dataset file to \"([^\"]*)\"$")
    public void I_download_the_dataset_file_to(String fileTo)
            throws Throwable {
        BigMLClient.getInstance().downloadDataset((String) context.getDataset().get("resource"),
                fileTo);
    }

    @Then("^the dataset file \"([^\"]*)\" is like \"([^\"]*)\"$")
    public void the_dataset_file_is_like(String downloadedFile,
                                                  String checkFile) throws Throwable {

        FileInputStream downloadFis = new FileInputStream(new File(
                downloadedFile));
        FileInputStream checkFis = new FileInputStream(new File(checkFile));

        String localCvs = Utils.inputStreamAsString(downloadFis, "UTF-8");
        String checkCvs = Utils.inputStreamAsString(checkFis, "UTF-8");

        if (!localCvs.equals(checkCvs)) {
            throw new Exception();
        }

    }

}