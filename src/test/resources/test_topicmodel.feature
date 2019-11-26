Feature: TopicModel REST api

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
        Then delete test data

        Examples:
        | data             | time_1  | time_2 | time_3 | time_4 | topic_model_name | params |
        | data/spam.csv | 100      | 100     | 200     | 500 | my new topic model name | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": true, "stem_words": true, "use_stopwords": false, "language": "en"}}}}   |
		
		
		Scenario Outline: Successfully creating a topic model:
	      Given I create a data source uploading a "<data>" file
	      And I wait until the source is ready less than <time_1> secs
	      And I update the source with "<options>" waiting less than <time_1> secs
	      And I create a dataset
	      And I wait until the dataset is ready less than <time_2> secs
	      When I create topic model from a dataset
	      Then I wait until the topic model is ready less than <time_3> secs
	      Then delete test data
	
	      Examples:
	      | data                    | time_1  | time_2 | time_3   | options  |
	      | data/movies.csv       | 10      | 10     | 100      | {"fields": {"000007": {"optype": "items", "item_analysis": {"separator": "$"}}, "000006": {"optype": "text"}}} |
			