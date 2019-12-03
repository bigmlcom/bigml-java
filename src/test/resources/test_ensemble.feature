Feature: Ensembles REST api

    Scenario Outline: Successfully creating a prediction from an ensemble:
        Given I provision a dataset from "<data>" file
        And I create an ensemble of <number_of_models> models
        And I wait until the ensemble is ready less than <time_1> secs
        When I create a prediction with ensemble for "<data_input>"
        And I wait until the prediction is ready less than <time_2> secs
        Then the prediction for "<objective>" is "<prediction>"

				Examples:
        | data	| time_1  | time_2 | number_of_models | data_input    | objective | prediction  |
        | data/iris.csv |  100	| 100	| 5	| {"petal width": 0.5} | 000004    | Iris-versicolor |
        | data/iris_sp_chars.csv | 100 | 100 | 5 | {"pétal&width\u0000": 0.5} | 000004 | Iris-versicolor |


  	Scenario Outline: Successfully creating a prediction from an ensemble:
		    Given I provision a dataset from "<data>" file
		    And I create an ensemble of <number_of_models> models
		    And I wait until the ensemble is ready less than <time_1> secs
		    When I create a prediction with ensemble for "<data_input>"
		    And I wait until the prediction is ready less than <time_2> secs
		    Then the numerical prediction for "<objective>" is <prediction>

				Examples:
		      | data             | time_1  | time_2 | number_of_models | data_input    | objective | prediction  |
		    	| data/grades.csv | 150 | 150 | 30 |{"Assignment": 81.22, "Tutorial": 91.95, "Midterm": 79.38, "TakeHome": 105.93} | 000005 | 89.165 |
		    	| data/grades.csv | 150 | 150 | 30 |{"Assignment": 97.33, "Tutorial": 106.74, "Midterm": 76.88, "TakeHome": 108.89} | 000005 | 71.938 |
