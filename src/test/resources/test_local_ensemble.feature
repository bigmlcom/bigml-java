Feature: LocalEnsemble
    
    Scenario Outline: Successfully creating a local prediction from an Ensemble:
	    Given I create a data source uploading a "<data>" file
	    And I wait until the source is ready less than <time_1> secs
	    And I create a dataset
	    And I wait until the dataset is ready less than <time_2> secs
	    And I create an ensemble of <number_of_models> models
	    And I wait until the ensemble is ready less than <time_3> secs
	    And I create a local ensemble
	    When the local ensemble prediction for "<data_input>" is "<prediction>" with confidence <confidence>
	    #And the local probabilities are "<probabilities>"
	    Then delete test data
	
	    Examples:
	      | data             |  time_1  | time_2 | time_3 | number_of_models |  data_input    |prediction  | confidence |
	      | data/iris.csv |  50      | 50     | 50     | 5               | {"petal width": 0.5} | Iris-versicolor | 0.3687 | ["0.3403","0.4150","0.2447"]  |


	  Scenario Outline: Successfully obtaining field importance from an Ensemble:
	    Given I create a data source uploading a "<data>" file
	    And I wait until the source is ready less than <time_1> secs
	    And I create a dataset
	    And I wait until the dataset is ready less than <time_2> secs
	    And I create a model with "<parms1>"
	    And I wait until the model is ready less than <time_3> secs
	    And I create a model with "<parms2>"
	    And I wait until the model is ready less than <time_4> secs
	    And I create a model with "<parms3>"
	    And I wait until the model is ready less than <time_5> secs
	    When I create a local Ensemble with the last <number_of_models> models
	    Then the field importance text is "<field_importance>"
	    Then delete test data
	
	    Examples:
	      | data             |  time_1  | time_2 |parms1 | time_3 |parms2 | time_4 |parms3| time_5 |number_of_models |field_importance |
	      | data/iris.csv |  50      | 50     |{"input_fields": ["000000", "000001","000003", "000004"]} |20      |{"input_fields": ["000000", "000001","000002", "000004"]} | 20     |{"input_fields": ["000000", "000001","000002", "000003", "000004"]} | 20   | 3 |[["000002", 0.5269933333333333], ["000003", 0.38936], ["000000", 0.04662333333333333], ["000001", 0.037026666666666666]] |
	

	  Scenario Outline: Successfully creating a local prediction from an Ensemble adding confidence:
	    Given I create a data source uploading a "<data>" file
	    And I wait until the source is ready less than <time_1> secs
	    And I create a dataset
	    And I wait until the dataset is ready less than <time_2> secs
	    And I create an ensemble of <number_of_models> models
	    And I wait until the ensemble is ready less than <time_3> secs
	    And I create a local ensemble
	    #When I create a local ensemble prediction for "<data_input>" in JSON adding confidence
	    When the local ensemble prediction for "<data_input>" is "<prediction>" with confidence <confidence>
	    Then delete test data
	
	    Examples:
	      | data            |  time_1  | time_2 | time_3 | number_of_models |  data_input           |prediction       | confidence  |
	      | data/iris.csv   |  50      | 50     | 50     | 5                | {"petal width": 0.5}  | Iris-versicolor |  0.3687     |


	  Scenario Outline: Successfully creating a local prediction from an Ensemble:
	    Given I create a data source uploading a "<data>" file
	    And I wait until the source is ready less than <time_1> secs
	    And I create a dataset
	    And I wait until the dataset is ready less than <time_2> secs
	    And I create an ensemble of <number_of_models> models
	    And I wait until the ensemble is ready less than <time_3> secs
	    And I create a local ensemble
	    When the local ensemble prediction using median with confidence for "<data_input>" is "<prediction>"
	    Then delete test data
	
	    Examples:
	      | data            |  time_1  | time_2 | time_3 | number_of_models |  data_input    |prediction  |
	      | data/grades.csv   |  50      | 50     | 50     | 2              | {}             | 69.0934    |


	  Scenario Outline: Successfully creating a local prediction from an Ensemble with max models:
	    Given I create a data source uploading a "<data>" file
	    And I wait until the source is ready less than <time_1> secs
	    And I create a dataset
	    And I wait until the dataset is ready less than <time_2> secs
	    And I create an ensemble of <number_of_models> models
	    And I wait until the ensemble is ready less than <time_3> secs
	    And I create a local ensemble with max models <max_models>
	    When the local ensemble prediction for "<data_input>" is "<prediction>"
	    Then delete test data
	
	    Examples:
	      | data            |  time_1  | time_2 | time_3 | time_4 | number_of_models | max_models |  data_input    |prediction  |
	      | data/iris.csv   |  50      | 50     | 50     | 20     | 5                | 2          | {"petal width": 0.5} | Iris-versicolor |
      
    Scenario Outline: Successfully comparing predictions for ensembles
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create an ensemble with "<params>"
        And I wait until the ensemble is ready less than <time_3> secs
        And I create a local ensemble
				When I create a prediction with ensemble for "<data_input>"
				Then the prediction for "<objective>" is "<prediction>"
				When the local ensemble prediction for "<data_input>" is "<prediction>"
				Then delete test data
		
        Examples:
          | data	| time_1  | time_2 | time_3 | params | data_input | objective  | prediction |
          | data/iris_unbalanced.csv | 30      | 30     | 120     | {"boosting": {"iterations": 5}, "number_of_models": 5} |{"petal width": 4}  | 000004   | Iris-virginica   |
          | data/grades.csv | 30      | 30     | 120     | {"boosting": {"iterations": 5}, "number_of_models": 5} |{"Midterm": 20}  | 000005   | 61.61036   |
          
     
     Scenario Outline: Successfully comparing predictions for ensembles with proportional missing strategy
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create an ensemble with "<params>"
        And I wait until the ensemble is ready less than <time_3> secs
        And I create a local ensemble
        When I create a proportional missing strategy prediction with ensemble with "<options>" for "<data_input>"
				Then the prediction for "<objective>" is "<prediction>"
				And the confidence for the prediction is <confidence>
				And I create a proportional missing strategy local prediction with ensemble with "<options>" for "<data_input>"
				Then the local ensemble prediction is "<prediction>"
        And the local ensemble confidence is <confidence>
				Then delete test data
		
        Examples:
          | data	| time_1  | time_2 | time_3 | params | data_input | objective  | prediction | options	| confidence	|
          | data/iris.csv | 30      | 30     | 120     | {"boosting": {"iterations": 5}} | {}  | 000004   | Iris-virginica   | {}	| 0.33784	|
          | data/iris.csv | 30      | 30     | 120     | {"number_of_models": 5} |	{}  | 000004   | Iris-versicolor   | {"operating_kind": "confidence"}	| 0.2923	|
          | data/grades.csv | 30      | 30     | 120     | {"number_of_models": 5} | {}  | 000005   | 70.50579   | {}	| 30.7161	|
          | data/grades.csv | 30      | 30     | 120     | {"number_of_models": 5} | {"Midterm": 20}  | 000005   | 54.82214   | {"operating_kind": "confidence"}	| 25.89672	|
          | data/grades.csv | 30      | 30     | 120     | {"number_of_models": 5} | {"Midterm": 20}  | 000005   | 45.4573   | {}	| 29.58403	|
          | data/grades.csv | 30      | 30     | 120     | {"number_of_models": 5} | {"Midterm": 20, "Tutorial": 90, "TakeHome": 100}  | 000005   | 42.814   | {}	| 31.51804	|
          
          
    Scenario Outline: Successfully comparing predictions for ensembles with operating point
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create an ensemble
        And I wait until the ensemble is ready less than <time_3> secs
        And I create a local ensemble
        When I create a prediction with ensemble with operating point "<operating_point>" for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        When I create a local prediction with ensemble with operating point "<operating_point>" for "<data_input>"
        Then the local ensemble prediction is "<prediction>"
        Then delete test data

        Examples:
        | data	| time_1  | time_2 | time_3 | data_input | objective	| prediction	| operating_point	|
        | data/iris.csv | 50      | 50     | 30000	| {"petal width": 4} | 000004	| Iris-setosa	| {"kind": "probability", "threshold": 0.1, "positive_class": "Iris-setosa"}	|
				| data/iris.csv | 50      | 50     | 30000	| {"petal width": 4} | 000004	| Iris-virginica	| {"kind": "probability", "threshold": 0.9, "positive_class": "Iris-setosa"}	|
				| data/iris.csv | 50      | 50     | 30000	| {"sepal length": 4.1, "sepal width": 2.4} | 000004	| Iris-setosa	| {"kind": "confidence", "threshold": 0.1, "positive_class": "Iris-setosa"}	|	
				| data/iris.csv | 50      | 50     | 30000	| {"sepal length": 4.1, "sepal width": 2.4}| 000004	| Iris-versicolor	| {"kind": "confidence", "threshold": 0.9, "positive_class": "Iris-setosa"}	|
	
	
		Scenario Outline: Successfully comparing predictions for ensembles with operating kind
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create an ensemble
        And I wait until the ensemble is ready less than <time_3> secs
        And I create a local ensemble
        When I create a prediction with ensemble with operating kind "<operating_kind>" for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        When I create a local prediction with ensemble with operating kind "<operating_kind>" for "<data_input>"
        Then the local ensemble prediction is "<prediction>"
        Then delete test data

        Examples:
        | data  | time_1  | time_2 | time_3 | data_input | objective    | prediction    | operating_kind    |
        | data/iris.csv | 50      | 50     | 30000  | {"petal length": 2.46} | 000004   | Iris-versicolor   | probability   |
        | data/iris.csv | 50      | 50     | 30000  | {"petal length": 2} | 000004  | Iris-setosa   | probability   |      
        | data/iris.csv | 50      | 50     | 30000  | {"petal length": 2.46} | 000004   | Iris-versicolor   | confidence   |
        | data/iris.csv | 50      | 50     | 30000  | {"petal length": 2} | 000004  | Iris-setosa   | confidence   |
        | data/iris.csv | 50      | 50     | 30000  | {"petal length": 2.46} | 000004   | Iris-versicolor   | votes   |
        | data/iris.csv | 50      | 50     | 30000  | {"petal length": 2} | 000004  | Iris-setosa   | votes   |
 