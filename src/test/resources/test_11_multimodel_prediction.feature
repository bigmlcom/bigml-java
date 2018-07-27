Feature: Create Predictions from Multi Models
    In order to create a prediction from a multi model
    I need to create a multi model first

    Scenario Outline: Successfully creating a prediction from a multi model:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a model with "<params>"
        And I wait until the model is ready less than <time_3> secs
        And I create a model with "<params>"
        And I wait until the model is ready less than <time_3> secs
        And I create a model with "<params>"
        And I wait until the model is ready less than <time_3> secs
        And I retrieve a list of remote models tagged with "<tag>"
        And I create a local multi model
        Then the local multi prediction for "<data_input>" is "<prediction>"
        Then delete test data

        Examples:
        | data             | time_1  | time_2 | time_3 | params                         |  tag  |  data_input    | prediction  |
        | data/iris.csv |  10      | 10     | 10     | {"tags":["mytag"]} | mytag |  {"petal width": 0.5} | Iris-setosa |


    Scenario Outline: Successfully creating a local batch prediction from a multi model:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a model with "<params>"
        And I wait until the model is ready less than <time_3> secs
        And I create a model with "<params>"
        And I wait until the model is ready less than <time_3> secs
        And I create a model with "<params>"
        And I wait until the model is ready less than <time_3> secs
        And I retrieve a list of remote models tagged with "<tag>"
        And I create a local multi model
        Then I create a batch multimodel prediction for "<data_inputs>" and predictions "<predictions>"
        Then delete test data

        Examples:
            | data          | time_1  | time_2 | time_3 | params             |  tag  |  data_inputs                                                   | predictions                       |
            | data/iris.csv |  10     | 10     | 10     | {"tags":["mytag"]} | mytag |  [{"petal width": 0.5}, {"petal length": 6, "petal width": 2}] | ["Iris-setosa", "Iris-virginica"] |
