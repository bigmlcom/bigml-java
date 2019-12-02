Feature: Ensembles REST api

    Scenario Outline: Successfully creating a prediction from an ensemble:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create an ensemble of <number_of_models> models
        And I wait until the ensemble is ready less than <time_3> secs
        When I create a prediction with ensemble for "<data_input>"
        And I wait until the prediction is ready less than <time_4> secs
        Then the prediction for "<objective>" is "<prediction>"

				Examples:
        | data             | time_1  | time_2 | time_3 | time_4 | number_of_models | data_input    | objective | prediction  |
        | data/iris.csv |  10      | 10     | 100     | 20     | 5                | {"petal width": 0.5} | 000004    | Iris-versicolor |
        | data/iris_sp_chars.csv | 10 | 10 | 100 | 20 | 5 | {"pétal&width\u0000": 0.5} | 000004 | Iris-versicolor |


  	Scenario Outline: Successfully creating a prediction from an ensemble:
		    Given I create a data source uploading a "<data>" file
		    And I wait until the source is ready less than <time_1> secs
		    And I create a dataset
		    And I wait until the dataset is ready less than <time_2> secs
		    And I create an ensemble of <number_of_models> models
		    And I wait until the ensemble is ready less than <time_3> secs
		    When I create a prediction with ensemble for "<data_input>"
		    And I wait until the prediction is ready less than <time_4> secs
		    Then the numerical prediction for "<objective>" is <prediction>

				Examples:
		      | data             | time_1  | time_2 | time_3 | time_4 | number_of_models | data_input    | objective | prediction  |
		    	| data/grades.csv | 30 | 30 | 150 | 30 | 30 |{"Assignment": 81.22, "Tutorial": 91.95, "Midterm": 79.38, "TakeHome": 105.93} | 000005 | 89.165 |
		    	| data/grades.csv | 30 | 30 | 150 | 30 | 30 |{"Assignment": 97.33, "Tutorial": 106.74, "Midterm": 76.88, "TakeHome": 108.89} | 000005 | 71.938 |
