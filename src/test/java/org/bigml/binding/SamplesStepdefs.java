package org.bigml.binding;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.util.*;

import static org.junit.Assert.*;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class SamplesStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(SamplesStepdefs.class);
    
    @Autowired
    CommonStepdefs commonSteps;

    @Autowired
    private ContextRepository context;


    @When("^I download the sample file to \"([^\"]*)\" with (\\d+) rows and \"([^\"]*)\" seed$")
    public void I_download_the_sample_content_to(String fileTo, Long numOfRows, String seed)
            throws Throwable {
        String sampleId = (String) context.getSample().get("resource");
        JSONObject sample = context.api.getSample(sampleId,
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