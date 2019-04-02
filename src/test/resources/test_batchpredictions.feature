Feature: Batch Predictions
	
	Scenario Outline: Successfully creating a batch prediction for a model:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a model
        And I wait until the model is ready less than <time_3> secs

        When I create a batch prediction for the dataset with the model
        And I wait until the batch prediction is ready less than <time_4> secs
        And I download the created predictions file to "<local_file>"
        Then the batch prediction file "<local_file>" is like "<predictions_file>"
        Then delete test data

        Examples:
          | data             | time_1  | time_2 | time_3 | time_4 | local_file | predictions_file |
          | data/iris.csv |   30      | 30     | 50     | 50     | data/downloaded_batch_predictions.csv | data/batch_predictions.csv |
    
    
    Scenario Outline: Successfully creating a batch prediction from a multi model:
	    Given I create a data source uploading a "<data>" file
	    And I wait until the source is ready less than <time_1> secs
	    And I create a dataset
	    And I wait until the dataset is ready less than <time_2> secs
	    And I create a model with "<params>"
	    And I wait until the model is ready less than <time_3> secs
	    And I create a model with "<params>"
	    And I wait until the model is ready less than <time_3> secs
	    And I create a model with "<params>"
	    And I wait until the model is ready less than <time_3> secs
	    And I retrieve a list of remote models tagged with "<tag>"
	    And I create a local multi model
	    When I create a batch prediction for "<data_input>" and save it in "<path>"
	    And I combine the votes in "<path>"
	    Then the plurality combined predictions are "<predictions>"
	    And the confidence weighted predictions are "<predictions>"
	    Then delete test data
	
	  Examples:
	    | data          | time_1  | time_2 | time_3 | params                         |  tag  |  data_input    | path | predictions  |
	    | data/iris.csv |  10     | 10     | 10     | {"tags":["mytag"]} | mytag |  [{"petal width": 0.5}, {"petal length": 6, "petal width": 2}, {"petal length": 4, "petal width": 1.5}]  | data | ["Iris-setosa", "Iris-virginica", "Iris-versicolor"] |
	   	
    
    
	Scenario Outline: Successfully creating a source from a batch prediction:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a model
        And I wait until the model is ready less than <time_3> secs
        When I create a batch prediction for the dataset with the model
        And I wait until the batch prediction is ready less than <time_4> secs
        Then I create a source from the batch prediction
        And I wait until the source is ready less than <time_1> secs
        Then delete test data

        Examples:
          | data              |  time_1   | time_2 | time_3 | time_4 |
          | data/iris.csv     |   30      | 30     | 50     | 50     |
          | data/diabetes.csv |   30      | 30     | 50     | 50     |
 
 
	Scenario Outline: Successfully creating a batch prediction for an ensemble:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create an ensemble of <number_of_models> models
        And I wait until the ensemble is ready less than <time_3> secs
        When I create a batch prediction for the dataset with the ensemble
        And I wait until the batch prediction is ready less than <time_4> secs
        And I download the created predictions file to "<local_file>"
        Then the batch prediction file "<local_file>" is like "<predictions_file>"
        Then delete test data

        Examples:
          | data             | time_1  | time_2 | number_of_models | time_3 | time_4 | local_file | predictions_file |
          | data/iris.csv |   30      | 30     | 5                | 80     | 50     | data/downloaded_batch_predictions_e.csv | data/batch_predictions_e.csv |
          
          
     Scenario Outline: Successfully creating a batch prediction for a logistic regression:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a logisticregression from a dataset
        And I wait until the logisticregression is ready less than <time_3> secs
        When I create a batch prediction for the dataset with the logisticregression
        And I wait until the batch prediction is ready less than <time_4> secs
        And I download the created predictions file to "<local_file>"
        Then the batch prediction file "<local_file>" is like "<predictions_file>"

        Examples:
          | data              |  time_1   | time_2 | time_3 | time_4 | local_file | predictions_file       |
          | data/iris.csv     |   30      | 30     | 50     | 50     | data/downloaded_batch_predictions_lr.csv | data/batch_predictions_lr.csv |
	
	
	Scenario Outline: Successfully creating a batch prediction for a linear regression:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a linearregression from a dataset
        And I wait until the linearregression is ready less than <time_3> secs
        When I create a batch prediction for the dataset with the linearregression
        And I wait until the batch prediction is ready less than <time_4> secs    
        And I download the created predictions file to "<local_file>"
        Then the batch prediction file "<local_file>" is like "<predictions_file>"

        Examples:
          | data              |  time_1   | time_2 | time_3 | time_4 | local_file | predictions_file       |
          | data/grades.csv     |   30      | 30     | 50     | 50     | data/downloaded_batch_predictions_linear.csv | data/batch_predictions_linear.csv |
	
	
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
        
        
        
    Scenario Outline: Successfully creating a batch anomaly score from an anomaly detector:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create an anomaly detector from a dataset
        And I wait until the anomaly detector is ready less than <time_3> secs
        When I create a batch anomaly score
        And I wait until the batch anomaly score is ready less than <time_4> secs
        And I download the created batch anomaly score file to "<local_file>"
        Then the batch prediction file "<local_file>" is like "<predictions_file>"
        Then delete test data

        Examples:
          | data             | time_1  | time_2 | time_3 | time_4 | local_file | predictions_file       |
          | data/tiny_kdd.csv | 30      | 30     | 50     | 50     | data/downloaded_batch_predictions_a.csv | data/batch_predictions_a.csv |
    
    
    Scenario Outline: Successfully creating a batch centroid from a cluster:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a cluster
        And I wait until the cluster is ready less than <time_3> secs
        When I create a batch centroid for the dataset
        And I wait until the batch centroid is ready less than <time_4> secs
        
        
        
        And I download the created centroid file to "<local_file>"
        Then the batch centroid file is like "<predictions_file>"
		Then delete test data

        Examples:
          | data             |  time_1  | time_2 | time_3 | time_4 | local_file | predictions_file       |
          | data/diabetes.csv |   50      | 50     | 50     | 50     | data/downloaded_batch_predictions_c.csv |data/batch_predictions_c.csv |
    
   
    
    Scenario Outline: Successfully creating a batch projection:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a pca from a dataset
        And I wait until the pca is ready less than <time_3> secs

        When I create a batch projection for the dataset with the pca
        And I wait until the batch projection is ready less than <time_4> secs
        
        And I download the created projections file to "<local_file>"
        Then the batch projection file is like "<projections_file>"
        Then delete test data

        Examples:
          | data             | time_1  | time_2 | time_3 | time_4 | local_file | projections_file |
          | data/iris.csv |   30      | 30     | 50     | 50     | data/batch_projections.csv | data/batch_projections.csv |
   