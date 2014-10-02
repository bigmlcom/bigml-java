Feature: Create and read a public dataset
    In order to read a public dataset
    I need to create a public dataset

    Scenario Outline: Successfully creating and reading a public dataset:
        Given that I use production mode with seed="<seed>"
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I add the unitTest tag to the data source waiting less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        
        And I make the dataset public
        And I wait until the dataset is ready less than <time_3> secs
        When I get the dataset status using the dataset's public url
        Then the dataset's status is FINISHED
		Then delete test data
		
        Examples:
        | data                | seed      | time_1  | time_2 | time_3 |
        | data/iris.csv | BigML |  10      | 10     | 10     |
