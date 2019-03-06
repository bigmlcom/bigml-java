Feature: Testing Deepnet REST api calls
    In order to create an deepnet
    I need to create a dataset first
	
	Scenario Outline: Successfully comparing predictions for models with operating point
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a model
        And I wait until the model is ready less than <time_3> secs
        And I create a local model
        When I create a prediction with model with operating point "<operating_point>" for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        When I create a local prediction with model with operating point "<operating_point>" for "<data_input>"
        Then the local model prediction is "<prediction>"
        Then delete test data

        Examples:
        | data	| time_1  | time_2 | time_3 | data_input | objective	| prediction	| operating_point	|
        | data/iris.csv | 50      | 50     | 30000	| {"petal width": 4} | 000004	| Iris-setosa	| {"kind": "probability", "threshold": 0.1, "positive_class": "Iris-setosa"}	|
		| data/iris.csv | 50      | 50     | 30000	| {"petal width": 4} | 000004	| Iris-versicolor	| {"kind": "probability", "threshold": 0.9, "positive_class": "Iris-setosa"}	|
		| data/iris.csv | 50      | 50     | 30000	| {"sepal length": 4.1, "sepal width": 2.4} | 000004	| Iris-setosa	| {"kind": "confidence", "threshold": 0.1, "positive_class": "Iris-setosa"}	|	
		| data/iris.csv | 50      | 50     | 30000	| {"sepal length": 4.1, "sepal width": 2.4}| 000004	| Iris-versicolor	| {"kind": "confidence", "threshold": 0.9, "positive_class": "Iris-setosa"}	|
	
	
	Scenario Outline: Successfully comparing predictions for models with operating kind
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a model
        And I wait until the model is ready less than <time_3> secs
        And I create a local model
        When I create a prediction with model with operating kind "<operating_kind>" for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        When I create a local prediction with model with operating kind "<operating_kind>" for "<data_input>"
        Then the local model prediction is "<prediction>"
        Then delete test data

        Examples:
        | data	| time_1  | time_2 | time_3 | data_input | objective	| prediction	| operating_kind	|
        | data/iris.csv | 50      | 50     | 30000	| {"petal length": 2.46, "sepal length": 5} | 000004	| Iris-versicolor	| probability	|
        | data/iris.csv | 50      | 50     | 30000	| {"petal length": 2.46, "sepal length": 5} | 000004	| Iris-versicolor	| confidence	|
		| data/iris.csv | 50      | 50     | 30000	| {"petal length": 2} | 000004	| Iris-setosa	| probability	|
        | data/iris.csv | 50      | 50     | 30000	| {"petal length": 2} | 000004	| Iris-setosa	| confidence	|
	
	
	Scenario Outline: Successfully comparing predictions for deepnets
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a deepnet with objective "<objective>" and params "<params>"
        And I wait until the deepnet is ready less than <time_3> secs
        And I create a local deepnet
        When I create a deepnet prediction for "<data_input>"
        Then the deepnet prediction for objective "<objective>" is "<prediction>"
        And I create a local deepnet prediction for "<data_input>"
        Then the local deepnet prediction is "<prediction>"
        Then delete test data

        Examples:
        | data  | time_1  | time_2 | time_3 | data_input | objective    | prediction    | params    |
        | data/iris.csv | 50      | 50     | 30000  | {"petal width": 4} | 000004   | Iris-virginica    | {}    |
        | data/iris.csv | 50      | 50     | 30000  | {"sepal length": 4.1, "sepal width": 2.4} | 000004    | Iris-setosa   | {}    |
        | data/iris_missing2.csv | 50      | 50     | 30000 | {} | 000004   | Iris-setosa   | {}    |
        | data/grades.csv | 50      | 50     | 30000    | {} | 000005   | 42.15473  | {}    |
        | data/spam.csv | 50      | 50     | 30000  | {} | 000000   | ham   | {}    |
        | data/diabetes.csv | 50      | 50     | 30000  | {} | 000008   | false | {"search": true, "number_of_model_candidates": 10, "max_training_time": 600}  |
        | data/diabetes.csv | 50      | 50     | 30000  | {} | 000008   | false | {"learn_residuals": true, "number_of_model_candidates": 10, "max_training_time": 600} |

	
	Scenario Outline: Successfully comparing predictions for deepnets with operating point
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a deepnet with objective "<objective>" and params "<params>"
        And I wait until the deepnet is ready less than <time_3> secs
        And I create a local deepnet
        When I create a prediction with deepnet with operating point "<operating_point>" for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        When I create a local prediction with deepnet with operating point "<operating_point>" for "<data_input>"
        Then the local deepnet prediction is "<prediction>"
        Then delete test data

        Examples:
        | data	| time_1  | time_2 | time_3 | data_input | objective	| prediction	| params	| operating_point	|
        | data/iris.csv | 50      | 50     | 30000	| {"petal width": 4} | 000004	| Iris-versicolor	| {}	| {"kind": "probability", "threshold": 1, "positive_class": "Iris-virginica"}	|
		
	
	Scenario Outline: Successfully comparing predictions for deepnets with operating kind
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a deepnet with objective "<objective>" and params "<params>"
        And I wait until the deepnet is ready less than <time_3> secs
        And I create a local deepnet
        When I create a prediction with deepnet with operating kind "<operating_kind>" for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        When I create a local prediction with deepnet with operating kind "<operating_kind>" for "<data_input>"
        Then the local deepnet prediction is "<prediction>"
        Then delete test data

        Examples:
        | data  | time_1  | time_2 | time_3 | data_input | objective    | prediction    | params    | operating_kind    |
        | data/iris.csv | 50      | 50     | 30000  | {"petal length": 2.46} | 000004   | Iris-setosa   | {}    | probability   |
        | data/iris.csv | 50      | 50     | 30000  | {"petal length": 2} | 000004  | Iris-setosa   | {}    | probability   |
        
        
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
	   
	
     Scenario Outline: Successfully comparing predictions for logistic regressions with operating kind
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a logisticregression from a dataset
        And I wait until the logisticregression is ready less than <time_3> secs
        And I create a local logisticregression
        When I create a prediction with logisticregression with operating kind "<operating_kind>" for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        When I create a local prediction with logisticregression with operating kind "<operating_kind>" for "<data_input>"
        Then the local logisticregression prediction is "<prediction>"
        Then delete test data

        Examples:
        | data	| time_1  | time_2 | time_3 | data_input | objective	| prediction	| operating_kind	|
        | data/iris.csv | 50      | 50     | 30000	| {"petal length": 5} | 000004	| Iris-versicolor	| probability	|
        | data/iris.csv | 50      | 50     | 30000	| {"petal length": 2} | 000004	| Iris-setosa	| probability	|
