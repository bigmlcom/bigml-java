Feature: Create Predictions
  In order to compare a remote prediction with a local prediction
  I need to create a model first
  Then I need to create a local model


  Scenario Outline: Successfully comparing predictions with proportional missing strategy:
    Given that I use production mode with seed="<seed>"
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I add the unitTest tag to the data source waiting less than <time_1> secs
    And I create a dataset
    And I wait until the dataset is ready less than <time_2> secs
    And I create a model
    And I wait until the model is ready less than <time_3> secs
    When I create a proportional missing strategy prediction by name=<by_name> for "<data_input>"
    Then the prediction for "<objective>" is "<prediction>"
    And the confidence for the prediction is <confidence>
    And I create a local model
    Then the proportional missing strategy local prediction for "<data_input>" is "<prediction>"
    Then the confidence of the proportional missing strategy local prediction for "<data_input>" is <confidence>
    Then delete test data

    Examples:
      | data              | seed  | time_1  | time_2  | time_3  | by_name | data_input                                        | objective | prediction      | confidence        |
      | data/iris.csv     | BigML |   10    |   10    |   10    | true    |   {}                                              |   000004  |   Iris-setosa   | 0.26289           |
      | data/grades.csv | BigML |   10      | 10     | 10     | true     |   {}                   | 000005    | 68.62224       | 27.5358    |
      | data/grades.csv | BigML |   10      | 10     | 10     | true     |   {"Midterm": 20}      | 000005    | 46.69889      | 37.27594297134128   |
      | data/grades.csv | BigML |   10      | 10     | 10     | true     |   {"Midterm": 20, "Tutorial": 90, "TakeHome": 100}     | 000005    | 28.06      | 24.86634   |
