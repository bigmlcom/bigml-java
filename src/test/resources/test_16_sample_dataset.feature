Feature: Download the sample to filesystem
    In order to download a sample
    I need to create a sample

    Scenario Outline: Successfully creating a sample from a dataset:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a sample from a dataset
        And I wait until the sample is ready less than <time_3> secs
        And I update the sample with "<params>" waiting less than <time_4> secs
        And I wait until the sample is ready less than <time_3> secs
		Then the sample name is <name>
        Then delete test data

        Examples:
        | data          | time_1  | time_2 | time_3 |   time_4  |   params                            |   name                 |
        | data/iris.csv |   30    |  30    |  30    |   50      |   {"name": "my new sample name"}    | "my new sample name"   |


    Scenario Outline: Successfully creating, reading and downloading a sample:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a sample from a dataset
        And I wait until the sample is ready less than <time_3> secs
        And I download the sample file to "<local_file>" with <rows> rows and "<seed>" seed
        Then the sample file "<expected_file>" is like "<local_file>"
        Then delete test data

        Examples:
        | data          | seed  | time_1  | time_2 | time_3 | rows  |   expected_file                 |   local_file                    |
        | data/iris.csv | BigML |   30    |  30    |  30    | 50    |   data/expected_iris_sample.csv | data/exported_sample_iris.csv   |