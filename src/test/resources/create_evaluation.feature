Feature: Create Evaluations
    In order to create an evaluation
    I need to create a model and a dataset first

     Scenario Outline: Successfully creating an evaluation:
        Given that I use development mode
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a model
        And I wait until the model is ready less than <time_3> secs
        When I create an evaluation for the model with the dataset
        And I wait until the evaluation is ready less than <time_4> secs
        Then the measured "<measure>" is <value>
        Then delete test data

        Examples:
        | data             | time_1  | time_2 | time_3 | time_4 | measure       | value  |
        | data/iris.csv | 10      | 10     | 10     | 10     | average_phi   | 1      |


    Scenario Outline: Successfully creating an evaluation for an ensemble:
        Given that I use development mode
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create an ensemble of <number_of_models> models and <tlp> tlp
        And I wait until the ensemble is ready less than <time_3> secs
        When I create an evaluation for the ensemble with the dataset
        And I wait until the evaluation is ready less than <time_4> secs
        Then the measured "<measure>" is greater than <value>
        Then delete test data

        Examples:
        | data             | time_1  | time_2 | number_of_models | tlp | time_3 | time_4 | measure       | value  |
        | data/iris.csv | 10      | 10     | 5                | 1   | 60     | 10     | average_phi   | 0.8   |
