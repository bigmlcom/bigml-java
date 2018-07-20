package org.bigml.binding;

import org.springframework.beans.factory.annotation.Autowired;

import cucumber.annotation.After;
import cucumber.annotation.Before;

public class BeforeSteps {


    @Autowired
    private ContextRepository contextRepository;

    @Before
    public void beforeScenario() throws AuthenticationException {
        contextRepository = new ContextRepository();
    }

    @After
    public void afterScenario() throws AuthenticationException {
    }

}
