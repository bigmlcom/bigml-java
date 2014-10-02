package org.bigml.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.bigml.binding.resources.AbstractResource;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cucumber.annotation.en.Given;

public class EnsemblesStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(EnsemblesStepdefs.class);

    CommonStepdefs commonSteps = new CommonStepdefs();

    @Autowired
    private ContextRepository context;

    @Given("^I create a ensemble$")
    public void I_create_a_ensemble() throws AuthenticationException {
        String datasetId = (String) context.dataset.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));

        JSONObject resource = BigMLClient.getInstance().createEnsemble(
                datasetId, args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.ensemble = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I create an ensemble of (\\d+) models and (\\d+) tlp$")
    public void I_create_an_ensemble_of_models_and_tlp(int numberOfModels,
            int tlp) throws Throwable {
        JSONObject args = new JSONObject();
        args.put("number_of_models", numberOfModels);
        args.put("tlp", tlp);
        args.put("sample_rate", 0.70);

        args.put("tags", Arrays.asList("unitTest"));

        String datasetId = (String) context.dataset.get("resource");
        JSONObject resource = BigMLClient.getInstance().createEnsemble(
                datasetId, args, 20, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.ensemble = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I wait until the ensemble status code is either (\\d) or (\\d) less than (\\d+)")
    public void I_wait_until_ensemble_status_code_is(int code1, int code2,
            int secs) throws AuthenticationException {
        Long code = (Long) ((JSONObject) context.ensemble.get("status"))
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
            I_get_the_ensemble((String) context.ensemble.get("resource"));
            code = (Long) ((JSONObject) context.ensemble.get("status"))
                    .get("code");
        }
        assertEquals(code1, code.intValue());
    }

    @Given("^I wait until the ensemble is ready less than (\\d+) secs$")
    public void I_wait_until_the_ensemble_is_ready_less_than_secs(int secs)
            throws AuthenticationException {
        I_wait_until_ensemble_status_code_is(AbstractResource.FINISHED,
                AbstractResource.FAULTY, secs);
    }

    @Given("^I get the ensemble \"(.*)\"")
    public void I_get_the_ensemble(String ensembleId)
            throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().getEnsemble(ensembleId);
        Integer code = (Integer) resource.get("code");
        assertEquals(AbstractResource.HTTP_OK, code.intValue());
        context.ensemble = (JSONObject) resource.get("object");
    }
}