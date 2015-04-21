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
      | data/iris_model.json | {"petal length": 3}    | [{"count":42,"prediction":"Iris-versicolor","probability":0.5060240963855421,"confidence":0.4006020980792863},{"count":41,"prediction":"Iris-virginica","probability":0.4939759036144578,"confidence":0.3890868795664999}] |
