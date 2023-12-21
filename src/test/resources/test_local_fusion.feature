@localfusion
Feature: LocalFusion

    Scenario Outline: Successfully creating fusion from models:
        Given I provision a dataset from "<data>" file
        And I create a logisticregression with objective "<objective>" and params "<params>"
        And I wait until the logisticregression is ready less than <time_1> secs
        And I create a logisticregression with objective "<objective>" and params "<params>"
        And I wait until the logisticregression is ready less than <time_1> secs
        And I create a linearregression from a dataset
        And I wait until the linearregression is ready less than <time_1> secs
        And I create a fusion from models
        And I wait until the fusion is ready less than <time_1> secs
        And I create a local fusion
        When I create a prediction with fusion for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        Then the probability for the prediction is <probability>
        When I create a local fusion prediction for "<data_input>"
        Then the local fusion prediction is "<prediction>"
        Then the local prediction probability is <probability>

        Examples:
        | data	| time_1  | params | objective | data_input	| prediction	| probability	|
        | data/iris.csv | 50	| {"missing_numerics": true} | 000004	| {"petal width": 1.75, "petal length": 2.45}	| Iris-setosa	| 0.4726	|


    Scenario Outline: Successfully creating fusion from models:
        Given I provision a dataset from "<data>" file
        And I create a logisticregression with objective "<objective>" and params "<params>"
        And I wait until the logisticregression is ready less than <time_1> secs
        And I create a logisticregression with objective "<objective>" and params "<params2>"
        And I wait until the logisticregression is ready less than <time_1> secs
        And I create a linearregression from a dataset
        And I wait until the linearregression is ready less than <time_1> secs
        And I create a fusion from models
        And I wait until the fusion is ready less than <time_1> secs
        And I create a local fusion
        When I create a prediction with fusion for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        Then the probability for the prediction is <probability>
        When I create a local fusion prediction for "<data_input>"
        Then the local fusion prediction is "<prediction>"
        Then the local prediction probability is <probability>

        Examples:
        | data	| time_1  | params | params2	| objective | data_input	| prediction	| probability	|
        | data/iris.csv | 50	| {"missing_numerics": true} | {"missing_numerics": false, "balance_fields": false }	|  000004	| {"petal width": 1.75, "petal length": 2.45}	| Iris-setosa	| 0.4726	|


    Scenario Outline: Successfully creating fusion from models:
        Given I provision a dataset from "<data>" file
        And I create a logisticregression with objective "<objective>" and params "<params>"
        And I wait until the logisticregression is ready less than <time_1> secs
        And I create a logisticregression with objective "<objective>" and params "<params2>"
        And I wait until the logisticregression is ready less than <time_1> secs
        And I create a linearregression from a dataset
        And I wait until the linearregression is ready less than <time_1> secs
        And I create a fusion from models with weights "<weights>"
        And I wait until the fusion is ready less than <time_1> secs
        And I create a local fusion
        When I create a prediction with fusion for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        Then the probability for the prediction is <probability>
        When I create a local fusion prediction for "<data_input>"
        Then the local fusion prediction is "<prediction>"
        Then the local prediction probability is <probability>

        Examples:
        | data	| time_1  | params | params2	| objective | data_input	| prediction	| probability	| weights	|
        | data/iris.csv | 50	| {"missing_numerics": true} | {"missing_numerics": false, "balance_fields": false }	|  000004	| {"petal width": 1.75, "petal length": 2.45}	| Iris-setosa	| 0.4726	| [1, 2]	|
