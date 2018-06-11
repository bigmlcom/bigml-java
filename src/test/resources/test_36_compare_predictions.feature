Feature: Testing Deepnet REST api calls
    In order to create an deepnet
    I need to create a dataset first

    Scenario Outline: Successfully comparing predictions for deepnets
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


     Scenario Outline: Successfully comparing predictions for logistic regressions with operating kind
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a logisticregression from a dataset
        And I wait until the logisticregression is ready less than <time_3> secs
        And I create a local logisticregression
        When I create a prediction with logisticregression with operating kind "<operating_kind>" for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        When I create a local prediction with logisticregression with operating kind "<operating_kind>" for "<data_input>"
        Then the local logisticregression prediction is "<prediction>"

        Examples:
        | data	| time_1  | time_2 | time_3 | data_input | objective	| prediction	| operating_kind	|
        | data/iris.csv | 50      | 50     | 30000	| {"petal length": 5} | 000004	| Iris-versicolor	| probability	|
        | data/iris.csv | 50      | 50     | 30000	| {"petal length": 2} | 000004	| Iris-setosa	| probability	|
