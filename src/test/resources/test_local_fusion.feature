Feature: LocalFusion
    
    Scenario Outline: Successfully creating fusion from models:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a logisticregression with objective "<objective>" and params "<params>"
        And I wait until the logisticregression is ready less than <time_3> secs
        And I create a logisticregression with objective "<objective>" and params "<params>"
        And I wait until the logisticregression is ready less than <time_3> secs
        And I create a linearregression from a dataset
        And I wait until the linearregression is ready less than <time_3> secs
        And I create a fusion from models
        And I wait until the fusion is ready less than <time_3> secs
        And I create a local fusion
        When I create a prediction with fusion for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        Then the probability for the prediction is <probability>
        When I create a local fusion prediction for "<data_input>"
        Then the local fusion prediction is "<prediction>"
        Then the local prediction probability is <probability>
        
        Examples:
        | data	| time_1  | time_2 | time_3 | time_4 | params | objective | data_input	| prediction	| probability	|
        | data/iris.csv | 50      | 50     | 50     | 50 | {"missing_numerics": true} | 000004	| {"petal width": 1.75, "petal length": 2.45}	| Iris-setosa	| 0.4727	|
        
        
    Scenario Outline: Successfully creating fusion from models:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a logisticregression with objective "<objective>" and params "<params>"
        And I wait until the logisticregression is ready less than <time_3> secs
        And I create a logisticregression with objective "<objective>" and params "<params2>"
        And I wait until the logisticregression is ready less than <time_3> secs
        And I create a linearregression from a dataset
        And I wait until the linearregression is ready less than <time_3> secs
        And I create a fusion from models
        And I wait until the fusion is ready less than <time_3> secs
        And I create a local fusion
        When I create a prediction with fusion for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        Then the probability for the prediction is <probability>
        When I create a local fusion prediction for "<data_input>"
        Then the local fusion prediction is "<prediction>"
        Then the local prediction probability is <probability>
        
        Examples:
        | data	| time_1  | time_2 | time_3 | time_4 | params | params2	| objective | data_input	| prediction	| probability	|
        | data/iris.csv | 50      | 50     | 50     | 50 | {"missing_numerics": true} | {"missing_numerics": false, "balance_fields": false }	|  000004	| {"petal width": 1.75, "petal length": 2.45}	| Iris-setosa	| 0.4727	|
        
        
    Scenario Outline: Successfully creating fusion from models:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a logisticregression with objective "<objective>" and params "<params>"
        And I wait until the logisticregression is ready less than <time_3> secs
        And I create a logisticregression with objective "<objective>" and params "<params2>"
        And I wait until the logisticregression is ready less than <time_3> secs
        And I create a linearregression from a dataset
        And I wait until the linearregression is ready less than <time_3> secs
        And I create a fusion from models with weights "<weights>"
        And I wait until the fusion is ready less than <time_3> secs
        And I create a local fusion
        When I create a prediction with fusion for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        Then the probability for the prediction is <probability>
        When I create a local fusion prediction for "<data_input>"
        Then the local fusion prediction is "<prediction>"
        Then the local prediction probability is <probability>
        
        Examples:
        | data	| time_1  | time_2 | time_3 | time_4 | params | params2	| objective | data_input	| prediction	| probability	| weights	|
        | data/iris.csv | 50      | 50     | 50     | 50 | {"missing_numerics": true} | {"missing_numerics": false, "balance_fields": false }	|  000004	| {"petal width": 1.75, "petal length": 2.45}	| Iris-setosa	| 0.4727	| [1, 2]	|
         