Feature: LocalAnomaly
	
		Scenario Outline: Successfully comparing scores from anomaly detectors:
        Given I provision a dataset from "<data>" file
        And I create an anomaly detector from a dataset
        And I wait until the anomaly detector is ready less than <time_1> secs
        And I create a local anomaly detector
        When I create an anomaly score for "<data_input>"
        Then the anomaly score is "<score>"
        And I create a local anomaly score for "<data_input>"
        Then the local anomaly score is <score>

      Examples:
        | data	| time_1  | data_input	| score	|
        | data/tiny_kdd.csv	| 1000	| {"000020": 255.0, "000004": 183.0, "000016": 4.0, "000024": 0.04, "000025": 0.01, "000026": 0.0, "000019": 0.25, "000017": 4.0, "000018": 0.25, "00001e": 0.0, "000005": 8654.0, "000009": "0", "000023": 0.01, "00001f": 123.0}         | 0.69802  |
	