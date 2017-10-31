Feature: Testing Deepnet REST api calls
    In order to create an deepnet
    I need to create a dataset first

    Scenario Outline: Successfully creating deepnets from a dataset:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a deepnet from a dataset
        And I wait until the deepnet is ready less than <time_3> secs
        And I update the deepnet name to "<deepnet_name>"
        When I wait until the deepnet is ready less than <time_4> secs
        Then the deepnet name is "<deepnet_name>"
        Then I delete the deepnet

        Examples:
        | data                | time_1  | time_2 | time_3 | time_4 | deepnet_name |
        | data/iris.csv | 50      | 50     | 50     | 100 | my new deepnet name |
