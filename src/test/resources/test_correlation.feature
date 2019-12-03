Feature: Correlation REST api calls

    Scenario Outline: Successfully creating a correlation from a dataset:
        Given I provision a dataset from "<data>" file
        And I create a correlation from a dataset
        And I wait until the correlation is ready less than <time_1> secs
        And I update the correlation name to "<correlation_name>"
        When I wait until the correlation is ready less than <time_2> secs
        Then the correlation name is "<correlation_name>"

        Examples:
        | data	|  time_1  | time_2 | correlation_name |
        | data/iris.csv | 50	| 50	| my new correlation name |
