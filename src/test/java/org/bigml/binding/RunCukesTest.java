package org.bigml.binding;

import org.junit.runner.RunWith;

import cucumber.junit.Cucumber;


@RunWith(Cucumber.class)
@Cucumber.Options(format = {"pretty", "html:target/cucumber-html-report"})
//@Cucumber.Options(format = {"pretty", "html:target/cucumber-html-report"}, features = {"src/test/resources/org/bigml/binding/createPrediction.feature"})
public class RunCukesTest {
}
