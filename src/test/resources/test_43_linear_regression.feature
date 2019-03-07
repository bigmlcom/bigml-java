Feature: Linear regression
	
	Scenario Outline: Successfully creating a linear regression from a dataset:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a linearregression from a dataset
        And I wait until the linearregression is ready less than <time_3> secs
        And I update the linearregression name to "<linearregression_name>"
        When I wait until the linearregression is ready less than <time_4> secs
        Then the linearregression name is "<linearregression_name>"

        Examples:
        | data  |  time_1  | time_2 | time_3 | time_4 | linearregression_name |
        | data/grades.csv | 100      | 100     | 200     | 200 | my new linear regression name |
        

	Scenario Outline: Successfully creating a prediction from linear regression:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a linearregression from a dataset
        And I wait until the linearregression is ready less than <time_3> secs
        When I create a linearregression prediction for "<data_input>"  
        Then the linearregression prediction is "<prediction>"

        Examples:
        | data  |  time_1  | time_2 | time_3 | data_input | prediction |
        | data/grades.csv | 100      | 100     | 200 | {"000000": 0.5, "000001": 1, "000002": 1, "000003": 1} | 2.27312	|


	Scenario Outline: Successfully creating a batch prediction for a linear regression:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a linearregression from a dataset
        And I wait until the linearregression is ready less than <time_3> secs
        
        When I create a batch prediction for the dataset with the linear regression
        And I wait until the batch prediction is ready less than <time_4> secs    
        And I download the created predictions file to "<local_file>"
        Then the batch prediction file "<local_file>" is like "<predictions_file>"

        Examples:
          | data              |  time_1   | time_2 | time_3 | time_4 | local_file | predictions_file       |
          | data/grades.csv     |   30      | 30     | 50     | 50     | data/downloaded_batch_predictions_linear.csv | data/batch_predictions_linear.csv |
