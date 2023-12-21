@association
Feature: Association REST api calls

    Scenario Outline: Successfully creating associations from a dataset:
        Given I provision a dataset from "<data>" file
        And I create an association from a dataset
        And I wait until the association is ready less than <time_1> secs
        And I update the association name to "<association_name>"
        When I wait until the association is ready less than <time_2> secs
        Then the association name is "<association_name>"
        Then I delete the association

        Examples:
        | data	| time_1  | time_2 | association_name |
        | data/iris.csv | 50      | 50     | my new association name |
