Feature: LocalEnsemble

		Scenario Outline: Successfully creating a local prediction from an Ensemble:
        Given I provision a dataset from "<data>" file
        And I create an ensemble of <number_of_models> models
        And I wait until the ensemble is ready less than <time_1> secs
        And I create a local ensemble
        When the local ensemble prediction for "<data_input>" is "<prediction>" with confidence <confidence>
        #And the local probabilities are "<probabilities>"

        Examples:
          | data    |  time_1  | number_of_models | data_input  | prediction  | confidence |
          | data/iris.csv | 50  | 5     | {"petal width": 0.5} | Iris-versicolor | 0.3687 | ["0.3403","0.4150","0.2447"]  |


      Scenario Outline: Successfully obtaining field importance from an Ensemble:
        Given I provision a dataset from "<data>" file
        And I create a model with "<parms1>"
        And I wait until the model is ready less than <time_1> secs
        And I create a model with "<parms2>"
        And I wait until the model is ready less than <time_2> secs
        And I create a model with "<parms3>"
        And I wait until the model is ready less than <time_3> secs
        When I create a local Ensemble with the last <number_of_models> models
        Then the field importance text is "<field_importance>"

        Examples:
          | data        | parms1 | time_1 | parms2 | time_2 | parms3    | time_3 | number_of_models | field_importance |
          | data/iris.csv | {"input_fields": ["000000", "000001","000003", "000004"]} | 20  | {"input_fields": ["000000", "000001","000002", "000004"]} | 20    | {"input_fields": ["000000", "000001","000002", "000003", "000004"]} | 20   | 3 |[["000002", 0.5269933333333333], ["000003", 0.38936], ["000000", 0.04662333333333333], ["000001", 0.037026666666666666]] |


      Scenario Outline: Successfully creating a local prediction from an Ensemble adding confidence:
        Given I provision a dataset from "<data>" file
        And I create an ensemble of <number_of_models> models
        And I wait until the ensemble is ready less than <time_1> secs
        And I create a local ensemble
        #When I create a local ensemble prediction for "<data_input>" in JSON adding confidence
        When the local ensemble prediction for "<data_input>" is "<prediction>" with confidence <confidence>

        Examples:
          | data            |  time_1  | number_of_models |  data_input           |prediction       | confidence  |
          | data/iris.csv   |  50      | 5                | {"petal width": 0.5}  | Iris-versicolor |  0.3687     |
          #['data/iris.csv', '10', '10', '50', '5', '1', '{"petal width": 0.5}', 'Iris-versicolor', '0.415']]


      Scenario Outline: Successfully creating a local prediction from an Ensemble:
        Given I provision a dataset from "<data>" file
        And I create an ensemble of <number_of_models> models
        And I wait until the ensemble is ready less than <time_1> secs
        And I create a local ensemble
        When the local ensemble prediction using median with confidence for "<data_input>" is "<prediction>"

        Examples:
          | data            |  time_1  | number_of_models |  data_input    |prediction  |
          | data/grades.csv   | 50     | 2              | {}             | 69.0934    |


      Scenario Outline: Successfully creating a local prediction from an Ensemble with max models:
        Given I provision a dataset from "<data>" file
        And I create an ensemble of <number_of_models> models
        And I wait until the ensemble is ready less than <time_1> secs
        And I create a local ensemble with max models <max_models>
        When the local ensemble prediction for "<data_input>" is "<prediction>"
    
        Examples:
          | data            |  time_1  | number_of_models | max_models |  data_input    |prediction  |
          | data/iris.csv   |  50      | 5                | 2          | {"petal width": 0.5} | Iris-versicolor |


    Scenario Outline: Successfully comparing predictions for ensembles
        Given I provision a dataset from "<data>" file
        And I create an ensemble with "<params>"
        And I wait until the ensemble is ready less than <time_1> secs
        And I create a local ensemble
        When I create a prediction with ensemble for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        When the local ensemble prediction for "<data_input>" is "<prediction>"

        Examples:
          | data    | time_1  | params | data_input | objective  | prediction |
          | data/iris_unbalanced.csv | 120     | {"boosting": {"iterations": 5}, "number_of_models": 5} |{"petal width": 4}  | 000004   | Iris-virginica   |
          | data/grades.csv | 120     | {"boosting": {"iterations": 5}, "number_of_models": 5} |{"Midterm": 20}  | 000005   | 61.61036   |


     Scenario Outline: Successfully comparing predictions for ensembles with proportional missing strategy
        Given I provision a dataset from "<data>" file
        And I create an ensemble with "<params>"
        And I wait until the ensemble is ready less than <time_1> secs
        And I create a local ensemble
        When I create a proportional missing strategy prediction with ensemble with "<options>" for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        And the confidence for the prediction is <confidence>
        And I create a proportional missing strategy local prediction with ensemble with "<options>" for "<data_input>"
        Then the local ensemble prediction is "<prediction>"
        And the local ensemble confidence is <confidence>

        Examples:
          | data    | time_1  | params | data_input | objective  | prediction | options | confidence    |
          | data/iris.csv | 120     | {"boosting": {"iterations": 5}} | {}  | 000004   | Iris-virginica   | {}  | 0.33784   |
          | data/iris.csv | 120     | {"number_of_models": 5} | {}  | 000004   | Iris-versicolor   | {"operating_kind": "confidence"}   | 0.2923    |
          | data/grades.csv | 120     | {"number_of_models": 5} | {}  | 000005   | 70.50579   | {}  | 30.7161   |
          | data/grades.csv | 120     | {"number_of_models": 5} | {"Midterm": 20}  | 000005   | 54.82214   | {"operating_kind": "confidence"}   | 25.89672  |
          | data/grades.csv | 120     | {"number_of_models": 5} | {"Midterm": 20}  | 000005   | 45.4573   | {}  | 29.58403  |
          | data/grades.csv | 120     | {"number_of_models": 5} | {"Midterm": 20, "Tutorial": 90, "TakeHome": 100}  | 000005   | 42.814   | {}  | 31.51804  |


    Scenario Outline: Successfully comparing predictions for ensembles with operating point
        Given I provision a dataset from "<data>" file
        And I create an ensemble
        And I wait until the ensemble is ready less than <time_1> secs
        And I create a local ensemble
        When I create a prediction with ensemble with operating point "<operating_point>" for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        When I create a local prediction with ensemble with operating point "<operating_point>" for "<data_input>"
        Then the local ensemble prediction is "<prediction>"

        Examples:
        | data  | time_1  | data_input | objective  | prediction    | operating_point   |
        | data/iris.csv | 30000 | {"petal width": 4} | 000004   | Iris-setosa   | {"kind": "probability", "threshold": 0.1, "positive_class": "Iris-setosa"}    |
        | data/iris.csv | 30000 | {"petal width": 4} | 000004   | Iris-virginica    | {"kind": "probability", "threshold": 0.9, "positive_class": "Iris-setosa"}    |
        | data/iris.csv | 30000 | {"sepal length": 4.1, "sepal width": 2.4} | 000004    | Iris-setosa   | {"kind": "confidence", "threshold": 0.1, "positive_class": "Iris-setosa"} |   
        | data/iris.csv | 30000 | {"sepal length": 4.1, "sepal width": 2.4}| 000004 | Iris-versicolor   | {"kind": "confidence", "threshold": 0.9, "positive_class": "Iris-setosa"} |


    Scenario Outline: Successfully comparing predictions for ensembles with operating kind
        Given I provision a dataset from "<data>" file
        And I create an ensemble
        And I wait until the ensemble is ready less than <time_1> secs
        And I create a local ensemble
        When I create a prediction with ensemble with operating kind "<operating_kind>" for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        When I create a local prediction with ensemble with operating kind "<operating_kind>" for "<data_input>"
        Then the local ensemble prediction is "<prediction>"

        Examples:
        | data  | time_1  | data_input | objective    | prediction    | operating_kind    |
        | data/iris.csv | 30000  | {"petal length": 2.46} | 000004   | Iris-versicolor   | probability   |
        | data/iris.csv | 30000  | {"petal length": 2} | 000004  | Iris-setosa   | probability   |      
        | data/iris.csv | 30000  | {"petal length": 2.46} | 000004   | Iris-versicolor   | confidence   |
        | data/iris.csv | 30000  | {"petal length": 2} | 000004  | Iris-setosa   | confidence   |
        | data/iris.csv | 30000  | {"petal length": 2.46} | 000004   | Iris-versicolor   | votes   |
        | data/iris.csv | 30000  | {"petal length": 2} | 000004  | Iris-setosa   | votes   |


    Scenario Outline: Successfully comparing predictions with raw date input
        Given I provision a dataset from "<data>" file
        And I create an ensemble
        And I wait until the ensemble is ready less than <time_1> secs
        And I create a local ensemble
        When I create a prediction with ensemble for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        When I create a local prediction with ensemble for "<data_input>"
        Then the local ensemble prediction is "<prediction>"

        Examples:
        | data  | time_1  | data_input | objective    | prediction    |
        | data/dates2.csv | 30000  | {"time-1": "1910-05-08T19:10:23.106", "cat-0":"cat2"} | 000002   | -0.11052   |
        | data/dates2.csv | 30000  | {"time-1": "1920-06-30T20:21:20.320", "cat-0":"cat1"} | 000002   | 0.79179   |
        | data/dates2.csv | 30000  | {"time-1": "1932-01-30T19:24:11.450", "cat-0":"cat2"} | 000002   | -1.00834   |
        | data/dates2.csv | 30000  | {"time-1": "1950-11-06T05:34:05.252", "cat-0":"cat1"} | 000002   | -0.14442   |
        | data/dates2.csv | 30000  | {"time-1": "1969-7-14 17:36", "cat-0":"cat2"} | 000002   | -0.05469   |
        | data/dates2.csv | 30000  | {"time-1": "2001-01-05T23:04:04.693", "cat-0":"cat2"} | 000002   | -0.23387   |
        | data/dates2.csv | 30000  | {"time-1": "1969-W29-1T17:36:39Z", "cat-0":"cat1"} | 000002   | -0.05469   |
        | data/dates2.csv | 30000  | {"time-1": "Mon Jul 14 17:36 +0000 1969", "cat-0":"cat1"} | 000002   | -0.05469   |


    Scenario Outline: Successfully comparing predictions with ensembles
        Given I provision a dataset from "<data>" file
        And I create an ensemble from a dataset with "<params>"
        And I wait until the ensemble is ready less than <time_1> secs
        And I create a local ensemble
        When I create a prediction with ensemble for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        When I create a local prediction with ensemble for "<data_input>"

        Examples:
        | data  | time_1  | data_input | objective      | prediction    | params                |
        | data/iris.csv | 120  | {} | 000004    | Iris-versicolor       | {}    |
        | data/iris.csv | 120  | {} | 000004    | Iris-versicolor       | {"default_numeric_value": "mean"}     |
