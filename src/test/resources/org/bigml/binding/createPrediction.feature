Feature: Create Predictions
    In order to create a prediction
    I need to create a model first	
	
  Scenario Outline: Successfully creating a prediction
		
		Given a instantiated BigML client
    	And I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a model
        And I wait until the model is ready less than <time_3> secs
        And I create a ensemble
        And I wait until the ensemble is ready less than <time_3> secs
        And I create a evaluation
        And I wait until the evaluation is ready less than <time_4> secs
        When I create a prediction by name=<by_name> for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        When I create a prediction with ensemble by name=<by_name> for "<data_input>"
        Then the prediction with ensemble for "<objective>" is "<predictionEnsemble>"
        
        Then test listing
        
        Then delete test data

  Examples: 
        | data	| time_1  | time_2 | time_3 | time_4 | by_name    | data_input    | objective | prediction  | predictionEnsemble  |
        | data/iris.csv | 15      | 15     | 15     | 15     | true | {"petal width": 0.5} | 000004    | Iris-setosa | Iris-setosa |

		
   
   
   
        