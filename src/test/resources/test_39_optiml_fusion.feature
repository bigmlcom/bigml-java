Feature: Testing REST api calls
    
    Scenario Outline: Successfully creating an optiml from a dataset:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create an optiml from a dataset
        And I wait until the optiml is ready less than <time_3> secs
        And I update the optiml name to "<optiml_name>"
        When I wait until the optiml is ready less than <time_4> secs
        Then the optiml name is "<optiml_name>"
        Then I delete the optiml

        Examples:
        | data                | time_1  | time_2 | time_3 | time_4 | optiml_name |
        | data/iris.csv | 50      | 50     | 1000     | 100 | my new optiml name |
