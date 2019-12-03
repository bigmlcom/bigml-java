Feature: Evaluation REST api

     Scenario Outline: Successfully creating an evaluation:
       Given I provision a dataset from "<data>" file
        And I create a model
        And I wait until the model is ready less than <time_1> secs
        When I create an evaluation for the model with the dataset
        And I wait until the evaluation is ready less than <time_2> secs
        Then the measured "<measure>" is <value>

        Examples:
        | data	| time_1  | time_2 | measure	| value  |
        | data/iris.csv | 50	| 50	| average_phi   | 1	|


    Scenario Outline: Successfully creating an evaluation for an ensemble:
        Given I provision a dataset from "<data>" file
        And I create an ensemble of <number_of_models> models
        And I wait until the ensemble is ready less than <time_1> secs
        When I create an evaluation for the ensemble with the dataset
        And I wait until the evaluation is ready less than <time_2> secs
        Then the measured "<measure>" is equals to <value>

        Examples:
        | data	| time_1  | time_2 | number_of_models | measure	| value  |
        | data/iris.csv | 100	| 100	| 5	| average_phi	| 0.97064   |


    Scenario Outline: Successfully creating an evaluation for a linear regression:
        Given I provision a dataset from "<data>" file
        And I create a linearregression from a dataset
        And I wait until the linearregression is ready less than <time_1> secs
        When I create an evaluation for the linear regression with the dataset
        And I wait until the evaluation is ready less than <time_2> secs
        Then the measured "<measure>" is equals to <value>

        Examples:
        | data	| time_1  | time_2 | tlp | measure	| value  |
        | data/iris.csv | 800	| 1000	| 5	| r_squared	| 0.95382   |
