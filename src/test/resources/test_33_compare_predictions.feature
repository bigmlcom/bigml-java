Feature: Compare predictions

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
        Then delete test data


        Examples:
          | data             |    time_1  | time_2 | time_3 | options | data_input                            | centroid  | distance |
          | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": true, "stem_words": true, "use_stopwords": false, "language": "en"}}}} |{"Type": "ham", "Message": "Mobile call"}             | Cluster 7   | 0.36637   |
          | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": true, "stem_words": true, "use_stopwords": false}}}} |{"Type": "ham", "Message": "A normal message"}        | Cluster 0   | 0.5     |
          | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": false, "use_stopwords": false, "language": "en"}}}} |{"Type": "ham", "Message": "Mobile calls"}            | Cluster 0     | 0.5    |
          | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": false, "use_stopwords": false, "language": "en"}}}} |{"Type": "ham", "Message": "A normal message"}       | Cluster 0     | 0.5     |
          | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": true, "use_stopwords": true, "language": "en"}}}} |{"Type": "ham", "Message": "Mobile call"}               | Cluster 0      | 0.5   |
          | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": true, "use_stopwords": true, "language": "en"}}}} |{"Type": "ham", "Message": "A normal message"}       | Cluster 1    | 0.36637   |
          | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"token_mode": "full_terms_only", "language": "en"}}}} |{"Type": "ham", "Message": "FREE for 1st week! No1 Nokia tone 4 ur mob every week just txt NOKIA to 87077 Get txting and tell ur mates. zed POBox 36504 W45WQ norm150p/tone 16+"}       | Cluster 0      | 0.5     |
          | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"token_mode": "full_terms_only", "language": "en"}}}} |{"Type": "ham", "Message": "Ok"}       | Cluster 0    | 0.478833312167     |
          | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": true, "stem_words": true, "use_stopwords": false, "language": "en"}}}} |{"Type": "", "Message": ""}             | Cluster 1   | 0.5   |
          | data/diabetes.csv |    20      | 20     | 30     | {"fields": {}} |{"pregnancies": 0, "plasma glucose": 118, "blood pressure": 84, "triceps skin thickness": 47, "insulin": 230, "bmi": 45.8, "diabetes pedigree": 0.551, "age": 31, "diabetes": "true"}       | Cluster 3    | 0.5033378686559257     |
          | data/diabetes.csv |    20      | 20     | 30     | {"fields": {}} |{"pregnancies": 0, "plasma glucose": 118, "blood pressure": 84, "triceps skin thickness": 47, "insulin": 230, "bmi": 45.8, "diabetes pedigree": 0.551, "age": 31, "diabetes": true}       | Cluster 3    | 0.5033378686559257     |
          | data/iris_sp_chars.csv |    20      | 20     | 30     | {"fields": {}} |{"pétal.length":1, "pétal&width\u0000": 2, "sépal.length":1, "sépal&width": 2, "spécies": "Iris-setosa"}       | Cluster 7    | 0.8752380218327035     |
          | data/movies.csv |    20      | 20     | 30     | {"fields": {"000007": {"optype": "items", "item_analysis": {"separator": "$"}}}} |{"gender": "Female", "age_range": "18-24", "genres": "Adventure$Action", "timestamp": 993906291, "occupation": "K-12 student", "zipcode": 59583, "rating": 3}       | Cluster 1    | 0.7294650227133437     |


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


    Scenario Outline: Successfully comparing scores from anomaly detectors:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create an anomaly detector from a dataset
        And I wait until the anomaly detector is ready less than <time_3> secs
        And I create a local anomaly detector
        When I create an anomaly score for "<data_input>"
        Then the anomaly score is "<score>"
        And I create a local anomaly score for "<data_input>" by name=<by_name>
        Then the local anomaly score is <score>

      Examples:
        | data	| time_1  | time_2 | time_3 | by_name | data_input    | score    |
        | data/tiny_kdd.csv    | 20      | 20     | 30     | false   | {"000020": 255.0, "000004": 183.0, "000016": 4.0, "000024": 0.04, "000025": 0.01, "000026": 0.0, "000019": 0.25, "000017": 4.0, "000018": 0.25, "00001e": 0.0, "000005": 8654.0, "000009": "0", "000023": 0.01, "00001f": 123.0}         | 0.69802  |

        
     Scenario Outline: Successfully comparing topic distributions:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I add the unitTest tag to the data source waiting less than <time_1> secs
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
        | data/spam.csv    | 20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": true, "stem_words": true, "use_stopwords": false, "language": "en"}}}}    |   {"Type": "ham", "Message": "Mobile call"}   | [0.01878, 0.00388, 0.00388, 0.00388, 0.20313, 0.47315, 0.00574, 0.05695, 0.00388, 0.19382, 0.00388, 0.02902]  |   
        | data/spam.csv    | 20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": true, "stem_words": true, "use_stopwords": false, "language": "en"}}}}         | {"Type": "ham", "Message": "Go until jurong point, crazy.. Available only in bugis n great world la e buffet... Cine there got amore wat..."}    | [0.00263, 0.01083, 0.00831, 0.06004, 0.33701, 0.00263, 0.01209, 0.44553, 0.0531, 0.00326, 0.06193, 0.00263]   |
        