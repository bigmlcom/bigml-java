package org.bigml.binding;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import cucumber.annotation.After;
import cucumber.annotation.Before;

public class BeforeSteps {
	
	@Autowired
	private ContextRepository context;
	
    @Before
    public void beforeScenario() throws AuthenticationException {
    	
    	// Check test project
    	if (context.testProject == null) {
			JSONObject listProjects = (JSONObject) context.api.listProjects(
	        	";tags__in=unitTestProject");
			JSONArray projects = (JSONArray) listProjects.get("objects");
			context.testProject = (String) ((JSONObject) projects.get(0)).get("resource");
		}
    	
    }

    @After
    public void afterScenario() throws AuthenticationException {
    	context.datasets = null;
    	context.models = null;
    }

}
