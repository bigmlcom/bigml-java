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

public class ProjectsStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(ProjectsStepdefs.class);

    CommonStepdefs commonSteps = new CommonStepdefs();

    @Autowired
    private ContextRepository context;

    @Given("^I create a project with name \"(.*)\"$")
    public void I_create_a_project_with_name(String projectName) throws AuthenticationException {
        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));
        args.put("name", projectName);

        JSONObject resource = BigMLClient.getInstance().createProject(args);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.project = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I update the project with \"(.*)\"$")
    public void I_update_the_project_with(String args)
            throws Throwable {
        if (args.equals("{}")) {
            assertTrue("No update params. Continue", true);
        } else {
            String projectId = (String) context.project.get("resource");
            JSONObject resource = BigMLClient.getInstance().updateProject(
                    projectId, args);
            context.status = (Integer) resource.get("code");
            context.location = (String) resource.get("location");
            context.project = (JSONObject) resource.get("object");
            commonSteps
                    .the_resource_has_been_updated_with_status(context.status);
        }
    }

    @Given("^I check the project name \"(.*)\"$")
    public void I_check_the_project_name(String expectedName)
            throws Throwable {
        assertEquals(expectedName, context.project.get("name"));
    }

    @Given("^I wait until the project status code is either (\\d) or (\\d) less than (\\d+)")
    public void I_wait_until_project_status_code_is(int code1, int code2,
                                                   int secs) throws AuthenticationException {
        Long code = (Long) context.project.get("code");
        GregorianCalendar start = new GregorianCalendar();
        start.add(Calendar.SECOND, secs);
        Date end = start.getTime();
        while (code.intValue() != code1 && code.intValue() != code2) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            assertTrue("Time exceded ", end.after(new Date()));
            I_get_the_project((String) context.project.get("resource"));
            code = (Long) context.project.get("code");
        }
        assertEquals(code1, code.intValue());
    }

    @Given("^I wait until the project is ready less than (\\d+) secs$")
    public void I_wait_until_the_project_is_ready_less_than_secs(int secs)
            throws AuthenticationException {
        I_wait_until_project_status_code_is(AbstractResource.HTTP_CREATED,
                AbstractResource.HTTP_NOT_FOUND, secs);
    }

    @Given("^I get the project \"(.*)\"")
    public void I_get_the_project(String projectId)
            throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().getProject(projectId);
        Integer code = (Integer) resource.get("code");
        assertEquals(AbstractResource.HTTP_OK, code.intValue());
        context.project = (JSONObject) resource.get("object");
    }

    @Given("^I store the project id in a list$")
    public void I_store_the_project_id_in_a_list()
            throws AuthenticationException {
        if (null == context.projects) {
            context.projects = new JSONArray();
        }

        assertNotNull("No project available in the context", context.projects);
        context.projects.add(context.project.get("resource"));
        context.project = null;
    }

    @When("the project\'s status is FINISHED$")
    public void project_status_finished() {
        Long code = (Long) ((JSONObject) context.project.get("status"))
                .get("code");
        assertEquals(AbstractResource.FINISHED, code.intValue());
    }

    @When("I delete the project$")
    public void i_delete_the_project() throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().deleteProject(context.project);
        context.status = (Integer) resource.get("code");
        assertTrue(context.status == AbstractResource.HTTP_NO_CONTENT);
        context.project = null;
    }

}