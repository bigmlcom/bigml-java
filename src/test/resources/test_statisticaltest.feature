Feature: StatiticalTest REST api calls

    Scenario Outline: Successfully creating a statiticaltest from a dataset:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a statisticaltest from a dataset
        And I wait until the statisticaltest is ready less than <time_3> secs
        And I update the statisticaltest name to "<statisticaltest_name>"
        When I wait until the statisticaltest is ready less than <time_4> secs
        Then the statisticaltest name is "<statisticaltest_name>"
        Then delete test data

        Examples:
        | data	|  time_1  | time_2 | time_3 | time_4 | statisticaltest_name |
        | data/iris.csv | 10      | 10     | 20     | 20 | my new statisticaltest name |
