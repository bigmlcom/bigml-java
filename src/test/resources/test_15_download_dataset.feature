Feature: Create and read a public dataset
    In order to read a public dataset
    I need to create a public dataset

    Scenario Outline: Successfully exporting a dataset:
        Given that I use production mode with seed="<seed>"
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I add the unitTest tag to the data source waiting less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I download the dataset file to "<local_file>"
		Then the dataset file "<data>" is like "<local_file>"
        Then delete test data

        Examples:
        | data          | seed      | time_1  | time_2 | local_file |
        | data/iris.csv | BigML |   30      | 30     | data/exported_iris.csv |
