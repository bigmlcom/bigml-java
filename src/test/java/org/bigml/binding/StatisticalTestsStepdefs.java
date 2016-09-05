package org.bigml.binding;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;
import org.bigml.binding.resources.AbstractResource;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.junit.Assert.*;

public class StatisticalTestsStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(StatisticalTestsStepdefs.class);

    CommonStepdefs commonSteps = new CommonStepdefs();

    @Autowired
    private ContextRepository context;

    private String sharedHash;
    private String sharedKey;

    @Given("^I create a statisticaltest from a dataset$")
    public void I_create_a_statisticaltest_from_a_dataset() throws AuthenticationException {
        String datasetId = (String) context.dataset.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));

        JSONObject resource = BigMLClient.getInstance().createStatisticalTest(datasetId,
                args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.statisticaltest = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }


    @Given("^I wait until the statisticaltest is ready less than (\\d+) secs$")
    public void I_wait_until_the_statisticaltest_is_ready_less_than_secs(int secs) throws AuthenticationException {
        I_wait_until_statisticaltest_status_code_is(AbstractResource.FINISHED,
                AbstractResource.FAULTY, secs);
    }

    @Given("^I wait until the statisticaltest status code is either (\\d) or (\\d) less than (\\d+)$")
    public void I_wait_until_statisticaltest_status_code_is(int code1, int code2, int secs)
            throws AuthenticationException {
        Long code = (Long) ((JSONObject) context.statisticaltest.get("status"))
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
            I_get_the_statisticaltest((String) context.statisticaltest.get("resource"));
            code = (Long) ((JSONObject) context.statisticaltest.get("status"))
                    .get("code");
        }
        assertEquals(code1, code.intValue());
    }

    @Given("^I get the statisticaltest \"(.*)\"")
    public void I_get_the_statisticaltest(String statisticaltestId) throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().getStatisticalTest(statisticaltestId);
        Integer code = (Integer) resource.get("code");
        assertEquals(AbstractResource.HTTP_OK, code.intValue());
        context.statisticaltest = (JSONObject) resource.get("object");
    }


    @Given("^I update the statisticaltest name to \"([^\"]*)\"$")
    public void I_update_the_statisticaltest_name_to(String statisticaltestName) throws Throwable {
        String statisticaltestId = (String) context.statisticaltest.get("resource");

        JSONObject args = new JSONObject();
        args.put("name", statisticaltestName);

        JSONObject resource = BigMLClient.getInstance().updateStatisticalTest(
                statisticaltestId, args.toString());
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.statisticaltest = (JSONObject) resource.get("object");
        commonSteps
                .the_resource_has_been_updated_with_status(context.status);
    }

    @Then("^the statisticaltest name is \"([^\"]*)\"$")
    public void the_statisticaltest_name_is(String expectedName) throws Throwable {
        assertEquals(expectedName, context.statisticaltest.get("name"));
    }

}