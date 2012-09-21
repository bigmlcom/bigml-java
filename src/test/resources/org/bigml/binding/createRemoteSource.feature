Feature: Create Predictions
    In order to create a prediction
    I need to create a model first	
	
  Scenario Outline: Successfully creating a prediction
		
		Given a instantiated BigML client
    	And I create a remote source from the "<url>" url
    	And I wait until the remote source is ready less than <time_1> secs
    	Then test listing remote source
        Then delete test remote source data
       
  Examples: 
        | url	| time_1  |
        | https://static.bigml.com/csv/iris.csv | 30     |


   
   
   
        