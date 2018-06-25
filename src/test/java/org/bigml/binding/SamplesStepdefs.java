package org.bigml.binding;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
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
    
    @Autowired
    CommonStepdefs commonSteps;

    @Autowired
    private ContextRepository context;


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