Feature: LocalDeepnet
	
		Scenario Outline: Successfully comparing predictions for deepnets
        Given I provision a dataset from "<data>" file
        And I create a deepnet with objective "<objective>" and params "<params>"
        And I wait until the deepnet is ready less than <time_1> secs
        And I create a local deepnet
        When I create a deepnet prediction for "<data_input>"
        Then the deepnet prediction for objective "<objective>" is "<prediction>"
        And I create a local deepnet prediction for "<data_input>"
        Then the local deepnet prediction is "<prediction>"

        Examples:
        | data  | time_1  | data_input | objective    | prediction    | params    |
        | data/iris.csv | 30000  | {"petal width": 4} | 000004   | Iris-virginica    | {}    |
        | data/iris.csv | 30000  | {"sepal length": 4.1, "sepal width": 2.4} | 000004    | Iris-setosa   | {}    |
        | data/iris_missing2.csv | 30000 | {} | 000004   | Iris-setosa   | {}    |
        | data/grades.csv | 30000    | {} | 000005   | 42.15473  | {}    |
        | data/spam.csv | 30000  | {} | 000000   | ham   | {}    |
        | data/diabetes.csv | 30000  | {} | 000008   | false | {"search": true, "number_of_model_candidates": 10, "max_training_time": 600}  |
        | data/diabetes.csv | 30000  | {} | 000008   | false | {"learn_residuals": true, "number_of_model_candidates": 10, "max_training_time": 600} |

	
		Scenario Outline: Successfully comparing predictions for deepnets with operating point
        Given I provision a dataset from "<data>" file
        And I create a deepnet with objective "<objective>" and params "<params>"
        And I wait until the deepnet is ready less than <time_1> secs
        And I create a local deepnet
        When I create a prediction with deepnet with operating point "<operating_point>" for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        When I create a local prediction with deepnet with operating point "<operating_point>" for "<data_input>"
        Then the local deepnet prediction is "<prediction>"

        Examples:
        | data	| time_1  | data_input | objective	| prediction	| params	| operating_point	|
        | data/iris.csv | 30000 | {"petal width": 4} | 000004	| Iris-versicolor	| {}	| {"kind": "probability", "threshold": 1, "positive_class": "Iris-virginica"}	|
		
	
		Scenario Outline: Successfully comparing predictions for deepnets with operating kind
        Given I provision a dataset from "<data>" file
        And I create a deepnet with objective "<objective>" and params "<params>"
        And I wait until the deepnet is ready less than <time_1> secs
        And I create a local deepnet
        When I create a prediction with deepnet with operating kind "<operating_kind>" for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        When I create a local prediction with deepnet with operating kind "<operating_kind>" for "<data_input>"
        Then the local deepnet prediction is "<prediction>"

        Examples:
        | data  | time_1  | data_input | objective    | prediction    | params    | operating_kind    |
        | data/iris.csv | 30000  | {"petal length": 2.46} | 000004   | Iris-setosa   | {}    | probability   |
        | data/iris.csv | 30000  | {"petal length": 2} | 000004  | Iris-setosa   | {}    | probability   |
