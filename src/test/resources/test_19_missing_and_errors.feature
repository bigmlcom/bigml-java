Feature: Obtain missing values and errors counters
          In order to get the missing values and errors
          I need to create a dataset first

  Scenario Outline: Successfully obtaining missing values counts:
    Given that I use production mode with seed="<seed>"
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I update the source with "<params>" waiting less than <time_1> secs
    And I add the unitTest tag to the data source waiting less than <time_1> secs
    And I create a dataset
    And I wait until the dataset is ready less than <time_2> secs
    When I ask for the missing values counts in the fields
    Then the missing values counts dict is "<missing_values>"

  Examples:
    | data                     | seed  |   time_1  | params                                          | time_2 | missing_values       |
    | data/iris_missing.csv | BigML |    30      | {"fields": {"000000": {"optype": "numeric"}}}   |30      | {"000000": 1}      |


  Scenario Outline: Successfully obtaining parsing error counts:
    Given that I use production mode with seed="<seed>"
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I update the source with "<params>" waiting less than <time_1> secs
    And I add the unitTest tag to the data source waiting less than <time_1> secs
    And I create a dataset
    And I wait until the dataset is ready less than <time_2> secs
    When I ask for the error counts in the fields
    Then the error counts dict is "<error_values>"

  Examples:
    | data                     | seed  |   time_1  | params                                          | time_2 |error_values       |
    | data/iris_missing.csv | BigML |    30      | {"fields": {"000000": {"optype": "numeric"}}}   |30      |{"000000": 1}      |
