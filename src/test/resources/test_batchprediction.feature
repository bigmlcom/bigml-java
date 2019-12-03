Feature: Batch Predictions

  Scenario Outline: Successfully creating a batch prediction:
        Given I provision a dataset from "<data>" file
        And I create a model
        And I wait until the model is ready less than <time_1> secs
        When I create a batch prediction for the dataset with the model
        And I wait until the batch prediction is ready less than <time_2> secs
        And I download the created predictions file to "<local_file>"
        Then the batch prediction file "<local_file>" is like "<predictions_file>"

        Examples:
          | data	| time_1  | time_2 | local_file | predictions_file |
          | data/iris.csv | 50     | 50     | data/downloaded_batch_predictions.csv | data/batch_predictions.csv |


  Scenario Outline: Successfully creating a batch prediction for an ensemble:
        Given I provision a dataset from "<data>" file
        And I create an ensemble of <number_of_models> models
        And I wait until the ensemble is ready less than <time_1> secs
        When I create a batch prediction for the dataset with the ensemble
        And I wait until the batch prediction is ready less than <time_2> secs
        And I download the created predictions file to "<local_file>"
        Then the batch prediction file "<local_file>" is like "<predictions_file>"

        Examples:
          | data	| time_1  | time_2 | number_of_models | local_file | predictions_file |
          | data/iris.csv |   80      | 530     | 5	| data/downloaded_batch_predictions_e.csv | data/batch_predictions_e.csv |


	Scenario Outline: Successfully creating a batch centroid from a cluster:
        Given I provision a dataset from "<data>" file
        And I create a cluster
        And I wait until the cluster is ready less than <time_1> secs
        When I create a batch centroid for the dataset
        And I wait until the batch centroid is ready less than <time_2> secs
        And I download the created centroid file to "<local_file>"
        Then the batch centroid file is like "<predictions_file>"

        Examples:
          | data             |  time_1  | time_2 | local_file | predictions_file       |
          | data/diabetes.csv |   50      | 50     | data/downloaded_batch_predictions_c.csv |data/batch_predictions_c.csv |


    Scenario Outline: Successfully creating a source from a batch prediction:
        Given I provision a dataset from "<data>" file
        And I create a model
        And I wait until the model is ready less than <time_1> secs
        When I create a batch prediction for the dataset with the model
        And I wait until the batch prediction is ready less than <time_2> secs
        Then I create a source from the batch prediction
        And I wait until the source is ready less than <time_1> secs

        Examples:
          | data              | time_1	| time_2	|
          | data/iris.csv     | 50     | 50     |
          | data/diabetes.csv | 50     | 50     |


    Scenario Outline: Successfully creating a batch anomaly score from an anomaly detector:
        Given I provision a dataset from "<data>" file
        And I create an anomaly detector from a dataset
        And I wait until the anomaly detector is ready less than <time_1> secs
        When I create a batch anomaly score
        And I wait until the batch anomaly score is ready less than <time_2> secs
        And I download the created batch anomaly score file to "<local_file>"
        Then the batch prediction file "<local_file>" is like "<predictions_file>"

        Examples:
          | data             | time_1  | time_2 | local_file | predictions_file       |
          | data/tiny_kdd.csv | 50     | 50     | data/downloaded_batch_predictions_a.csv | data/batch_predictions_a.csv |


    Scenario Outline: Successfully creating a batch prediction for a logistic regression:
        Given I provision a dataset from "<data>" file
        And I create a logisticregression from a dataset
        And I wait until the logisticregression is ready less than <time_1> secs
        When I create a batch prediction for the dataset with the logistic regression
        And I wait until the batch prediction is ready less than <time_2> secs
        And I download the created predictions file to "<local_file>"
        Then the batch prediction file "<local_file>" is like "<predictions_file>"

        Examples:
          | data              |  time_1   | time_2 | local_file | predictions_file       |
          | data/iris.csv     | 50     | 50     | data/downloaded_batch_predictions_lr.csv | data/batch_predictions_lr.csv |

          
     Scenario Outline: Successfully creating a batch prediction for a linear regression:
        Given I provision a dataset from "<data>" file
        And I create a linearregression from a dataset
        And I wait until the linearregression is ready less than <time_1> secs
        When I create a batch prediction for the dataset with the linear regression
        And I wait until the batch prediction is ready less than <time_2> secs    
        And I download the created predictions file to "<local_file>"
        Then the batch prediction file "<local_file>" is like "<predictions_file>"

        Examples:
          | data	|  time_1   | time_2 | local_file | predictions_file       |
          | data/grades.csv	| 50     | 50     | data/downloaded_batch_predictions_linear.csv | data/batch_predictions_linear.csv |
	
	
		Scenario Outline: Successfully creating a fusion
        Given I provision a dataset from "<data>" file
        And I create a model
        And I wait until the model is ready less than <time_1> secs
        And I create a model
        And I wait until the model is ready less than <time_1> secs
        And I create a model
        And I wait until the model is ready less than <time_1> secs
        And I create a fusion from models
        And I wait until the fusion is ready less than <time_1> secs
        When I create a batch prediction for the dataset with the fusion
        And I wait until the batch prediction is ready less than <time_2> secs
        And I download the created predictions file to "<local_file>"
        Then the batch prediction file "<local_file>" is like "<predictions_file>"
        
        Examples:
        | data	| time_1  | time_2 | local_file | predictions_file |
        | data/iris.csv | 50      | 50     |  data/downloaded_batch_predictions.csv | data/batch_predictions_fs.csv |