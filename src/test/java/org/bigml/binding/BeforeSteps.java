package org.bigml.binding;

import org.springframework.beans.factory.annotation.Autowired;

import cucumber.annotation.After;
import cucumber.annotation.Before;

public class BeforeSteps {

    CommonStepdefs commonSteps = new CommonStepdefs();

    @Autowired
    private ContextRepository contextRepository;

    @Before
    public void beforeScenario() throws AuthenticationException {
        contextRepository = new ContextRepository();
    }

    @After
    public void afterScenario() throws AuthenticationException {
        // commonSteps.delete_test_data();
    }

}
