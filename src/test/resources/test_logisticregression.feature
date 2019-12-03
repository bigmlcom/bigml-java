Feature: LogisticRegression REST api calls

    Scenario Outline: Successfully creating a logisticregression from a dataset:
        Given I provision a dataset from "<data>" file
        And I create a logisticregression from a dataset
        And I wait until the logisticregression is ready less than <time_1> secs
        And I update the logisticregression name to "<logisticregression_name>"
        When I wait until the logisticregression is ready less than <time_2> secs
        Then the logisticregression name is "<logisticregression_name>"

        Examples:
        | data	| time_1  | time_2 | logisticregression_name |
        | data/iris.csv | 50	| 50	| my new logisticregression name |
