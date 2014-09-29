package org.bigml.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.bigml.binding.resources.AbstractResource;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cucumber.annotation.en.Given;

public class SourcesStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(SourcesStepdefs.class);

    CommonStepdefs commonSteps = new CommonStepdefs();

    @Autowired
    private ContextRepository context;

    @Given("^I create a data source uploading a \"([^\"]*)\" file$")
    public void I_create_a_data_source_uploading_a_file(String fileName)
            throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().createSource(fileName,
                "new source", new JSONObject());
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.source = (JSONObject) resource.get("object");

        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I create a data source using the url \"([^\"]*)\"$")
    public void I_create_a_data_source_using_the_url(String url)
            throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().createRemoteSource(url,
                new JSONObject());
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.source = (JSONObject) resource.get("object");

        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I wait until the resource status code is either (\\d) or (\\d) less than (\\d+)")
    public void I_wait_until_source_status_code_is(int code1, int code2,
            int secs) throws AuthenticationException {
        Long code = (Long) ((JSONObject) context.source.get("status"))
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
            I_get_the_source((String) context.source.get("resource"));
            code = (Long) ((JSONObject) context.source.get("status"))
                    .get("code");
        }
        assertEquals(code1, code.intValue());
    }

    @Given("^I wait until the source is ready less than (\\d+) secs$")
    public void I_wait_until_the_source_is_ready_less_than_secs(int secs)
            throws AuthenticationException {
        I_wait_until_source_status_code_is(AbstractResource.FINISHED,
                AbstractResource.FAULTY, secs);
    }

    @Given("^I get the source \"(.*)\"")
    public void I_get_the_source(String sourceId)
            throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().getSource(sourceId);
        Integer code = (Integer) resource.get("code");
        assertEquals(AbstractResource.HTTP_OK, code.intValue());
        context.source = (JSONObject) resource.get("object");
    }

    @Given("^I update the source with \"(.*)\" waiting less than (\\d+) secs$")
    public void I_update_the_source_with(String args, int secs)
            throws Throwable {
        if (args.equals("{}")) {
            assertTrue("No update params. Continue", true);
        } else {
            String sourceId = (String) context.source.get("resource");
            JSONObject resource = BigMLClient.getInstance().updateSource(
                    sourceId, args);
            context.status = (Integer) resource.get("code");
            context.location = (String) resource.get("location");
            context.dataset = (JSONObject) resource.get("object");
            commonSteps
                    .the_resource_has_been_updated_with_status(context.status);
            I_wait_until_the_source_is_ready_less_than_secs(secs);
        }
    }

}