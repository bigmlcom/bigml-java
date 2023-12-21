@locallinearregression
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


    Scenario Outline: Successfully comparing remote and local predictions with raw date input for linear regression
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
        | data/dates2.csv | 30000 | {"time-1": "1910-05-08T19:10:23.106", "cat-0":"cat2"} | 000002 |  -0.01284 | {}  |
        | data/dates2.csv | 30000 | {"time-1": "1920-06-30T20:21:20.320", "cat-0":"cat1"} | 000002 |  -0.09459 | {}  |
        | data/dates2.csv | 30000 | {"time-1": "1932-01-30T19:24:11.440",  "cat-0":"cat2"} | 000002 |  -0.02259 | {}  |
        | data/dates2.csv | 30000 | {"time-1": "1950-11-06T05:34:05.252", "cat-0":"cat1"} | 000002 |  -0.06754 | {}  |
        | data/dates2.csv | 30000 | {"time-1": "2001-01-05T23:04:04.693", "cat-0":"cat2"} | 000002 |  0.05204 | {}  |
        | data/dates2.csv | 30000 | {"time-1": "2011-04-01T00:16:45.747", "cat-0":"cat2"} | 000002 |  0.05878 | {}  |


    Scenario Outline: Successfully comparing remote and local predictions for Linear regressions
        Given I provision a dataset from "<data>" file
        And I create a linearregression from a dataset with "<params>"
        And I wait until the linearregression is ready less than <time_1> secs
        When I create a linearregression prediction for "<data_input>"
        Then the linearregression prediction is "<prediction>"
        And I create a local linearregression
        And I create a local linearregression prediction for "<data_input>"
        Then the local linearregression prediction is "<prediction>"

        Examples:
        | data  | time_1  | data_input | prediction | params  |
        | data/grades.csv | 120 | {"Prefix": 5, "Assignment": 57.14, "Tutorial": 34.09, "Midterm": 64, "TakeHome": 40, "Final": 50} |  54.695511642999996 | {}  |
        | data/grades.csv | 120 | {} |  100.332461974 | {"default_numeric_value": "maximum"}  |
