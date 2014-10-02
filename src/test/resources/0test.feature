Feature: Create Predictions
  In order to create a prediction
  I need to create a model first

  Scenario Outline: Successfully creating a centroid and the associated dataset:
    Given that I use production mode with seed="<seed>"
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I add the unitTest tag to the data source waiting less than <time_1> secs
    And I create a dataset
    And I wait until the dataset is ready less than <time_2> secs
    And I create a cluster
    And I wait until the cluster is ready less than <time_3> secs
    When I create a centroid for "<data_input>"
    And I check the centroid is ok
    Then the centroid is "<centroid>"
    Then delete test data

  Examples:
    | data                | seed      | time_1  | time_2 | time_3 | data_input    | centroid  |
    | data/diabetes.csv | BigML tests |  10      | 10     | 10     | {"pregnancies": 0, "plasma glucose": 118, "blood pressure": 84, "triceps skin thickness": 47, "insulin": 230, "bmi": 45.8, "diabetes pedigree": 0.551, "age": 31, "diabetes": "true"} | Cluster 0 |
