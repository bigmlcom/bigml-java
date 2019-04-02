Feature: Anomaly

    Scenario Outline: Successfully creating an anomaly detector from a dataset and a dataset list:
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I create a dataset
    And I wait until the dataset is ready less than <time_2> secs
    Then I create an anomaly detector from a dataset
    And I wait until the anomaly detector is ready less than <time_4> secs
    And I check the anomaly detector stems from the original dataset
    And I store the dataset id in a list
    And I create a dataset
    And I wait until the dataset is ready less than <time_3> secs
    And I store the dataset id in a list
    Then I create an anomaly detector from a dataset list
    And  I wait until the anomaly detector is ready less than <time_4> secs
    And I check the anomaly detector stems from the original dataset list
    Then delete test data

  Examples:
    | data                | time_1  | time_2 | time_3 | time_4 |
    | data/iris.csv       |  40     | 40     | 40     | 100    |
    | data/tiny_kdd.csv   |  40     | 40     | 40     | 100    |


  Scenario Outline: Successfully creating an anomaly detector from a dataset and generating the anomalous dataset:
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I create a dataset
    And I wait until the dataset is ready less than <time_2> secs
    Then I create an anomaly detector of <rows> anomalies from a dataset
    And I wait until the anomaly detector is ready less than <time_4> secs
    And I create a dataset with only the anomalies
    And I wait until the dataset is ready less than <time_3> secs
    And I check that the dataset has <rows> rows

    Examples:
    | data                | time_1  | time_2 | time_3 | time_4 |  rows|
    | data/iris.csv       | 40     | 40     | 40     | 100    |  1 |
    
    
  Scenario Outline: Successfully creating an anomaly score:
      Given I create a data source uploading a "<data>" file
      And I wait until the source is ready less than <time_1> secs
      And I create a dataset
      And I wait until the dataset is ready less than <time_2> secs
      And I create an anomaly detector from a dataset
      And I wait until the anomaly detector is ready less than <time_3> secs
      When I create an anomaly score for "<data_input>"
      Then the anomaly score is "<score>"
      Then delete test data

      Examples:
      | data                    | time_1  | time_2 | time_3   | data_input                  | score   |
      | data/tiny_kdd.csv       | 10      | 10     | 100      | {"src_bytes": 350}           | 0.92846 |
      | data/iris_sp_chars.csv  | 10      | 10     | 100      | {"pétal&width\u0000": 300}  | 0.89313 |
      
   
   Scenario Outline: Successfully comparing scores from anomaly detectors:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create an anomaly detector from a dataset
        And I wait until the anomaly detector is ready less than <time_3> secs
        And I create a local anomaly detector
        When I create an anomaly score for "<data_input>"
        Then the anomaly score is "<score>"
        And I create a local anomaly score for "<data_input>"
        Then the local anomaly score is <score>

      Examples:
        | data	| time_1  | time_2 | time_3 | data_input    | score    |
        | data/tiny_kdd.csv    | 20      | 20     | 30     | {"000020": 255.0, "000004": 183.0, "000016": 4.0, "000024": 0.04, "000025": 0.01, "000026": 0.0, "000019": 0.25, "000017": 4.0, "000018": 0.25, "00001e": 0.0, "000005": 8654.0, "000009": "0", "000023": 0.01, "00001f": 123.0}         | 0.69802  |
        