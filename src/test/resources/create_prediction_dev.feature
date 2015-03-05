Feature: Create Predictions in DEV mode
    In order to create a prediction in DEV mode
    I need to change to an API instance in DEV mode
    And I need to create a model first

    Scenario Outline: Successfully creating a prediction in DEV mode:
        Given that I use development mode with seed="<seed>"
        When I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I add the unitTest tag to the data source waiting less than <time_1> secs
        And The source has development <is_dev>
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a model
        And I wait until the model is ready less than <time_3> secs
        When I create a prediction by name=<by_name> for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"

        Examples:
          | data                |  seed  |   time_1  | time_2 | time_3 | is_dev |  by_name |  data_input    | objective | prediction  |
          | data/iris.csv | BigML |   10      | 10     | 10     | true |   true |  {"petal width": 0.5} | 000004    | Iris-setosa |
