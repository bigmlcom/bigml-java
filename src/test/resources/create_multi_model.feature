Feature:  Create a model from a dataset list
    In order to create a model from a list of datasets
    I need to create some datasets first

    Scenario Outline: Successfully creating a model from a dataset list:
        Given that I use development mode with seed="<seed>"
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I add the unitTest tag to the data source waiting less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I store the dataset id in a list
        And I create a dataset
        And I wait until the dataset is ready less than <time_3> secs
        And I store the dataset id in a list
        Then I create a model from a dataset list
        And I wait until the model is ready less than <time_4> secs
        And I check the model stems from the original dataset list
        Then delete test data

        Examples:
          | data                | seed      |  time_1  | time_2 | time_3 |  time_4 |
          | data/iris.csv    | BigML     |   10     | 10     | 10     |  10     |
