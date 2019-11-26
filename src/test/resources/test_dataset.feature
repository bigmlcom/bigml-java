Feature: Dataset REST api

    Scenario Outline: Successfully creating and reading a public dataset:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I make the dataset public
        And I wait until the dataset is ready less than <time_3> secs
        When I get the dataset status using the dataset's public url
        Then the dataset's status is FINISHED
				Then delete test data

        Examples:
        | data	| time_1  | time_2 | time_3 |
        | data/iris.csv |  20      | 20     | 20     |


    Scenario Outline: Successfully exporting a dataset:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I download the dataset file to "<local_file>"
				Then the dataset file "<data>" is like "<local_file>"
        Then delete test data

        Examples:
        | data          | time_1  | time_2 | local_file |
        | data/iris.csv |   30      | 30     | data/exported_iris.csv |
        
    
    Scenario Outline: Successfully creating a sample from a dataset:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a sample from a dataset
        And I wait until the sample is ready less than <time_3> secs
        And I update the sample with "<params>" waiting less than <time_4> secs
        And I wait until the sample is ready less than <time_3> secs
				Then the sample name is <name>
        Then delete test data

        Examples:
        | data          | time_1  | time_2 | time_3 |   time_4  |   params                            |   name                 |
        | data/iris.csv |   30    |  30    |  30    |   50      |   {"name": "my new sample name"}    | "my new sample name"   |


    Scenario Outline: Successfully creating, reading and downloading a sample:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a sample from a dataset
        And I wait until the sample is ready less than <time_3> secs
        And I download the sample file to "<local_file>" with <rows> rows and "<seed>" seed
        Then the sample file "<expected_file>" is like "<local_file>"
        Then delete test data

        Examples:
        | data          | seed  | time_1  | time_2 | time_3 | rows  |   expected_file                 |   local_file                    |
        | data/iris.csv | BigML |   30    |  30    |  30    | 50    |   data/expected_iris_sample.csv | data/exported_sample_iris.csv   |
        
        
     Scenario Outline: Successfully creating a split dataset:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a dataset extracting a <rate> sample
        And I wait until the dataset is ready less than <time_3> secs
        When I compare the datasets' instances
        Then the proportion of instances between datasets is <rate>
        Then delete test data

				Examples:
				| data             | time_1  | time_2 | time_3 | rate |
        | data/iris.csv    |  10     | 10     | 10     | 0.8  |
        
     
     Scenario Outline: Successfully obtaining missing values counts:
		    Given I create a data source uploading a "<data>" file
		    And I wait until the source is ready less than <time_1> secs
		    And I update the source with "<params>" waiting less than <time_1> secs
		    And I create a dataset
		    And I wait until the dataset is ready less than <time_2> secs
		    When I ask for the missing values counts in the fields
		    Then the missing values counts dict is "<missing_values>"
		    Then delete test data
		
		  Examples:
		    | data	|  time_1  | params	| time_2 | missing_values       |
		    | data/iris_missing.csv |    30      | {"fields": {"000000": {"optype": "numeric"}}}   |30      | {"000000": 1}      |


	  Scenario Outline: Successfully obtaining parsing error counts:
		    Given I create a data source uploading a "<data>" file
		    And I wait until the source is ready less than <time_1> secs
		    And I update the source with "<params>" waiting less than <time_1> secs
		    And I create a dataset
		    And I wait until the dataset is ready less than <time_2> secs
		    When I ask for the error counts in the fields
		    Then the error counts dict is "<error_values>"
		    Then delete test data
		
		  Examples:
		    | data	|   time_1  | params	| time_2 |error_values       |
		    | data/iris_missing.csv |    30      | {"fields": {"000000": {"optype": "numeric"}}}   |30      |{"000000": 1}      |
	     