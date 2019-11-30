Feature: LogisticRegression REST api calls

    Scenario Outline: Successfully creating a logisticregression from a dataset:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a logisticregression from a dataset
        And I wait until the logisticregression is ready less than <time_3> secs
        And I update the logisticregression name to "<logisticregression_name>"
        When I wait until the logisticregression is ready less than <time_4> secs
        Then the logisticregression name is "<logisticregression_name>"

        Examples:
        | data	|  time_1  | time_2 | time_3 | time_4 | logisticregression_name |
        | data/iris.csv | 10      | 10     | 20     | 20 | my new logisticregression name |
