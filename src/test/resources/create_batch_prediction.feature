Feature: Create Batch Predictions
    In order to create a batch prediction
    I need to create a model and a dataset first
	
  Scenario Outline: Successfully creating a batch prediction:
        Given that I use development mode with seed="<seed>"
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I add the unitTest tag to the data source waiting less than <time_1> secs
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
          | data             | seed      | time_1  | time_2 | time_3 | time_4 | local_file | predictions_file |
          | data/iris.csv | BigML |   30      | 30     | 50     | 50     | data/downloaded_batch_predictions.csv | data/batch_predictions.csv |


  Scenario Outline: Successfully creating a batch prediction for an ensemble:
        Given that I use development mode with seed="<seed>"
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I add the unitTest tag to the data source waiting less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create an ensemble of <number_of_models> models and <tlp> tlp
        And I wait until the ensemble is ready less than <time_3> secs
        When I create a batch prediction for the dataset with the ensemble
        And I wait until the batch prediction is ready less than <time_4> secs
        And I download the created predictions file to "<local_file>"
        Then the batch prediction file "<local_file>" is like "<predictions_file>"
        Then delete test data

        Examples:
          | data             | seed      | time_1  | time_2 | number_of_models | tlp | time_3 | time_4 | local_file | predictions_file |
          | data/iris.csv | BigML |   30      | 30     | 5                | 1   | 80     | 50     | data/downloaded_batch_predictions_e.csv | data/batch_predictions_e.csv |


	Scenario Outline: Successfully creating a batch centroid from a cluster:
        Given that I use development mode with seed="<seed>"
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I add the unitTest tag to the data source waiting less than <time_1> secs
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
          | data             | seed      |  time_1  | time_2 | time_3 | time_4 | local_file | predictions_file       |
          | data/diabetes.csv | BigML |   50      | 50     | 50     | 50     | data/downloaded_batch_predictions_c.csv |data/batch_predictions_c.csv |

    Scenario Outline: Successfully creating a source from a batch prediction:
        Given that I use development mode with seed="<seed>"
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I add the unitTest tag to the data source waiting less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a model
        And I wait until the model is ready less than <time_3> secs
        When I create a batch prediction for the dataset with the model
        And I wait until the batch prediction is ready less than <time_4> secs
        Then I create a source from the batch prediction
        And I wait until the source is ready less than <time_1> secs
        And I add the unitTest tag to the data source waiting less than <time_1> secs

        Examples:
          | data              | seed      |  time_1   | time_2 | time_3 | time_4 |
          | data/iris.csv     | BigML     |   30      | 30     | 50     | 50     |
          | data/diabetes.csv | BigML     |   30      | 30     | 50     | 50     |

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

        Examples:
          | data             | time_1  | time_2 | time_3 | time_4 | local_file | predictions_file       |
          | data/tiny_kdd.csv | 30      | 30     | 50     | 50     | data/downloaded_batch_predictions_a.csv | data/batch_predictions_a.csv |
