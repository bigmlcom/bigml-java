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
        Then delete test data

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
        Then delete test data

        Examples:
        | data  |  time_1  | time_2 | time_3 | data_input | prediction |
        | data/grades.csv | 100      | 100     | 200 | {"000000": 0.5, "000001": 1, "000002": 1, "000003": 1} | 2.27312	|
