Feature: Create Predictions
    In order to create a prediction
    I need to create a model first

    Scenario Outline: Successfully creating a prediction
        Given that I use production mode with seed="<seed>"
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I add the unitTest tag to the data source waiting less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a model
        And I wait until the model is ready less than <time_3> secs
        When I create a prediction by name=<by_name> for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        And I create a ensemble
        And I wait until the ensemble is ready less than <time_3> secs
        When I create a prediction with ensemble by name=<by_name> for "<data_input>"
        Then the prediction with ensemble for "<objective>" is "<predictionEnsemble>"
        And I create a evaluation
        And I wait until the evaluation is ready less than <time_4> secs
        Then test listing

    Examples:
      | data	| seed      | time_1  | time_2 | time_3 | time_4 | by_name    | data_input    | objective | prediction  | predictionEnsemble  |
      | data/iris.csv | BigML |  15      | 15     | 60     | 15     | true | {"petal width": 0.5} | 000004    | Iris-setosa | Iris-versicolor |
