Feature: Create Predictions from Local Model
    In order to create a prediction
    I need to have a local model first	
	
  Scenario Outline: Successfully creating a prediction from local mode
		
		Given a instantiated BigML client
		And I get the model with modelId "<modelId>"
		And I create the local model
		Then the local prediction for "<objective1>" is "<prediction1>"
		Then the local prediction by name for "<objective2>" is "<prediction2>"
    	

  Examples: 
        | modelId	| objective1 | prediction1  | objective2 | prediction2  |
        | model/xxx(Replace with valid modelId in dev mode) | {"petal length": 1}    | Iris-setosa | {"000002": 1}    | Iris-setosa |


   
   
   
        