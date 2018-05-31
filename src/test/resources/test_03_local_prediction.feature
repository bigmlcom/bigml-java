Feature: Create Predictions
    In order to create a prediction
    I need to create a model first

    Scenario Outline: Successfully creating a prediction from a local model in a json file:
      Given I create a local model from a "<model>" file
      And the local prediction for "<data_input>" is "<prediction>"
      And the confidence of the local prediction for "<data_input>" is <confidence>
      Then delete test data

      Examples:
      | model                | data_input             | prediction  | confidence  |
      | data/iris_model.json | {"petal length": 0.5}  | Iris-setosa | 0.90594     |


    Scenario Outline: Successfully creating a multiple prediction from a local model in a json file:
      Given I create a local model from a "<model>" file
      And the multiple local prediction for "<data_input>" is "<prediction>"
      Then delete test data

      Examples:
      | model                | data_input             | prediction  |
      | data/iris_model.json | {"petal length": 3}    | [{"probability":0.5060240963855421,"confidence":0.4006020980792863,"prediction":"Iris-versicolor","count":42},{"probability":0.4939759036144578,"confidence":0.3890868795664999,"prediction":"Iris-virginica","count":41}] |





    Scenario Outline: Successfully creating a prediction from local model
        Given that I use development mode with seed="<seed>"
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I add the unitTest tag to the data source waiting less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a model
        And I wait until the model is ready less than <time_3> secs
        And I create a local model
        Then the local prediction for "<objective1>" is "<prediction1>"
        Then the local prediction by name for "<objective2>" is "<prediction2>"
        Then delete test data

        Examples:
          | data  | seed      | time_1  | time_2 | time_3 | objective1 | prediction1  | objective2 | prediction2  |
          | data/iris.csv | BigML |  15      | 15     | 15     | {"petal width": 0.5}    | Iris-setosa | {"000003": 0.5}    | Iris-setosa |
