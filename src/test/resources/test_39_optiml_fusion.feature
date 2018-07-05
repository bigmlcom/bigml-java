Feature: Testing REST api calls
    
    Scenario Outline: Successfully creating an optiml from a dataset:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create an optiml from a dataset
        And I wait until the optiml is ready less than <time_3> secs
        And I update the optiml name to "<optiml_name>"
        When I wait until the optiml is ready less than <time_4> secs
        Then the optiml name is "<optiml_name>"
        Then I delete the optiml

        Examples:
        | data                | time_1  | time_2 | time_3 | time_4 | optiml_name |
        | data/iris.csv | 50      | 50     | 1000     | 100 | my new optiml name |
   
    Scenario Outline: Successfully creating fusion from models:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a model
        And I wait until the model is ready less than <time_3> secs
        And I create a model
        And I wait until the model is ready less than <time_3> secs
        And I create a model
        And I wait until the model is ready less than <time_3> secs
        And I create a fusion from models
        And I wait until the fusion is ready less than <time_3> secs
        And I update the fusion name to "<fusion_name>"
        When I wait until the fusion is ready less than <time_4> secs
        Then the fusion name is "<fusion_name>"
        When I create a prediction for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        When I create an evaluation for the fusion with the dataset
        And I wait until the evaluation is ready less than <time_4> secs
        Then the measured "<measure>" is <value>
        Then I delete the fusion

        Examples:
        | data                | time_1  | time_2 | time_3 | time_4 | fusion_name | data_input    | objective | prediction  | measure       | value  |
        | data/iris.csv | 50      | 50     | 50     | 50 | my new fusion name | {"petal width": 1.75, "petal length": 2.45}	| 000004    | Iris-setosa | average_phi   | 1      |
      
      
	Scenario Outline: Successfully creating a fusion
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a model
        And I wait until the model is ready less than <time_3> secs
        And I create a model
        And I wait until the model is ready less than <time_3> secs
        And I create a model
        And I wait until the model is ready less than <time_3> secs
        And I create a fusion from models
        And I wait until the fusion is ready less than <time_3> secs
        When I create a batch prediction for the dataset with the fusion
        And I wait until the batch prediction is ready less than <time_4> secs
        And I download the created predictions file to "<local_file>"
        Then the batch prediction file "<local_file>" is like "<predictions_file>"
        Then delete test data
        
        Examples:
        | data                | time_1  | time_2 | time_3 | time_4 | local_file | predictions_file |
        | data/iris.csv | 50      | 50     | 50     | 50 |  data/downloaded_batch_predictions.csv | data/batch_predictions_fs.csv |
        
    
    Scenario Outline: Successfully creating fusion from models:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a logisticregression with objective "<objective>" and params "<params>"
        And I wait until the logisticregression is ready less than <time_3> secs
        And I create a logisticregression with objective "<objective>" and params "<params>"
        And I wait until the logisticregression is ready less than <time_3> secs
        And I create a fusion from models
        And I wait until the fusion is ready less than <time_3> secs
        And I create a local fusion
        When I create a prediction with fusion for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        Then the probability for the prediction is <probability>
        When I create a local fusion prediction for "<data_input>"
        Then the local fusion prediction is "<prediction>"
        Then the local prediction probability is <probability>
        Then delete test data
        
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
        And I create a fusion from models
        And I wait until the fusion is ready less than <time_3> secs
        And I create a local fusion
        When I create a prediction with fusion for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        Then the probability for the prediction is <probability>
        When I create a local fusion prediction for "<data_input>"
        Then the local fusion prediction is "<prediction>"
        Then the local prediction probability is <probability>
        Then delete test data
        
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
        And I create a fusion from models with weights "<weights>"
        And I wait until the fusion is ready less than <time_3> secs
        And I create a local fusion
        When I create a prediction with fusion for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        Then the probability for the prediction is <probability>
        When I create a local fusion prediction for "<data_input>"
        Then the local fusion prediction is "<prediction>"
        Then the local prediction probability is <probability>
        Then delete test data
        
        Examples:
        | data	| time_1  | time_2 | time_3 | time_4 | params | params2	| objective | data_input	| prediction	| probability	| weights	|
        | data/iris.csv | 50      | 50     | 50     | 50 | {"missing_numerics": true} | {"missing_numerics": false, "balance_fields": false }	|  000004	| {"petal width": 1.75, "petal length": 2.45}	| Iris-setosa	| 0.4727	| [1, 2]	|
        
          