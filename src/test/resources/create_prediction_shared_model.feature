Feature: Create Predictions from shared Model
    In order to create a prediction from a shared model
    I need to create a shared model

    Scenario Outline: Successfully creating a prediction using a public model:
        Given that I use production mode with seed="<seed>"
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I add the unitTest tag to the data source waiting less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a model
        And I wait until the model is ready less than <time_3> secs
        And I make the model shared
        And I wait until the model is ready less than <time_3> secs
        And I get the model sharing info
        And I check the model status using the model's shared url
        And I check the model status using the model's shared key
        And I create a local model
        Then the local prediction by name=<by_name> for "<data_input>" is "<prediction>"
        Then delete test data

        Examples:
        | data                | seed      | time_1  | time_2 | time_3 | by_name    | data_input    | prediction  |
        | data/iris.csv | BigML |  10      | 10     | 10     | true     | {"petal width": 0.5} | Iris-setosa |