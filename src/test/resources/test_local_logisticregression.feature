Feature: LocalLogisticRegression
		
		Scenario Outline: Successfully comparing logistic regression predictions
        Given I provision a dataset from "<data>" file
        And I create a logisticregression from a dataset
        And I wait until the logisticregression is ready less than <time_1> secs
        And I create a local logisticregression
        When I create a logisticregression prediction for "<data_input>"  
        Then the logisticregression prediction is "<prediction>"
        And I create a local logisticregression prediction for "<data_input>"
        Then the local logisticregression prediction is "<prediction>"
        
        Examples:
        | data  | time_1  | data_input | prediction |
        | data/iris.csv | 50 | {"petal width": 0.5, "petal length": 0.5, "sepal width": 0.5, "sepal length": 0.5} |  Iris-versicolor |
        | data/iris.csv | 50 | {"petal width": 2, "petal length": 6, "sepal width": 0.5, "sepal length": 0.5} |  Iris-versicolor |
        | data/iris.csv | 50 | {"petal width": 1.5, "petal length": 4, "sepal width": 0.5, "sepal length": 0.5} |  Iris-versicolor |
      	| data/iris.csv | 50 | {"petal length": 1} |  Iris-setosa  |
        | data/iris_sp_chars.csv | 50 | {"pétal.length": 4, "pétal&width\u0000": 1.5, "sépal&width": 0.5, "sépal.length": 0.5} |  Iris-versicolor |
        | data/price.csv | 50  | {"Price": 1200} |  Product1 |
  

		Scenario Outline: Successfully comparing predictions with text options
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I update the source with "<options>" waiting less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a logisticregression from a dataset
        And I wait until the logisticregression is ready less than <time_3> secs
        And I create a local logisticregression        
        When I create a logisticregression prediction for "<data_input>"  
        Then the logisticregression prediction is "<prediction>"
        And I create a local logisticregression prediction for "<data_input>"
        Then the local logisticregression prediction is "<prediction>"
		
        Examples:
        | data  | time_1  | time_2 | time_3 | options | data_input | prediction |
        | data/spam.csv | 50      | 50     | 50 | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": true, "stem_words": true, "use_stopwords": false, "language": "en"}}}}  | {"Message": "Mobile call"}  |  ham |
        | data/spam.csv | 50      | 50     | 50 | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": true, "stem_words": true, "use_stopwords": false, "language": "en"}}}}  | {"Message": "A normal message"}  |  ham  |
        | data/spam.csv | 50      | 50     | 50 | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": false, "use_stopwords": false, "language": "en"}}}}  | {"Message": "Mobile calls"}  |  ham  |       
        | data/spam.csv | 50      | 50     | 50 | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": false, "use_stopwords": false, "language": "en"}}}}  | {"Message": "A normal message"}  |  ham  |
        | data/spam.csv | 50      | 50     | 50 | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": true, "use_stopwords": true, "language": "en"}}}}  | {"Message": "Mobile call"}  |  ham |
        | data/spam.csv | 50      | 50     | 50 | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": true, "use_stopwords": true, "language": "en"}}}}  | {"Message": "A normal message"}  |  ham  |
      	| data/spam.csv | 50      | 50     | 50 | {"fields": {"000001": {"optype": "text", "term_analysis": {"token_mode": "full_terms_only", "language": "en"}}}}  | {"Message": "FREE for 1st week! No1 Nokia tone 4 ur mob every week just txt NOKIA to 87077 Get txting and tell ur mates. zed POBox 36504 W45WQ norm150p/tone 16+"}  |  ham  |
        | data/spam.csv | 50      | 50     | 50 | {"fields": {"000001": {"optype": "text", "term_analysis": {"token_mode": "full_terms_only", "language": "en"}}}}  | {"Message": "Ok"}  |  ham |


    Scenario Outline: Successfully comparing predictions with text options
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I update the source with "<options>" waiting less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a logisticregression with objective "<objective>" and params ""
        And I wait until the logisticregression is ready less than <time_3> secs
        And I create a local logisticregression
        When I create a logisticregression prediction for "<data_input>"
      	Then the logisticregression prediction is "<prediction>"
      	And the logisticregression probability for the prediction is "<probability>"
        And I create a local logisticregression prediction for "<data_input>"
        Then the local logisticregression prediction is "<prediction>"
        
        Examples:
        | data  | time_1  | time_2 | time_3 | options | data_input | prediction | probability | objective |
        | data/spam.csv | 50      | 50     | 180  | {"fields": {"000001": {"optype": "text", "term_analysis": {"token_mode": "full_terms_only", "language": "en"}}}}  | {"Message": "A normal message"}  |  ham | 0.8795  | 000000  |
        | data/spam.csv | 50      | 50     | 180  | {"fields": {"000001": {"optype": "text", "term_analysis": {"token_mode": "all", "language": "en"}}}}  | {"Message": "mobile"}  |  ham | 0.91833 | 000000  |
        | data/movies.csv | 50      | 50     | 180  | {"fields": {"000007": {"optype": "items", "item_analysis": {"separator": "$"}}}}  | {"gender": "Female", "genres": "Adventure$Action", "timestamp": 993906291, "occupation": "K-12 student", "zipcode": 59583, "rating": 3}  |  Under 18  | 0.83441  | 000002  |
    
    Scenario Outline: Successfully comparing predictions with text options
        Given I provision a dataset from "<data>" file
        And I create a logisticregression with objective "<objective>" and params "<params>"
        And I wait until the logisticregression is ready less than <time_1> secs
        And I create a local logisticregression
        When I create a logisticregression prediction for "<data_input>"
      	Then the logisticregression prediction is "<prediction>"
      	And the logisticregression probability for the prediction is "<probability>"
        And I create a local logisticregression prediction for "<data_input>"
        Then the local logisticregression prediction is "<prediction>"       
        And the local logisticregression probability for the prediction is "<probability>"
		
        Examples:
        | data  | time_1  | params | data_input | prediction | probability | objective |
        | data/iris.csv | 180  | {"weight_field": "000000", "missing_numerics": false}  | {"petal width": 1.5, "petal length": 2, "sepal width":1}  |  Iris-versicolor | 0.9547  | 000004  |     


     Scenario Outline: Successfully comparing predictions with text options
        Given I create a data source uploading a "<data>" file
      	And I wait until the source is ready less than <time_1> secs
      	And I update the source with "<options>" waiting less than <time_1> secs
      	And I create a dataset
      	And I wait until the dataset is ready less than <time_2> secs
      	And I create a logisticregression with objective "<objective>" and params "<params>"
        And I wait until the logisticregression is ready less than <time_3> secs
        And I create a local logisticregression
        When I create a logisticregression prediction for "<data_input>"
    		Then the logisticregression prediction is "<prediction>"
    		And the logisticregression probability for the prediction is "<probability>"
        And I create a local logisticregression prediction for "<data_input>"
        Then the local logisticregression prediction is "<prediction>"       
        And the local logisticregression probability for the prediction is "<probability>"
		
        Examples:
        | data  | time_1  | time_2 | time_3 | options | data_input | prediction | probability | objective | params  |
        | data/iris.csv | 50      | 50     | 180  | {"fields": {"000000": {"optype": "categorical"}}}  | {"species": "Iris-setosa"}  |  5.0 | 0.0394  | 000000  | {"field_codings": [{"field": "species", "coding": "dummy", "dummy_class": "Iris-setosa"}]}  |
        | data/iris.csv | 50      | 50     | 180  | {"fields": {"000000": {"optype": "categorical"}}}  | {"species": "Iris-setosa"}  |  5.0 | 0.051 | 000000  | {"balance_fields": false, "field_codings": [{"field": "species", "coding": "contrast", "coefficients": [[1, 2, -1, -2]]}]}  |
        | data/iris.csv | 50      | 50     | 180  | {"fields": {"000000": {"optype": "categorical"}}}  | {"species": "Iris-setosa"}  |  5.0 | 0.051 | 000000  | {"balance_fields": false, "field_codings": [{"field": "species", "coding": "other", "coefficients": [[1, 2, -1, -2]]}]} |
    		| data/iris.csv | 50      | 50     | 180  | {"fields": {"000000": {"optype": "categorical"}}}  | {"species": "Iris-setosa"}  |  5.0 | 0.0417  | 000000  | {"bias": false} |


    Scenario Outline: Successfully comparing predictions for logistic regression with balance_fields
        Given I create a data source uploading a "<data>" file
      	And I wait until the source is ready less than <time_1> secs
      	And I update the source with "<options>" waiting less than <time_1> secs
      	And I create a dataset
      	And I wait until the dataset is ready less than <time_2> secs
      	And I create a logisticregression with objective "<objective>" and params "<params>"
        And I wait until the logisticregression is ready less than <time_3> secs
        And I create a local logisticregression
        When I create a logisticregression prediction for "<data_input>"
   			Then the logisticregression prediction is "<prediction>"
    		And the logisticregression probability for the prediction is "<probability>"
        And I create a local logisticregression prediction for "<data_input>"
        Then the local logisticregression prediction is "<prediction>"       
        And the local logisticregression probability for the prediction is "<probability>"
		
        Examples:
        | data  | time_1  | time_2 | time_3 | options | data_input | prediction | probability | objective | params  |
        | data/movies.csv | 50      | 50     | 180  | {"fields": {"000000": {"name": "user_id", "optype": "numeric"}, "000001": {"name": "gender", "optype": "categorical"}, "000002": {"name": "age_range", "optype": "categorical"}, "000003": {"name": "occupation", "optype": "categorical"}, "000004": {"name": "zipcode", "optype": "numeric"}, "000005": {"name": "movie_id", "optype": "numeric"}, "000006": {"name": "title", "optype": "text"}, "000007": {"name": "genres", "optype": "items", "item_analysis": {"separator": "$"}}, "000008": {"name": "timestamp", "optype": "numeric"}, "000009": {"name": "rating", "optype": "categorical"}}, "source_parser": {"separator": ";"}}  | {"timestamp": 999999999}  |  4  | 0.4079  | 000009  | {"balance_fields": false} |
        | data/movies.csv | 50      | 50     | 180  | {"fields": {"000000": {"name": "user_id", "optype": "numeric"}, "000001": {"name": "gender", "optype": "categorical"}, "000002": {"name": "age_range", "optype": "categorical"}, "000003": {"name": "occupation", "optype": "categorical"}, "000004": {"name": "zipcode", "optype": "numeric"}, "000005": {"name": "movie_id", "optype": "numeric"}, "000006": {"name": "title", "optype": "text"}, "000007": {"name": "genres", "optype": "items", "item_analysis": {"separator": "$"}}, "000008": {"name": "timestamp", "optype": "numeric"}, "000009": {"name": "rating", "optype": "categorical"}}, "source_parser": {"separator": ";"}}  | {"timestamp": 999999999}  |  4  | 0.2547  | 000009  | {"normalize": true} |
        | data/movies.csv | 50      | 50     | 180  | {"fields": {"000000": {"name": "user_id", "optype": "numeric"}, "000001": {"name": "gender", "optype": "categorical"}, "000002": {"name": "age_range", "optype": "categorical"}, "000003": {"name": "occupation", "optype": "categorical"}, "000004": {"name": "zipcode", "optype": "numeric"}, "000005": {"name": "movie_id", "optype": "numeric"}, "000006": {"name": "title", "optype": "text"}, "000007": {"name": "genres", "optype": "items", "item_analysis": {"separator": "$"}}, "000008": {"name": "timestamp", "optype": "numeric"}, "000009": {"name": "rating", "optype": "categorical"}}, "source_parser": {"separator": ";"}}  | {"timestamp": 999999999}  |  4  | 0.2547  | 000009  | {"balance_fields": true, "normalize": true} |


     Scenario Outline: Successfully comparing logistic regression predictions with constant fields
        Given I create a data source uploading a "<data>" file
	    	And I wait until the source is ready less than <time_1> secs
				And I create a dataset
	    	And I wait until the dataset is ready less than <time_2> secs
	    	And I update the dataset with "<options>" waiting less than <time_2> secs
	    	And I create a logisticregression from a dataset
        And I wait until the logisticregression is ready less than <time_3> secs
        And I create a local logisticregression
        When I create a logisticregression prediction for "<data_input>"
				Then the logisticregression prediction is "<prediction>"
				And I create a local logisticregression prediction for "<data_input>"
        Then the local logisticregression prediction is "<prediction>"
		
        Examples:
        | data	| time_1  | time_2 | time_3 | data_input	| prediction | options	|
        | data/constant_field.csv | 50      | 50     | 50	| {"a": 1, "b": 1, "c": 1}	| a	| {"fields": {"000000": {"preferred": true}}}	|
		
		
		Scenario Outline: Successfully comparing predictions for logistic regressions with operating kind
        Given I provision a dataset from "<data>" file
        And I create a logisticregression from a dataset
        And I wait until the logisticregression is ready less than <time_1> secs
        And I create a local logisticregression
        When I create a prediction with logisticregression with operating kind "<operating_kind>" for "<data_input>"
        Then the prediction for "<objective>" is "<prediction>"
        When I create a local prediction with logisticregression with operating kind "<operating_kind>" for "<data_input>"
        Then the local logisticregression prediction is "<prediction>"

        Examples:
        | data	| time_1  | data_input | objective	| prediction	| operating_kind	|
        | data/iris.csv | 30000	| {"petal length": 5} | 000004	| Iris-versicolor	| probability	|
        | data/iris.csv | 30000	| {"petal length": 2} | 000004	| Iris-setosa	| probability	|
  