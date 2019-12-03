Feature: LocalLinearRegression
	
	Scenario Outline: Successfully comparing predictions for linear regression
        Given I provision a dataset from "<data>" file
        And I create a linearregression with objective "<objective>" and params "<params>"
        And I wait until the linearregression is ready less than <time_1> secs
        When I create a linearregression prediction for "<data_input>"  
        Then the linearregression prediction is "<prediction>"
        And I create a local linearregression
        And I create a local linearregression prediction for "<data_input>"
        Then the local linearregression prediction is "<prediction>"

        Examples:
        | data  | time_1  | data_input | objective | prediction | params  |
        | data/grades.csv | 30000 | {"000000": 1, "000001": 1, "000002": 1} | 000005 |  29.63024 | {"input_fields": ["000000", "000001", "000002"]}  |
        | data/iris.csv | 30000 | {"000000": 1, "000001": 1, "000004": "Iris-virginica"} |  000003   |  1.21187 | {"input_fields": ["000000", "000001", "000004"]}   |
        | data/movies.csv | 30000 | {"000007": "Action"} | 000009    |  4.33333 | {"input_fields": ["000007"]}   |
        