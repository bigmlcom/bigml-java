Feature: Assocaition REST api calls

    Scenario Outline: Successfully creating associations from a dataset:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create an association from a dataset
        And I wait until the association is ready less than <time_3> secs
        And I update the association name to "<association_name>"
        When I wait until the association is ready less than <time_4> secs
        Then the association name is "<association_name>"
        Then I delete the association
        Then delete test data

        Examples:
        | data                | time_1  | time_2 | time_3 | time_4 | association_name |
        | data/iris.csv | 10      | 10     | 20     | 20 | my new association name |
