Feature: Create Predictions
    In order to create a prediction
    I need to create a model first

    Scenario Outline: Successfully creating a prediction from a local model in a json file:
      Given I create a local model from a "<model>" file
      And the local prediction for "<data_input>" is "<prediction>"
      And the confidence of the local prediction for "<data_input>" is <confidence>
      Then delete test data

      Examples:
      | model                | data_input             | objective | prediction  | confidence  |
      | data/iris_model.json | {"petal length": 0.5}  | 000004    | Iris-setosa | 0.90594     |
