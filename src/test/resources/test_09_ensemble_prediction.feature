Feature: Create Predictions from Ensembles
    In order to create a prediction from an ensemble
    I need to create an ensemble first

    Scenario Outline: Successfully creating a prediction from an ensemble:
        Given that I use production mode with seed="<seed>"
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I add the unitTest tag to the data source waiting less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create an ensemble of <number_of_models> models and <tlp> tlp
        And I wait until the ensemble is ready less than <time_3> secs
        When I create a prediction with ensemble for "<data_input>"
        And I wait until the prediction is ready less than <time_4> secs
        Then the prediction for "<objective>" is "<prediction>"
        Then delete test data


	Examples:
        | data             | seed      | time_1  | time_2 | time_3 | time_4 | number_of_models | tlp   | data_input    | objective | prediction  |
        | data/iris.csv | BigML |  10      | 10     | 100     | 20     | 5                | 1     | {"petal width": 0.5} | 000004    | Iris-versicolor |
        | data/iris_sp_chars.csv | BigML | 10 | 10 | 100 | 20 | 5 | 1 | {"pétal&width\u0000": 0.5} | 000004 | Iris-versicolor |


  Scenario Outline: Successfully creating a prediction from an ensemble:
    Given that I use development mode with seed="<seed>"
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I add the unitTest tag to the data source waiting less than <time_1> secs
    And I create a dataset
    And I wait until the dataset is ready less than <time_2> secs
    And I create an ensemble of <number_of_models> models and <tlp> tlp
    And I wait until the ensemble is ready less than <time_3> secs
    When I create a prediction with ensemble for "<data_input>"
    And I wait until the prediction is ready less than <time_4> secs
    Then the numerical prediction for "<objective>" is <prediction>
    Then delete test data

    | data/grades.csv | BigML | 10 | 10 | 150 | 20 | 10 | 1 | {"Assignment": 81.22, "Tutorial": 91.95, "Midterm": 79.38, "TakeHome": 105.93} | 000005 | 84.556 |
    | data/grades.csv | BigML | 10 | 10 | 150 | 20 | 10 | 1 | {"Assignment": 97.33, "Tutorial": 106.74, "Midterm": 76.88, "TakeHome": 108.89} | 000005 | 73.13558 |
