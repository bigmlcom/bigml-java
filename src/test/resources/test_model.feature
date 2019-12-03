Feature: Model REST api
		
		Scenario Outline: Successfully creating a prediction:
	      Given I provision a dataset from "<data>" file
	      And I create a model
	      And I wait until the model is ready less than <time_1> secs
	      When I create a prediction for "<data_input>"
	      Then the prediction for "<objective>" is "<prediction>"
	
	      Examples:
	      | data                |  time_1  | data_input    | objective | prediction  |
	      | data/iris.csv | 50      | {"petal width": 0.5} | 000004    | Iris-setosa |
	      | data/iris_sp_chars.csv |  50      | {"pétal&width\u0000": 0.5} | 000004    | Iris-setosa |
		
		Scenario Outline: Successfully creating a prediction using a public model:
        Given I provision a dataset from "<data>" file
        And I create a model
        And I wait until the model is ready less than <time_1> secs
        And I make the model public
        And I wait until the model is ready less than <time_1> secs
        And I check the model status using the model's public url
        When I create a prediction for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"

        Examples:
        | data                | time_1  | data_input    | objective | prediction  |
        | data/iris.csv |  50      | {"petal width": 0.5} | 000004    | Iris-setosa |


    Scenario Outline: Successfully creating a prediction using a shared model:
        Given I provision a dataset from "<data>" file
        And I create a model
        And I wait until the model is ready less than <time_1> secs
        And I make the model shared
        And I wait until the model is ready less than <time_1> secs
        And I get the model sharing info
        And I check the model status using the model's shared url
        And I check the model status using the model's shared key
        And I create a local model
        When I create a prediction for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"

        Examples:
        | data	| time_1  | data_input    | objective | prediction  |
        | data/iris.csv | 50	| {"petal width": 0.5} | 000004    | Iris-setosa |
        
        
     Scenario Outline: Successfully creating a model from a dataset list:
        Given I provision a data source from "<data>" file
        And I create a dataset
        And I wait until the dataset is ready less than <time_1> secs
        And I store the dataset id in a list
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I store the dataset id in a list
        Then I create a model from a dataset list
        And I wait until the model is ready less than <time_3> secs
        And I check the model stems from the original dataset list

        Examples:
          | data	|  time_1  | time_2 | time_3 |
          | data/iris.csv    |   30     | 30     | 30     |
          