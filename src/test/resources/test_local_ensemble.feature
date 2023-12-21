@localensemble
Feature: LocalEnsemble

	Scenario Outline: Successfully comparing predictions with ensembles
        Given I provision a dataset from "<data>" file
        And I create an ensemble of <number_of_models> models
        And I wait until the ensemble is ready less than <time_1> secs
        And I create a local ensemble
        And I create a local prediction with ensemble for "<data_input>"
        Then the local ensemble prediction is "<prediction>"

        Examples:
        | data  | time_1  | number_of_models | data_input | prediction    |
        | data/grades.csv | 120  | 2 	| {} | 69.0934       |


	Scenario Outline: Successfully comparing remote and local predictions with raw date input
        Given I provision a dataset from "<data>" file
        And I create an ensemble
        And I wait until the ensemble is ready less than <time_1> secs
        And I create a local ensemble
        When I create a prediction with ensemble for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        And I create a local prediction with ensemble for "<data_input>"
        Then the local ensemble prediction is "<prediction>"

        Examples:
        | data            | time_1 | data_input                             | objective | prediction  |
        | data/dates2.csv | 30     | {"time-1": "1910-05-08T19:10:23.106", "cat-0":"cat2"}  | 000002    | -0.11052    |
		| data/dates2.csv | 30     | {"time-1": "1920-06-30T20:21:20.320", "cat-0":"cat1"}  | 000002    | 0.79179    |
        | data/dates2.csv | 30     | {"time-1": "1932-01-30T19:24:11.450",  "cat-0":"cat2"}  | 000002    | -1.00834    |
        | data/dates2.csv | 30     | {"time-1": "1950-11-06T05:34:05.252", "cat-0":"cat1"}  | 000002    | -0.14442    |
        | data/dates2.csv | 30     | {"time-1": "1969-7-14 17:36", "cat-0":"cat2"}  | 000002    | -0.05469    |
        | data/dates2.csv | 30     | {"time-1": "2001-01-05T23:04:04.693", "cat-0":"cat2"}  | 000002    | -0.23387    |
        | data/dates2.csv | 30     | {"time-1": "1969-W29-1T17:36:39Z", "cat-0":"cat1"}  | 000002    | -0.05469    |
        | data/dates2.csv | 30     | {"time-1": "Mon Jul 14 17:36 +0000 1969", "cat-0":"cat1"}  | 000002    | -0.05469    |
