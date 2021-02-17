Feature: LocalModel

		Scenario Outline: Successfully comparing predictions:
		    Given I create a data source uploading a "<data>" file
		    And I wait until the source is ready less than <time_1> secs
		    And I update the source with "<options>" waiting less than <time_1> secs
		    And I create a dataset
		    And I wait until the dataset is ready less than <time_2> secs
		    And I create a model
		    And I wait until the model is ready less than <time_3> secs
		    And I create a local model
		    When I create a prediction for "<data_input>"
		    Then the prediction for "<objective>" is "<prediction>"
		    And the local prediction for "<data_input>" is "<prediction>"

		    Examples:
		      | data             			| time_1	| time_2 | time_3 | options | data_input                             | objective | prediction  |
		      | data/iris_missing.csv | 30      | 30     | 30     | {"fields": {"000000": {"optype": "numeric"}}, "source_parser": {"missing_tokens": ["foo"]}} | {"sepal length": "foo", "petal length": 3}	|	000004	| Iris-versicolor	|
		      | data/iris_missing.csv | 30      | 30     | 30     | {"fields": {"000000": {"optype": "numeric"}}, "source_parser": {"missing_tokens": ["foo"]}} | {"sepal length": "foo", "petal length": 5, "petal width": 1.5}	|	000004	| Iris-virginica	|


	 Scenario Outline: Successfully changing duplicated field names:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset with "<options>"
        And I wait until the dataset is ready less than <time_2> secs
        And I create a model
        And I wait until the model is ready less than <time_3> secs
        And I create a local model
        Then "<field_id>" field's name is changed to "<new_name>"

				Examples:
				| data	| time_1  | time_2 | time_3 | options | field_id | new_name  |
        | data/iris.csv |  20      | 20     | 30     | {"fields": {"000001": {"name": "species"}}} | 000001 | species1  |
        | data/iris.csv |  20      | 20     | 30     | {"fields": {"000001": {"name": "petal width"}}} | 000001 | petal width3  |
	
	
		Scenario Outline: Successfully comparing predictions:
		    Given I provision a dataset from "<data>" file
		    And I create a model
		    And I wait until the model is ready less than <time_1> secs
		    And I create a local model
		    When I create a prediction for "<data_input>"
		    Then the prediction for "<objective>" is "<prediction>"
		    Then the local prediction for "<data_input>" is "<prediction>"
		
		    Examples:
		      | data	|  time_1  |  data_input    | objective | prediction  |
		      | data/iris.csv | 50     | {"petal width": 0.5}                   | 000004    | Iris-setosa |
		      | data/iris.csv | 50     | {"petal length": 6, "petal width": 2}  | 000004    | Iris-virginica |
		      | data/iris.csv | 50     | {"petal length": 4, "petal width": 1.5}| 000004    | Iris-versicolor |
		      | data/iris_sp_chars.csv | 50     | {"pétal.length": 4, "pétal&width\u0000": 1.5}| 000004    | Iris-versicolor |


	  Scenario Outline: Successfully comparing predictions with text options:
		    Given I create a data source uploading a "<data>" file
		    And I wait until the source is ready less than <time_1> secs
		    And I update the source with "<options>" waiting less than <time_1> secs
		    And I create a dataset
		    And I wait until the dataset is ready less than <time_2> secs
		    And I create a model
		    And I wait until the model is ready less than <time_3> secs
		    And I create a local model
		    When I create a prediction for "<data_input>"
		    Then the prediction for "<objective>" is "<prediction>"
		    And the local prediction for "<data_input>" is "<prediction>"
		
		    Examples:
		      | data             |   time_1  | time_2 | time_3 |    options | data_input                             | objective | prediction  |
		      | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": true, "stem_words": true, "use_stopwords": false, "language": "en"}}}} |{"Message": "Mobile call"}             | 000000    | spam    |
		      | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": true, "stem_words": true, "use_stopwords": false, "language": "en"}}}} |{"Message": "A normal message"}        | 000000    | ham     |
		      | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": false, "use_stopwords": false, "language": "en"}}}} |{"Message": "Mobile calls"}          | 000000    | spam   |
		      | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": false, "use_stopwords": false, "language": "en"}}}} |{"Message": "A normal message"}       | 000000    | ham     |
		      | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": true, "use_stopwords": true, "language": "en"}}}}|{"Message": "Mobile call"}            | 000000    | spam    |
		      | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": true, "use_stopwords": true, "language": "en"}}}} |{"Message": "A normal message"}       | 000000    | ham     |
		      | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"token_mode": "full_terms_only", "language": "en"}}}} |{"Message": "FREE for 1st week! No1 Nokia tone 4 ur mob every week just txt NOKIA to 87077 Get txting and tell ur mates. zed POBox 36504 W45WQ norm150p/tone 16+"}       | 000000    | spam     |
		      | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"token_mode": "full_terms_only", "language": "en"}}}} |{"Message": "Ok"}       | 000000    | ham     |
					#| data/movies.csv |    20      | 20     | 30     | {"fields": {"000007": {"optype": "items", "item_analysis": {"separator": "$"}}}} | {"genres": "Adventure$Action", "timestamp": 993906291, "occupation": "K-12 student"}	| 000009    | 3.92135     |
					#| data/text_missing.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"token_mode": "all", "language": "en"}}, "000000": {"optype": "text", "term_analysis": {"token_mode": "all", "language": "en"}}}} | {}	| 000003    | swap     |
			
		
		Scenario Outline: Successfully comparing predictions with proportional missing strategy:
		    Given I provision a dataset from "<data>" file
		    And I create a model
		    And I wait until the model is ready less than <time_1> secs
		    And I create a local model
		    When I create a proportional missing strategy prediction for "<data_input>"
		    Then the prediction for "<objective>" is "<prediction>"
		    And the confidence for the prediction is <confidence>
		    And I create a local model
		    Then the proportional missing strategy local prediction for "<data_input>" is "<prediction>"
		    Then the confidence of the proportional missing strategy local prediction for "<data_input>" is <confidence>
		
		    Examples:
		      | data              | time_1  | data_input  | objective | prediction      | confidence        |
		      | data/iris.csv     | 50    |   {}  |   000004  |   Iris-setosa   | 0.2629           |

		
		Scenario Outline: Successfully comparing predictions with proportional missing strategy:
		    Given I provision a dataset from "<data>" file
		    And I create a model
		    And I wait until the model is ready less than <time_1> secs
		    And I create a local model
		    When I create a proportional missing strategy prediction for "<data_input>"
		    Then the numerical prediction for "<objective>" is <prediction>
		    And the confidence for the prediction is <confidence>
		    Then the numerical prediction of proportional missing strategy local prediction for "<data_input>" is <prediction>
		    Then the confidence of the proportional missing strategy local prediction for "<data_input>" is <confidence>
		
		    Examples:
		      | data	| time_1  | data_input           | objective | prediction     | confidence |
		      | data/grades.csv | 50     | {}                   | 000005    | 68.62224       | 27.5358    |
		      | data/grades.csv | 50     | {"Midterm": 20}      | 000005    | 40.46667      | 54.89713   |
		      | data/grades.csv | 50     | {"Midterm": 20, "Tutorial": 90, "TakeHome": 500}     | 000005    | 28.06      | 25.65806   |


	  Scenario Outline: Successfully comparing predictions with proportional missing strategy for missing_splits models:
		    Given I provision a dataset from "<data>" file
		    And I create a model with missing splits
		    And I wait until the model is ready less than <time_1> secs
		    And I create a local model
		    When I create a proportional missing strategy prediction for "<data_input>"
		    Then the prediction for "<objective>" is "<prediction>"
		    And the confidence for the prediction is <confidence>
		    And the proportional missing strategy local prediction for "<data_input>" is "<prediction>"
		    And the confidence of the proportional missing strategy local prediction for "<data_input>" is <confidence>
		
		    Examples:
		      | data	| time_1  | data_input  | objective | prediction     | confidence |
		      | data/iris_missing2.csv   | 50     | {"petal width": 1}             | 000004    | Iris-setosa    | 0.8064     |
		      | data/iris_missing2.csv   | 50     | {"petal width": 1, "petal length": 4}             | 000004    | Iris-versicolor    | 0.7847     |

		Scenario Outline: Successfully comparing predictions for models with operating point
        Given I provision a dataset from "<data>" file
        And I create a model
        And I wait until the model is ready less than <time_1> secs
        And I create a local model
        When I create a prediction with model with operating point "<operating_point>" for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        When I create a local prediction with model with operating point "<operating_point>" for "<data_input>"
        Then the local model prediction is "<prediction>"

        Examples:
        | data	| time_1  | data_input | objective	| prediction	| operating_point	|
        | data/iris.csv | 30000	| {"petal width": 4} | 000004	| Iris-setosa	| {"kind": "probability", "threshold": 0.1, "positive_class": "Iris-setosa"}	|
				| data/iris.csv | 30000	| {"petal width": 4} | 000004	| Iris-versicolor	| {"kind": "probability", "threshold": 0.9, "positive_class": "Iris-setosa"}	|
				| data/iris.csv | 30000	| {"sepal length": 4.1, "sepal width": 2.4} | 000004	| Iris-setosa	| {"kind": "confidence", "threshold": 0.1, "positive_class": "Iris-setosa"}	|	
				| data/iris.csv | 30000	| {"sepal length": 4.1, "sepal width": 2.4}| 000004	| Iris-versicolor	| {"kind": "confidence", "threshold": 0.9, "positive_class": "Iris-setosa"}	|
	
	
		Scenario Outline: Successfully comparing predictions for models with operating kind
        Given I provision a dataset from "<data>" file
        And I create a model
        And I wait until the model is ready less than <time_1> secs
        And I create a local model
        When I create a prediction with model with operating kind "<operating_kind>" for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        When I create a local prediction with model with operating kind "<operating_kind>" for "<data_input>"
        Then the local model prediction is "<prediction>"

        Examples:
        | data	| time_1  | data_input | objective	| prediction	| operating_kind	|
        | data/iris.csv | 30000	| {"petal length": 2.46, "sepal length": 5} | 000004	| Iris-versicolor	| probability	|
        | data/iris.csv | 30000	| {"petal length": 2.46, "sepal length": 5} | 000004	| Iris-versicolor	| confidence	|
				| data/iris.csv | 30000	| {"petal length": 2} | 000004	| Iris-setosa	| probability	|
        | data/iris.csv | 30000	| {"petal length": 2} | 000004	| Iris-setosa	| confidence	|
        
        
    Scenario Outline: Successfully creating a prediction from a local model in a json file:
      Given I create a local model from a "<model>" file
      And the local prediction for "<data_input>" is "<prediction>"
      And the confidence of the local prediction for "<data_input>" is <confidence>

      Examples:
      | model                | data_input             | prediction  | confidence  |
      | data/iris_model.json | {"petal length": 0.5}  | Iris-setosa | 0.90594     |


    Scenario Outline: Successfully creating a multiple prediction from a local model in a json file:
      Given I create a local model from a "<model>" file
      And the multiple local prediction for "<data_input>" is "<prediction>"

      Examples:
      | model                | data_input             | prediction  |
      | data/iris_model.json | {"petal length": 3}    | [{"probability":0.5060240963855421,"confidence":0.4006020980792863,"prediction":"Iris-versicolor","count":42},{"probability":0.4939759036144578,"confidence":0.3890868795664999,"prediction":"Iris-virginica","count":41}] |


    Scenario Outline: Successfully creating a prediction from local model
        Given I provision a dataset from "<data>" file
        And I create a model
        And I wait until the model is ready less than <time_1> secs
        And I create a local model
        Then the local prediction for "<objective1>" is "<prediction1>"
        Then the local prediction for "<objective2>" is "<prediction2>"

        Examples:
          | data  | time_1  | objective1 | prediction1  | objective2 | prediction2  |
          | data/iris.csv | 50     | {"petal width": 0.5}    | Iris-setosa | {"000003": 0.5}    | Iris-setosa |

     
     Scenario Outline: Successfully creating a model from a dataset list and predicting with it using median:
        Given I provision a dataset from "<data>" file
        Then I create a model
        And I wait until the model is ready less than <time_1> secs
        And I create a local multi model
        And I create a local mm median batch prediction using "<input_data>" with prediction <prediction>

        Examples:
          | data	|  time_1  |  input_data |  prediction    |
          | data/grades.csv    | 30     |  {"Tutorial": 99.47, "Midterm": 53.12, "TakeHome": 87.96}    |    63.33  |
          
              
     Scenario Outline: Successfully creating a prediction from a multi model:
        Given I provision a dataset from "<data>" file
        And I create a model with "<params>"
        And I wait until the model is ready less than <time_1> secs
        And I create a model with "<params>"
        And I wait until the model is ready less than <time_1> secs
        And I create a model with "<params>"
        And I wait until the model is ready less than <time_1> secs
        And I retrieve a list of remote models tagged with "<tag>"
        And I create a local multi model
        Then the local multi prediction for "<data_input>" is "<prediction>"

        Examples:
        | data	| time_1  | params                         |  tag  |  data_input    | prediction  |
        | data/iris.csv | 30     | {"tags":["mytag"]} | mytag |  {"petal width": 0.5} | Iris-setosa |


    Scenario Outline: Successfully creating a local batch prediction from a multi model:
        Given I provision a dataset from "<data>" file
        And I create a model with "<params>"
        And I wait until the model is ready less than <time_1> secs
        And I create a model with "<params>"
        And I wait until the model is ready less than <time_1> secs
        And I create a model with "<params>"
        And I wait until the model is ready less than <time_1> secs
        And I retrieve a list of remote models tagged with "<tag>"
        And I create a local multi model
        Then I create a batch multimodel prediction for "<data_inputs>" and predictions "<predictions>"

        Examples:
            | data          | time_1  | params             |  tag  |  data_inputs                                                   | predictions                       |
            | data/iris.csv |  30     | {"tags":["mytag"]} | mytag |  [{"petal width": 0.5}, {"petal length": 6, "petal width": 2}] | ["Iris-setosa", "Iris-virginica"] |
     
     
     Scenario Outline: Successfully creating a batch prediction from a multi model:
		    Given I provision a dataset from "<data>" file
		    And I create a model with "<params>"
		    And I wait until the model is ready less than <time_1> secs
		    And I create a model with "<params>"
		    And I wait until the model is ready less than <time_1> secs
		    And I create a model with "<params>"
		    And I wait until the model is ready less than <time_1> secs
		    And I retrieve a list of remote models tagged with "<tag>"
		    And I create a local multi model
		    When I create a batch prediction for "<data_input>" and save it in "<path>"
		    And I combine the votes in "<path>"
		    Then the plurality combined predictions are "<predictions>"
		    And the confidence weighted predictions are "<predictions>"
		
		  Examples:
		    | data          | time_1  | params                         |  tag  |  data_input    | path | predictions  |
		    | data/iris.csv |  30     | {"tags":["mytag"]} | mytag |  [{"petal width": 0.5}, {"petal length": 6, "petal width": 2}, {"petal length": 4, "petal width": 1.5}]  | data | ["Iris-setosa", "Iris-virginica", "Iris-versicolor"] |


    Scenario Outline: Successfully comparing remote and local predictions with raw date input
        Given I provision a dataset from "<data>" file
        And I create a model
        And I wait until the model is ready less than <time_1> secs
        And I create a local model
        When I create a prediction for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        And the local prediction for "<data_input>" is "<prediction>"

        Examples:
        | data            | time_1 | data_input                             | objective | prediction  |
        | data/dates2.csv | 30     | {"time-1": "1910-05-08T19:10:23.106", "cat-0":"cat2"}  | 000002    | -1.01482    |
        | data/dates2.csv | 30     | {"time-1": "1920-06-30T20:21:20.320", "cat-0":"cat1"}  | 000002    | 0.78406    |
        | data/dates2.csv | 30     | {"time-1": "1932-01-30T19:24:11.440",  "cat-0":"cat2"}  | 000002    | -0.98757    |
        | data/dates2.csv | 30     | {"time-1": "1950-11-06T05:34:05.252", "cat-0":"cat1"}  | 000002    | 0.27538    |
        | data/dates2.csv | 30     | {"time-1": "1969-7-14 17:36", "cat-0":"cat2"}  | 000002    | -0.06256    |
        | data/dates2.csv | 30     | {"time-1": "2001-01-05T23:04:04.693", "cat-0":"cat2"}  | 000002    | 0.9832    |
        | data/dates2.csv | 30     | {"time-1": "2011-04-01T00:16:45.747", "cat-0":"cat2"}  | 000002    | -0.5977    |
        | data/dates2.csv | 30     | {"time-1": "1969-W29-1T17:36:39Z", "cat-0":"cat1"}  | 000002    | -0.06256    |
        | data/dates2.csv | 30     | {"time-1": "Mon Jul 14 17:36 +0000 1969", "cat-0":"cat1"}  | 000002    | -0.06256    |


    Scenario Outline: Successfully comparing remote and local predictions for models
        Given I provision a dataset from "<data>" file
        And I create a model from a dataset with "<params>"
        And I wait until the model is ready less than <time_1> secs
        When I create a prediction for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        And I create a local model
        And the local prediction for "<data_input>" is "<prediction>"

        Examples:
        | data  | time_1  | data_input | objective  | prediction | params  |
        | data/iris.csv | 120 | {} | 000004 | Iris-setosa | {}  |
        | data/iris.csv | 120 | {} | 000004 | Iris-versicolor    | {"default_numeric_value": "mean"}  |
