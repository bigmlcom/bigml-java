package org.bigml.binding;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.bigml.binding.resources.AbstractResource;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.util.*;

import static org.junit.Assert.*;

public class ScriptsStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(ScriptsStepdefs.class);

    CommonStepdefs commonSteps = new CommonStepdefs();

    @Autowired
    private ContextRepository context;

    @Given("^I create a whizzml script from a excerpt of code \"([^\"]*)\"$")
    public void I_create_a_whizzml_script_from_a_excerpt_of_code(String source)
        throws AuthenticationException {
        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));

        JSONObject resource = BigMLClient.getInstance().createScript(source, args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.script = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);

        context.scriptsIds.add((String) context.script.get("resource"));
    }

    @Given("^I wait until the script status code is either (\\d) or (\\d) less than (\\d+)")
    public void I_wait_until_script_status_code_is(int code1, int code2,
                                                   int secs) throws AuthenticationException {
        Long code = (Long) context.script.get("code");
        GregorianCalendar start = new GregorianCalendar();
        start.add(Calendar.SECOND, secs);
        Date end = start.getTime();
        while (code.intValue() != code1 && code.intValue() != code2) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            assertTrue("Time exceded ", end.after(new Date()));
            I_get_the_script((String) context.script.get("resource"));
            code = (Long) context.script.get("code");
        }
        assertEquals(code1, code.intValue());
    }

    @Given("^I get the script \"(.*)\"")
    public void I_get_the_script(String scriptId)
            throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().getScript(scriptId);
        Integer code = (Integer) resource.get("code");
        assertEquals(AbstractResource.HTTP_OK, code.intValue());
        context.script = (JSONObject) resource.get("object");
    }

    @Given("^I wait until the script is ready less than (\\d+) secs$")
    public void I_wait_until_the_script_is_ready_less_than_secs(int secs)
        throws AuthenticationException {
        I_wait_until_script_status_code_is(AbstractResource.HTTP_OK,
                AbstractResource.HTTP_NOT_FOUND, secs);
    }

    @Given("^I wait until the script update is ready less than (\\d+) secs$")
    public void I_wait_until_the_script_update_is_ready_less_than_secs(int secs)
        throws AuthenticationException {
        I_wait_until_script_status_code_is(AbstractResource.HTTP_ACCEPTED,
                AbstractResource.HTTP_NOT_FOUND, secs);
    }

    @Given("^I update the script with \"([^\"]*)\", \"([^\"]*)\"$")
    public void I_update_the_script_with_(String param, String paramValue)
        throws AuthenticationException {

        JSONObject chamges = new JSONObject();
        chamges.put(param, paramValue);

        JSONObject resource = BigMLClient.getInstance().updateScript(
                context.script, chamges);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.script = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_updated_with_status(context.status);
    }

    @Then("^the script code is \"([^\"]*)\" and the value of \"([^\"]*)\" is \"([^\"]*)\"$")
    public void the_script_code_is_and_the_value_of_is(String source, String param, String paramValue) throws AuthenticationException {
        assertEquals(source, context.script.get("source_code"));
        assertEquals(paramValue, context.script.get(param));
    }

}