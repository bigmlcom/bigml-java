Feature: Anomaly REST api

	  Scenario Outline: Successfully creating an anomaly detector from a dataset and a dataset list:
	    Given I provision a dataset from "<data>" file
	    Then I create an anomaly detector from a dataset
	    And I wait until the anomaly detector is ready less than <time_2> secs
	    And I check the anomaly detector stems from the original dataset
	    And I store the dataset id in a list
	    And I create a dataset
	    And I wait until the dataset is ready less than <time_1> secs
	    And I store the dataset id in a list
	    Then I create an anomaly detector from a dataset list
	    And  I wait until the anomaly detector is ready less than <time_2> secs
	    And I check the anomaly detector stems from the original dataset list
			
	  	Examples:
	    | data                | time_1  | time_2	|
	    | data/iris.csv       |  50     | 100     |
	    | data/tiny_kdd.csv   |  50     | 100     |
	
	
		Scenario Outline: Successfully creating an anomaly detector from a dataset and generating the anomalous dataset:
	    Given I provision a dataset from "<data>" file
	    Then I create an anomaly detector of <rows> anomalies from a dataset
	    And I wait until the anomaly detector is ready less than <time_2> secs
	    And I create a dataset with only the anomalies
	    And I wait until the dataset is ready less than <time_1> secs
	    And I check that the dataset has <rows> rows
	
	    Examples:
	    | data                | time_1	| time_2 | rows	|
	    | data/iris.csv       | 50     | 100    |  1 |