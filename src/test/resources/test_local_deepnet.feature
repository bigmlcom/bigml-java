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
        | data/iris.csv | 30000  | {"sepal length": 4.1, "sepal width": 2.4} | 000004    | Iris-versicolor   | {}    |
        | data/iris_missing2.csv | 30000 | {} | 000004   | Iris-versicolor   | {}    |
        | data/grades.csv | 30000    | {} | 000005   | 55.65609  | {}    |
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
        | data  | time_1  | data_input | objective  | prediction  | params  | operating_point |
        | data/iris.csv | 30000 | {"petal width": 4} | 000004 | Iris-setosa | {}  | {"kind": "probability", "threshold": 1, "positive_class": "Iris-virginica"} |


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


    Scenario Outline: Successfully comparing remote and local predictions with raw date input for deepnet
        Given I provision a dataset from "<data>" file
        And I create a deepnet
        And I wait until the deepnet is ready less than <time_1> secs
        And I create a local deepnet
        When I create a deepnet prediction for "<data_input>"
        Then the deepnet prediction for objective "<objective>" is "<prediction>"
        And I create a local deepnet prediction for "<data_input>"
        Then the local deepnet prediction is "<prediction>"

        Examples:
        | data  | time_1  | data_input | objective    | prediction    |
        | data/dates2.csv | 30000  | {"time-1": "1910-05-08T19:10:23.106", "cat-0":"cat2"} | 000002   | -0.02616    |
        | data/dates2.csv | 30000  | {"time-1": "2011-04-01T00:16:45.747", "cat-0":"cat2"} | 000002   | 0.13352    |
        | data/dates2.csv | 30000  | {"time-1": "1969-W29-1T17:36:39Z", "cat-0":"cat1"} | 000002   | 0.10071    |
        | data/dates2.csv | 30000  | {"time-1": "2001-01-05T23:04:04.693", "cat-0":"cat2"} | 000002   | 0.15235    |
        | data/dates2.csv | 30000  | {"time-1": "1950-11-06T05:34:05.602", "cat-0":"cat1"} | 000002   | -0.07686    |
        | data/dates2.csv | 30000  | {"time-1": "1932-01-30T19:24:11.440",  "cat-0":"cat2"} | 000002   | 0.0017    |
        | data/dates2.csv | 30000  | {"time-1": "Mon Jul 14 17:36 +0000 1969", "cat-0":"cat1"} | 000002   | 0.10071    |


    Scenario Outline: Successfully comparing predictions from deepnets:
        Given I provision a dataset from "<data>" file
        And I create a deepnet from a dataset with "<params>"
        And I wait until the deepnet is ready less than <time_1> secs
        And I create a local deepnet
        When I create a deepnet prediction for "<data_input>"
        Then the deepnet prediction for objective "<objective>" is "<prediction>"
        And I create a local deepnet prediction for "<data_input>"
        Then the local deepnet prediction is "<prediction>"

      Examples:  
        | data  | time_1  | data_input  | objective    | prediction | params    |
        | data/iris.csv | 1000  | {}    | 000004   | Iris-versicolor  | {}  |
        | data/iris.csv | 1000  | {}    | 000004   | Iris-virginica  | {"default_numeric_value": "maximum"} |
