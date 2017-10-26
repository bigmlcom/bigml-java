Feature: Create a split dataset
    In order to create a split dataset
    I need to create an origin dataset

    Scenario Outline: Successfully creating a split dataset:
        Given that I use production mode with seed="<seed>"
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I add the unitTest tag to the data source waiting less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a dataset extracting a <rate> sample
        And I wait until the dataset is ready less than <time_3> secs
        When I compare the datasets' instances
        Then the proportion of instances between datasets is <rate>
        Then delete test data

	Examples:
		| data             | seed      | time_1  | time_2 | time_3 | rate |
        | data/iris.csv    | BigML     |  10     | 10     | 10     | 0.8  |
