package org.bigml.binding;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.bigml.binding.resources.AbstractResource;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.util.*;

import static org.junit.Assert.*;

public class SamplesStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(SamplesStepdefs.class);

    CommonStepdefs commonSteps = new CommonStepdefs();

    @Autowired
    private ContextRepository context;

    @Given("^I create a sample from dataset$")
    public void I_create_a_sample_from_dataset() throws AuthenticationException {
        String datasetId = (String) context.dataset.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));

        JSONObject resource = BigMLClient.getInstance().createSample(datasetId,
                args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.sample = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I create a sample from dataset with \"(.*)\"$")
    public void I_create_a_sample_from_dataset_with_options(String args) throws Throwable {
        String datasetId = (String) context.dataset.get("resource");
        JSONObject argsJSON = args != null ? (JSONObject) JSONValue.parse(args)
                : null;

        if( argsJSON != null ) {
            argsJSON.put("tags", Arrays.asList("unitTest"));
        } else {
            argsJSON = new JSONObject();
            argsJSON.put("tags", Arrays.asList("unitTest"));
        }

        JSONObject resource = BigMLClient.getInstance().createSample(datasetId,
                argsJSON, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.sample = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I update the sample with \"(.*)\" waiting less than (\\d+) secs$")
    public void I_update_the_source_with(String args, int secs)
            throws Throwable {
        if (args.equals("{}")) {
            assertTrue("No update params. Continue", true);
        } else {
            String sampleId = (String) context.sample.get("resource");
            JSONObject resource = BigMLClient.getInstance().updateSample(
                    sampleId, args);
            context.status = (Integer) resource.get("code");
            context.location = (String) resource.get("location");
            context.sample = (JSONObject) resource.get("object");
            commonSteps
                    .the_resource_has_been_updated_with_status(context.status);
            I_wait_until_the_sample_is_ready_less_than_secs(secs);
        }
    }

    @Given("^I check the sample name \"(.*)\"$")
    public void I_check_the_sample_name(String expectedName)
            throws Throwable {
        assertEquals(expectedName, context.sample.get("name"));
    }

    @Given("^I wait until the sample status code is either (\\d) or (\\d) less than (\\d+)")
    public void I_wait_until_sample_status_code_is(int code1, int code2,
            int secs) throws AuthenticationException {
        Long code = (Long) ((JSONObject) context.sample.get("status"))
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
            I_get_the_sample((String) context.sample.get("resource"));
            code = (Long) ((JSONObject) context.sample.get("status"))
                    .get("code");
        }
        assertEquals(code1, code.intValue());
    }

    @Given("^I wait until the sample is ready less than (\\d+) secs$")
    public void I_wait_until_the_sample_is_ready_less_than_secs(int secs)
            throws AuthenticationException {
        I_wait_until_sample_status_code_is(AbstractResource.FINISHED,
                AbstractResource.FAULTY, secs);
    }

    @Given("^I get the sample \"(.*)\"")
    public void I_get_the_sample(String sampleId)
            throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().getSample(sampleId);
        Integer code = (Integer) resource.get("code");
        assertEquals(AbstractResource.HTTP_OK, code.intValue());
        context.sample = (JSONObject) resource.get("object");
    }

    @Given("^I store the sample id in a list$")
    public void I_store_the_sample_id_in_a_list()
            throws AuthenticationException {
        if( null == context.samples ) {
            context.samples = new JSONArray();
        }

        assertNotNull("No sample available in the context", context.samples);
        context.samples.add(context.sample.get("resource"));
        context.sample = null;
    }

    @Given("^I ask for the missing values counts in the fields of the sample$")
    public void I_ask_for_the_missing_values_counts_in_the_fields_of_the_sample()
            throws AuthenticationException {
        Fields fields = new Fields((JSONObject) context.sample.get("fields"));
        context.sampleMissingCounts = fields.getMissingCounts();
    }

    @Given("^I ask for the error counts in the fields of the sample$")
    public void I_ask_for_the_error_counts_in_the_fields_of_the_sample()
            throws AuthenticationException {
        String sampleId = (String) context.sample.get("resource");
        context.sampleErrorCounts = BigMLClient.getInstance().getErrorCounts(sampleId);
    }

    @Given("^the (missing values counts|error counts) dict for the sample is \"(.*)\"$")
    public void I_get_the_error_values_for_the_sample(String missingOrErrors, String propertiesDict)
            throws AuthenticationException {
        JSONObject propertiesDictJSON = propertiesDict != null ? (JSONObject) JSONValue.parse(propertiesDict)
                : null;

        assertNotNull("No sample available in the context", context.sample);
        assertNotNull("The propertiesDict was not informed", propertiesDictJSON);


        if( missingOrErrors.equals("error counts") ) {
            for (String fieldId : context.sampleErrorCounts.keySet()) {
                assertEquals(context.sampleErrorCounts.get(fieldId), propertiesDictJSON.get(fieldId));
            }
        } else {
            for (String fieldId : context.sampleMissingCounts.keySet()) {
                assertEquals(context.sampleMissingCounts.get(fieldId), propertiesDictJSON.get(fieldId));
            }
        }
    }


    // ---------------------------------------------------------------------
    // split_dataset.feature
    // ---------------------------------------------------------------------

//    @Given("^I create a sample extracting a ([\\d,.]+) sample$")
//    public void I_create_a_sample_extracting_a_sample(double rate)
//            throws Throwable {
//        if( null == context.datasets ) {
//            context.datasets = new JSONArray();
//        }
//
//        String datasetId = (String) context.dataset.get("resource");
//
//
//        JSONObject args = new JSONObject();
//        args.put("tags", Arrays.asList("unitTest"));
//        args.put("sample_rate", rate);
//
//        datasetOrigRows = (Long) context.dataset.get("rows");
//
//        JSONObject resource = BigMLClient.getInstance().createDataset(datasetId,
//                args, 5, null);
//        context.status = (Integer) resource.get("code");
//        context.location = (String) resource.get("location");
//        context.dataset = (JSONObject) resource.get("object");
//        context.datasets.add(context.dataset.get("resource"));
//        commonSteps.the_resource_has_been_created_with_status(context.status);
//    }
//
//    @When("^I compare the datasets\' instances$")
//    public void I_compare_datasets_instances()
//            throws Throwable {
//        datasetRatedRows = (Long) context.dataset.get("rows");
//    }
//
//    @Then("^the proportion of instances between datasets is ([\\d,.]+)$")
//    public void The_proportion_datasets_instances(double rate)
//            throws Throwable {
//        assertEquals( (long) (datasetOrigRows * rate), datasetRatedRows);
//    }

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

//    @Given("^I make the dataset public$")
//    public void I_make_the_dataset_public() throws Throwable {
//        JSONObject changes = new JSONObject();
//        changes.put("private", new Boolean(false));
//
//        JSONObject resource = BigMLClient.getInstance().updateDataset(
//                context.dataset, changes);
//        context.status = (Integer) resource.get("code");
//        context.location = (String) resource.get("location");
//        context.dataset = (JSONObject) resource.get("object");
//        commonSteps.the_resource_has_been_updated_with_status(context.status);
//    }
//
//    @When("^I get the dataset status using the dataset's public url$")
//    public void I_get_the_dataset_status_using_the_dataset_s_public_url()
//            throws Throwable {
//        String datasetId = (String) context.dataset.get("resource");
//        JSONObject resource = BigMLClient.getInstance().getDataset(
//                "public/" + datasetId);
//        context.status = (Integer) resource.get("code");
//        context.location = (String) resource.get("location");
//        context.dataset = (JSONObject) resource.get("object");
//    }

    @When("the sample\'s status is FINISHED$")
    public void sample_status_finished() {
        Long code = (Long) ((JSONObject) context.sample.get("status"))
                .get("code");
        assertEquals(AbstractResource.FINISHED, code.intValue());
    }

//    @Given("^I check that the dataset is created for the cluster and the centroid$")
//    public void i_check_dataset_from_cluster_centroid() throws Throwable {
//        JSONObject changes = new JSONObject();
//        changes.put("private", new Boolean(false));
//
//        JSONObject resource = BigMLClient.getInstance().getCluster(
//                (JSONObject) context.cluster.get("resource"));
//        context.status = (Integer) resource.get("code");
//
//        assertEquals(AbstractResource.HTTP_OK, context.status);
//
//        assertEquals(context.getDataset().get("resource"), String.format("dataset/%s",
//                Utils.getJSONObject(resource,
//                        String.format("object.cluster_datasets.%s", context.centroid.get("centroid_id")) )));
//
//    }

//    @When("^I download the sample file to \"([^\"]*)\"$")
//    public void I_download_the_sample_file_to(String fileTo)
//            throws Throwable {
//        BigMLClient.getInstance().downloadSample((String) context.getSample().get("resource"),
//                fileTo);
//    }

    @When("^I download the sample file to \"([^\"]*)\" with (\\d+) rows and \"([^\"]*)\" seed$")
    public void I_download_the_sample_content_to(String fileTo, Long numOfRows, String seed)
            throws Throwable {
        String sampleId = (String) context.getSample().get("resource");
        JSONObject sample = BigMLClient.getInstance().getSample(sampleId,
                String.format("rows=%d&seed=%s", numOfRows, seed) );

        JSONArray fields = (JSONArray) Utils.getJSONObject(sample, "object.sample.fields", new JSONArray());
        JSONArray rows = (JSONArray) Utils.getJSONObject(sample, "object.sample.rows", new JSONArray());

        assertEquals("The size of the sample is not the exepected", numOfRows.intValue(), rows.size());

        exportSampleData(fileTo, fields, rows);
    }

    private void exportSampleData(String fileTo, JSONArray fields, JSONArray rows) throws Exception {
        List<String> headers = new ArrayList<String>();
        for (Object field : fields) {
            headers.add((String) ((JSONObject) field).get("name"));
        }

        Writer predictionsFile = null;
        try {
            predictionsFile = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fileTo), "UTF-8"));
        } catch (IOException e) {
            throw new Exception(String.format("Cannot find %s directory.", fileTo));
        }

        final CSVPrinter printer = CSVFormat.DEFAULT.withHeader((String[])
                headers.toArray(new String[headers.size()])).print(predictionsFile);

        try {
            for (Object row : rows) {
                Object[] values = new Object[headers.size()];
                for (int iHeader = 0; iHeader < headers.size(); iHeader++) {
                    values[iHeader] = ((JSONArray) row).get(iHeader);
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

    @Then("^the sample file \"([^\"]*)\" is like \"([^\"]*)\"$")
    public void the_sample_file_is_like(String downloadedFile,
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