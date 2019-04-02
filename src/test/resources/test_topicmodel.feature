Feature: Topic Model
	
	Scenario Outline: Successfully creating a topic model:
      Given I create a data source uploading a "<data>" file
      And I wait until the source is ready less than <time_1> secs
      And I update the source with "<options>" waiting less than <time_1> secs
      And I create a dataset
      And I wait until the dataset is ready less than <time_2> secs
      When I create topic model from a dataset
      Then I wait until the topic model is ready less than <time_3> secs

      Examples:
      | data                    | time_1  | time_2 | time_3   | options  |
      | data/movies.csv       | 10      | 10     | 100      | {"fields": {"000007": {"optype": "items", "item_analysis": {"separator": "$"}}, "000006": {"optype": "text"}}} |
	
	
    Scenario Outline: Successfully creating Topic Model from a dataset:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I update the source with "<params>" waiting less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create topic model from a dataset
        And I wait until the topic model is ready less than <time_3> secs
        And I update the topic model name to "<topic_model_name>"
        When I wait until the topic model is ready less than <time_4> secs
        Then the topic model name is "<topic_model_name>"

        Examples:
        | data             | time_1  | time_2 | time_3 | time_4 | topic_model_name | params |
        | data/spam.csv | 100      | 100     | 200     | 500 | my new topic model name | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": true, "stem_words": true, "use_stopwords": false, "language": "en"}}}}   |


    Scenario Outline: Successfully comparing topic distributions:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I update the source with "<options>" waiting less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create topic model from a dataset
        And I wait until the topic model is ready less than <time_3> secs
        
        And I create a local topic model
        When I create a local topic distribution for "<data_input>"
        Then the local topic distribution is "<topic_distribution>"
        
        When I create a topic distribution for "<data_input>"
        Then the topic distribution is "<topic_distribution>"

      Examples:
        | data  | time_1  | time_2 | time_3 | options    |  data_input  | topic_distribution    |
        | data/spam.csv    | 20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": true, "stem_words": true, "use_stopwords": false, "language": "en"}}}}    |   {"Type": "ham", "Message": "Mobile call"}   | [0.51133, 0.00388, 0.00574, 0.00388, 0.00388, 0.00388, 0.00388, 0.00388, 0.00388, 0.00388, 0.00388, 0.44801]  |   
        | data/spam.csv    | 20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": true, "stem_words": true, "use_stopwords": false, "language": "en"}}}}         | {"Type": "ham", "Message": "Go until jurong point, crazy.. Available only in bugis n great world la e buffet... Cine there got amore wat..."}    | [0.39188, 0.00643, 0.00264, 0.00643, 0.08112, 0.00264, 0.37352, 0.0115, 0.00707, 0.00327, 0.00264, 0.11086]   |
        
        