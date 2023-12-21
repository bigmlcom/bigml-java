@statisticaltest
Feature: StatiticalTest REST api calls

    Scenario Outline: Successfully creating a statiticaltest from a dataset:
        Given I provision a dataset from "<data>" file
        And I create a statisticaltest from a dataset
        And I wait until the statisticaltest is ready less than <time_1> secs
        And I update the statisticaltest name to "<statisticaltest_name>"
        When I wait until the statisticaltest is ready less than <time_2> secs
        Then the statisticaltest name is "<statisticaltest_name>"

        Examples:
        | data	|  time_1  | time_2 | statisticaltest_name |
        | data/iris.csv | 20     | 20 | my new statisticaltest name |
