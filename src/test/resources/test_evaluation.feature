Feature: Evaluation REST api

		 Scenario Outline: Successfully creating an evaluation:
        Given I provision a dataset from "<data>" file
        And I create a model
        And I wait until the model is ready less than <time_1> secs
        When I create an evaluation for the model with the dataset
        And I wait until the evaluation is ready less than <time_2> secs
        Then the measured "<measure>" is <value>

        Examples:
        | data  | time_1  | time_2 | measure    | value  |
        | data/iris.csv | 50    | 50    | average_phi   | 1 |


    Scenario Outline: Successfully creating an evaluation for an ensemble:
        Given I provision a dataset from "<data>" file
        And I create an ensemble of <number_of_models> models
        And I wait until the ensemble is ready less than <time_1> secs
        When I create an evaluation for the ensemble with the dataset
        And I wait until the evaluation is ready less than <time_2> secs
        Then the measured "<measure>" is equals to <value>

        Examples:
        | data  | time_1  | time_2 | number_of_models | measure | value  |
        | data/iris.csv | 100   | 100   | 5 | average_phi   | 0.97064   |


		 Scenario Outline: Successfully creating an evaluation for an ensemble:
        Given I provision a dataset from "<data>" file
        And I create an ensemble of <number_of_models> models
        And I wait until the ensemble is ready less than <time_1> secs
        When I create an evaluation for the ensemble with the dataset and "<params>"
        And I wait until the evaluation is ready less than <time_2> secs
        Then the measured "<measure>" is equals to <value>

        Examples:
        | data	| time_1  | time_2 | number_of_models | params	| measure	| value  |
        | data/iris.csv | 100	| 100	| 5	| {"combiner": 0}	| average_phi	| 0.98029   |
        | data/iris.csv | 100	| 100	| 5	| {"combiner": 1}	| average_phi	| 0.95061   |
        | data/iris.csv | 100	| 100	| 5	| {"combiner": 2}	| average_phi	| 0.98029   |
				| data/iris.csv | 100	| 100	| 5	| {"operating_kind": "votes"}	| average_phi	| 0.98029   |
        | data/iris.csv | 100	| 100	| 5	| {"operating_kind": "probability"}	| average_phi	| 0.97064   |
        | data/iris.csv | 100	| 100	| 5	| {"operating_kind": "confidence"}	| average_phi	| 0.950619   |

    Scenario Outline: Successfully creating an evaluation for a linear regression:
        Given I provision a dataset from "<data>" file
        And I create a linearregression from a dataset
        And I wait until the linearregression is ready less than <time_1> secs
        When I create an evaluation for the linear regression with the dataset
        And I wait until the evaluation is ready less than <time_2> secs
        Then the measured "<measure>" is equals to <value>

        Examples:
        | data  | time_1  | time_2 | tlp | measure  | value  |
        | data/iris.csv | 800   | 1000  | 5 | r_squared | 0.95382   |


        Scenario Outline: Successfully creating an evaluation for a logistic regression:
        Given I provision a dataset from "<data>" file
        And I create a logisticregression from a dataset
        And I wait until the logisticregression is ready less than <time_1> secs
        When I create an evaluation for the logistic regression with the dataset
        And I wait until the evaluation is ready less than <time_2> secs
        Then the measured "<measure>" is equals to <value>

        Examples:
        | data  | time_1  | time_2 | tlp | measure  | value  |
        | data/iris.csv | 3000  | 1000  | 5 | average_phi   | 0.89054   |


    Scenario Outline: Successfully creating an evaluation for a deepnet:
        Given I provision a dataset from "<data>" file
        And I create a deepnet from a dataset
        And I wait until the deepnet is ready less than <time_1> secs
        When I create an evaluation for the deepnet with the dataset
        And I wait until the evaluation is ready less than <time_2> secs
        Then the measured "<measure>" is equals to <value>

        Examples:
        | data  | time_1  | time_2 | tlp | measure  | value  |
        | data/iris.csv | 800   | 1000  | 5 | average_phi   | 0.95007   |
