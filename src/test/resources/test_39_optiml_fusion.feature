Feature: Testing REST api calls
    
    Scenario Outline: Successfully creating an optiml from a dataset:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create an optiml from a dataset
        And I wait until the optiml is ready less than <time_3> secs
        And I update the optiml name to "<optiml_name>"
        When I wait until the optiml is ready less than <time_4> secs
        Then the optiml name is "<optiml_name>"
        Then I delete the optiml

        Examples:
        | data                | time_1  | time_2 | time_3 | time_4 | optiml_name |
        | data/iris.csv | 50      | 50     | 1000     | 100 | my new optiml name |
   
    Scenario Outline: Successfully creating fusion from models:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a model
        And I wait until the model is ready less than <time_3> secs
        And I create a model
        And I wait until the model is ready less than <time_3> secs
        And I create a model
        And I wait until the model is ready less than <time_3> secs
        And I create a fusion from models
        And I wait until the fusion is ready less than <time_3> secs
        And I update the fusion name to "<fusion_name>"
        When I wait until the fusion is ready less than <time_4> secs
        Then the fusion name is "<fusion_name>"
        When I create a prediction for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        When I create an evaluation for the fusion with the dataset
        And I wait until the evaluation is ready less than <time_4> secs
        Then the measured "<measure>" is <value>
        Then I delete the fusion

        Examples:
        | data                | time_1  | time_2 | time_3 | time_4 | fusion_name | data_input    | objective | prediction  | measure       | value  |
        | data/iris.csv | 50      | 50     | 50     | 50 | my new fusion name | {"petal width": 1.75, "petal length": 2.45}	| 000004    | Iris-setosa | average_phi   | 1      |
        