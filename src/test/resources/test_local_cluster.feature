Feature: LocalCluster
		
		Scenario Outline: Successfully getting the closest point in a cluster
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a cluster
        And I wait until the cluster is ready less than <time_3> secs
        And I create a local cluster
        Then the data point in the cluster closest to "<reference>" is "<closest>"
				Then the data point in the cluster closest to "<reference>" is "<closest>"

        Examples:
        | data	|  time_1  | time_2 | time_3 | time_4 | reference |	closest	|
        | data/iris.csv | 10      | 10     | 40     | 20 | {"petal length": 1.4, "petal width": 0.2, "sepal width": 3.0, "sepal length": 4.89, "species": "Iris-setosa"} | {"distance": 0.001894153207990619, "data": {"petal length": 1.4, "petal width": 0.2, "sepal width": 3.0, "sepal length": 4.9, "species": "Iris-setosa"}}	|
        
		
		Scenario Outline: Successfully comparing centroids with or without text options:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I update the source with "<options>" waiting less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a cluster
        And I wait until the cluster is ready less than <time_3> secs
        And I create a local cluster
        When I create a centroid for "<data_input>"
        Then the centroid is "<centroid>" with distance <distance>
        And I create a local centroid for "<data_input>"
        Then the local centroid is "<centroid>" with distance <distance>

        Examples:
          | data             |    time_1  | time_2 | time_3 | options | data_input                            | centroid  | distance |
          | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": true, "stem_words": true, "use_stopwords": false, "language": "en"}}}} |{"Type": "ham", "Message": "Mobile call"}             | Cluster 0   | 0.25   |
          | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": true, "stem_words": true, "use_stopwords": false}}}} |{"Type": "ham", "Message": "A normal message"}        | Cluster 0   | 0.5     |
          | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": false, "use_stopwords": false, "language": "en"}}}} |{"Type": "ham", "Message": "Mobile calls"}            | Cluster 0     | 0.5    |
          | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": false, "use_stopwords": false, "language": "en"}}}} |{"Type": "ham", "Message": "A normal message"}       | Cluster 0     | 0.5     |
          | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": true, "use_stopwords": true, "language": "en"}}}} |{"Type": "ham", "Message": "Mobile call"}               | Cluster 1      | 0.34189   |
          | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": true, "use_stopwords": true, "language": "en"}}}} |{"Type": "ham", "Message": "A normal message"}       | Cluster 0    | 0.5   |
          | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"token_mode": "full_terms_only", "language": "en"}}}} |{"Type": "ham", "Message": "FREE for 1st week! No1 Nokia tone 4 ur mob every week just txt NOKIA to 87077 Get txting and tell ur mates. zed POBox 36504 W45WQ norm150p/tone 16+"}       | Cluster 0      | 0.5     |
          | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"token_mode": "full_terms_only", "language": "en"}}}} |{"Type": "ham", "Message": "Ok"}       | Cluster 0    | 0.478833312167     |
          | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": true, "stem_words": true, "use_stopwords": false, "language": "en"}}}} |{"Type": "", "Message": ""}             | Cluster 6   | 0.5   |
          | data/diabetes.csv |    20      | 20     | 30     | {"fields": {}} |{"pregnancies": 0, "plasma glucose": 118, "blood pressure": 84, "triceps skin thickness": 47, "insulin": 230, "bmi": 45.8, "diabetes pedigree": 0.551, "age": 31, "diabetes": "true"}       | Cluster 3    | 0.5033378686559257     |
          | data/diabetes.csv |    20      | 20     | 30     | {"fields": {}} |{"pregnancies": 0, "plasma glucose": 118, "blood pressure": 84, "triceps skin thickness": 47, "insulin": 230, "bmi": 45.8, "diabetes pedigree": 0.551, "age": 31, "diabetes": true}       | Cluster 3    | 0.5033378686559257     |
          | data/iris_sp_chars.csv |    20      | 20     | 30     | {"fields": {}} |{"pétal.length":1, "pétal&width\u0000": 2, "sépal.length":1, "sépal&width": 2, "spécies": "Iris-setosa"}       | Cluster 7    | 0.8752380218327035     |
          | data/movies.csv |    20      | 20     | 30     | {"fields": {"000007": {"optype": "items", "item_analysis": {"separator": "$"}}}} |{"gender": "Female", "age_range": "18-24", "genres": "Adventure$Action", "timestamp": 993906291, "occupation": "K-12 student", "zipcode": 59583, "rating": 3}       | Cluster 3    | 0.62852     |


    Scenario Outline: Successfully comparing centroids with configuration options:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a cluster with options "<options>"
        And I wait until the cluster is ready less than <time_3> secs
        And I create a local cluster
        When I create a centroid for "<data_input>"
        Then the centroid is "<centroid>" with distance <distance>
        And I create a local centroid for "<data_input>"
        Then the local centroid is "<centroid>" with distance <distance>

        Examples:
        | data	| time_1  | time_2 | time_3 | options | data_input | centroid  | distance |
        | data/iris.csv | 20      | 20     | 30     | {"summary_fields": ["sepal width"]} |{"petal length": 1, "petal width": 1, "sepal length": 1, "species": "Iris-setosa"}  | Cluster 2   | 1.16436   |
         # | data/iris.csv | 20      | 20     | 30     | {"default_numeric_value": "zero"} |{"petal length": 1}  | Cluster 4   | 1.41215   |
		