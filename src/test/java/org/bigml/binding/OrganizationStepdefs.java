package org.bigml.binding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cucumber.annotation.After;
import cucumber.annotation.Before;

public class OrganizationStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(OrganizationStepdefs.class);
    
    @Autowired
    CommonStepdefs commonSteps;
    
    @Autowired
    private ContextRepository context;

    @Before("@beforeOrganizationScenario")
    public void beforeOrganizationTest() throws Throwable {
    	String organizationId = System.getProperty("BIGML_ORGANIZATION");
        if (organizationId == null)
        	organizationId = System.getenv("BIGML_ORGANIZATION");
    	
        if (organizationId == null)
        	throw new Exception("You need to set BIGML_ORGANIZATION to " + 
        			"an organization ID in your environment variables " +
        			"to run this test.");
        
        BigMLClient.getInstance(null, null, null, organizationId, null);
        
        commonSteps.I_create_a_resource_with_("project", "{\"name\": \"my new project\"}");
        String projectId = (String) context.project.get("resource");
        
        BigMLClient.resetInstance();
        BigMLClient.getInstance(null, null, projectId, organizationId, null);
    }

    @After("@afterOganizationScenario")
    public void afterOrganizationTest() throws Throwable {
    	BigMLClient.resetInstance();
    }
}
