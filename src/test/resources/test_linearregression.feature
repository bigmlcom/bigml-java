@linearregression
Feature: Linear regression

	Scenario Outline: Successfully creating a linear regression from a dataset:
        Given I provision a dataset from "<data>" file
        And I create a linearregression from a dataset
        And I wait until the linearregression is ready less than <time_1> secs
        And I update the linearregression name to "<linearregression_name>"
        When I wait until the linearregression is ready less than <time_2> secs
        Then the linearregression name is "<linearregression_name>"

        Examples:
        | data  |  time_1  | time_2 | linearregression_name |
        | data/grades.csv | 200	| 200	| my new linear regression name |


	Scenario Outline: Successfully creating a prediction from linear regression:
        Given I provision a dataset from "<data>" file
        And I create a linearregression from a dataset
        And I wait until the linearregression is ready less than <time_1> secs
        When I create a linearregression prediction for "<data_input>"
        Then the linearregression prediction is "<prediction>"

        Examples:
        | data  |  time_1  | time_2 | data_input | prediction |
        | data/grades.csv | 200	| 100	| {"000000": 0.5, "000001": 1, "000002": 1, "000003": 1} | 2.27312	|
