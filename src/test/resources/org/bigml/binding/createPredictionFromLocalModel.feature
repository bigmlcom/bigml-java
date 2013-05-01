Feature: Create Predictions from Local Model
    In order to create a prediction
    I need to have a local model first	
	
  Scenario Outline: Successfully creating a prediction from local model

        Given a instantiated BigML client
        And I create a model from the data source "<data>" waiting less than <time_1>, <time_2> and <time_3> secs in each step
        And I create the local model
        Then the local prediction for "<objective1>" is "<prediction1>"
        Then the local prediction by name for "<objective2>" is "<prediction2>"
    	

  Examples: 
        | data	| time_1  | time_2 | time_3 | objective1 | prediction1  | objective2 | prediction2  |
        | data/iris.csv | 15      | 15     | 15     | {"petal length": 1}    | Iris-setosa | {"000002": 1}    | Iris-setosa |
