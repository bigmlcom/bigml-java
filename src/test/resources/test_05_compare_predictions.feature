Feature: Create Predictions
  In order to compare a remote prediction with a local prediction
  I need to create a model first
  Then I need to create a local model


  Scenario Outline: Successfully creating a prediction:
    Given that I use production mode with seed="<seed>"
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I add the unitTest tag to the data source waiting less than <time_1> secs
    And I create a dataset
    And I wait until the dataset is ready less than <time_2> secs
    And I create a model
    And I wait until the model is ready less than <time_3> secs
    When I create a prediction by name=<by_name> for "<data_input>"
    Then the prediction for "<objective>" is "<prediction>"
    Then delete test data

    Examples:
      | data                | seed  |  time_1  | time_2 | time_3 | by_name |  data_input    | objective | prediction  |
      | data/iris.csv | BigML |   10      | 10     | 10     | true     | {"petal width": 0.5}                   | 000004    | Iris-setosa |
      | data/iris.csv | BigML |   10      | 10     | 10     | true     | {"petal length": 6, "petal width": 2}  | 000004    | Iris-virginica |
      | data/iris.csv | BigML |   10      | 10     | 10     | true     | {"petal length": 4, "petal width": 1.5}| 000004    | Iris-versicolor |
      | data/iris_sp_chars.csv | BigML |  10      | 10     | 10     | true     | {"pétal.length": 4, "pétal&width\u0000": 1.5}| 000004    | Iris-versicolor |


  Scenario Outline: Successfully comparing predictions with text options:
    Given that I use production mode with seed="<seed>"
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I update the source with "<options>" waiting less than <time_1> secs
    And I add the unitTest tag to the data source waiting less than <time_1> secs
    And I create a dataset
    And I wait until the dataset is ready less than <time_2> secs
    And I create a model
    And I wait until the model is ready less than <time_3> secs
    And I create a local model
    When I create a prediction by name=<by_name> for "<data_input>"
    Then the prediction for "<objective>" is "<prediction>"
    And I create a local model
    And the local prediction for "<data_input>" is "<prediction>"
    Then delete test data

    Examples:
      | data             | seed  |   time_1  | time_2 | time_3 | by_name |    options | data_input                             | objective | prediction  |
      | data/spam.csv | BigML |    20      | 20     | 30     | true     |   {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": true, "stem_words": true, "use_stopwords": false, "language": "en"}}}} |{"Message": "Mobile call"}             | 000000    | ham    |
      | data/spam.csv | BigML |    20      | 20     | 30     | true     |   {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": true, "stem_words": true, "use_stopwords": false, "language": "en"}}}} |{"Message": "A normal message"}        | 000000    | ham     |
      | data/spam.csv | BigML |    20      | 20     | 30     | true     |   {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": false, "use_stopwords": false, "language": "en"}}}} |{"Message": "Mobile calls"}          | 000000    | spam   |
      | data/spam.csv | BigML |    20      | 20     | 30     | true     |   {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": false, "use_stopwords": false, "language": "en"}}}} |{"Message": "A normal message"}       | 000000    | ham     |
      | data/spam.csv | BigML |    20      | 20     | 30     | true     |   {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": true, "use_stopwords": true, "language": "en"}}}} |{"Message": "Mobile call"}            | 000000    | spam    |
      | data/spam.csv | BigML |    20      | 20     | 30     | true     |   {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": true, "use_stopwords": true, "language": "en"}}}} |{"Message": "A normal message"}       | 000000    | ham     |
      | data/spam.csv | BigML |    20      | 20     | 30     | true     |   {"fields": {"000001": {"optype": "text", "term_analysis": {"token_mode": "full_terms_only", "language": "en"}}}} |{"Message": "FREE for 1st week! No1 Nokia tone 4 ur mob every week just txt NOKIA to 87077 Get txting and tell ur mates. zed POBox 36504 W45WQ norm150p/tone 16+"}       | 000000    | spam     |
      | data/spam.csv | BigML |    20      | 20     | 30     | true     |   {"fields": {"000001": {"optype": "text", "term_analysis": {"token_mode": "full_terms_only", "language": "en"}}}} |{"Message": "Ok"}       | 000000    | ham     |


  Scenario Outline: Successfully comparing predictions with proportional missing strategy:
    Given that I use production mode with seed="<seed>"
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I add the unitTest tag to the data source waiting less than <time_1> secs
    And I create a dataset
    And I wait until the dataset is ready less than <time_2> secs
    And I create a model
    And I wait until the model is ready less than <time_3> secs
    When I create a proportional missing strategy prediction by name=<by_name> for "<data_input>"
    Then the prediction for "<objective>" is "<prediction>"
    And the confidence for the prediction is <confidence>
    And I create a local model
    Then the proportional missing strategy local prediction for "<data_input>" is "<prediction>"
    Then the confidence of the proportional missing strategy local prediction for "<data_input>" is <confidence>
    Then delete test data

    Examples:
      | data              | seed  | time_1  | time_2  | time_3  | by_name | data_input                                        | objective | prediction      | confidence        |
      | data/iris.csv     | BigML |   10    |   10    |   10    | true    |   {}                                              |   000004  |   Iris-setosa   | 0.26289           |

  Scenario Outline: Successfully comparing predictions with proportional missing strategy:
    Given that I use production mode with seed="<seed>"
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I add the unitTest tag to the data source waiting less than <time_1> secs
    And I create a dataset
    And I wait until the dataset is ready less than <time_2> secs
    And I create a model
    And I wait until the model is ready less than <time_3> secs
    When I create a proportional missing strategy prediction by name=<by_name> for "<data_input>"
    Then the numerical prediction for "<objective>" is <prediction>
    And the confidence for the prediction is <confidence>
    And I create a local model
    Then the numerical prediction of proportional missing strategy local prediction for "<data_input>" is <prediction>
    Then the confidence of the proportional missing strategy local prediction for "<data_input>" is <confidence>
    Then delete test data

    Examples:
      | data               | seed  | time_1  | time_2 | time_3 | by_name | data_input           | objective | prediction     | confidence |
      | data/grades.csv | BigML |   10      | 10     | 10     | true     |   {}                   | 000005    | 68.62224       | 27.53581    |
      | data/grades.csv | BigML |   10      | 10     | 10     | true     |   {"Midterm": 20}      | 000005    | 46.69889      | 37.27594   |
      | data/grades.csv | BigML |   10      | 10     | 10     | true     |   {"Midterm": 20, "Tutorial": 90, "TakeHome": 100}     | 000005    | 28.06      | 24.86634   |


  Scenario Outline: Successfully comparing centroids with or without text options:
    Given that I use production mode with seed="<seed>"
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I add the unitTest tag to the data source waiting less than <time_1> secs
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
      | data             |  seed  |    time_1  | time_2 | time_3 | options | data_input                            | centroid  | distance |
      | data/spam.csv | BigML |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": true, "stem_words": true, "use_stopwords": false, "language": "en"}}}} |{"Type": "ham", "Message": "Mobile call"}             | Cluster 1   | 0.5   |
      | data/spam.csv | BigML |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": true, "stem_words": true, "use_stopwords": false}}}} |{"Type": "ham", "Message": "A normal message"}        | Cluster 1   | 0.5     |
      | data/spam.csv | BigML |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": false, "use_stopwords": false, "language": "en"}}}} |{"Type": "ham", "Message": "Mobile calls"}            | Cluster 0     | 0.5    |
      | data/spam.csv | BigML |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": false, "use_stopwords": false, "language": "en"}}}} |{"Type": "ham", "Message": "A normal message"}       | Cluster 0     | 0.5     |
      | data/spam.csv | BigML |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": true, "use_stopwords": true, "language": "en"}}}} |{"Type": "ham", "Message": "Mobile call"}               | Cluster 5      | 0.41161165235168157   |
      | data/spam.csv | BigML |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": true, "use_stopwords": true, "language": "en"}}}} |{"Type": "ham", "Message": "A normal message"}       | Cluster 1    | 0.35566243270259357   |
      | data/spam.csv | BigML |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"token_mode": "full_terms_only", "language": "en"}}}} |{"Type": "ham", "Message": "FREE for 1st week! No1 Nokia tone 4 ur mob every week just txt NOKIA to 87077 Get txting and tell ur mates. zed POBox 36504 W45WQ norm150p/tone 16+"}       | Cluster 0      | 0.5     |
      | data/spam.csv | BigML |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"token_mode": "full_terms_only", "language": "en"}}}} |{"Type": "ham", "Message": "Ok"}       | Cluster 0    | 0.478833312167     |
      | data/spam.csv | BigML |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": true, "stem_words": true, "use_stopwords": false, "language": "en"}}}} |{"Type": "", "Message": ""}             | Cluster 0   | 0.707106781187   |
      | data/diabetes.csv | BigML |    20      | 20     | 30     | {"fields": {}} |{"pregnancies": 0, "plasma glucose": 118, "blood pressure": 84, "triceps skin thickness": 47, "insulin": 230, "bmi": 45.8, "diabetes pedigree": 0.551, "age": 31, "diabetes": "true"}       | Cluster 3    | 0.5033378686559257     |
      | data/iris_sp_chars.csv | BigML |    20      | 20     | 30     | {"fields": {}} |{"pétal.length":1, "pétal&width\u0000": 2, "sépal.length":1, "sépal&width": 2, "spécies": "Iris-setosa"}       | Cluster 7    | 0.8752380218327035     |


  Scenario Outline: Successfully comparing centroids with summary fields:
    Given that I use production mode with seed="<seed>"
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I add the unitTest tag to the data source waiting less than <time_1> secs
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
      | data             | seed  | time_1  | time_2 | time_3 | options | data_input                            | centroid  | distance |
      | data/iris.csv | BigML |20      | 20     | 30     | {"summary_fields": ["sepal width"]} |{"petal length": 1, "petal width": 1, "sepal length": 1, "species": "Iris-setosa"}             | Cluster 2   | 1.1643644909783857   |


  Scenario Outline: Successfully comparing predictions with proportional missing strategy for missing_splits models:
    Given that I use production mode with seed="<seed>"
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I add the unitTest tag to the data source waiting less than <time_1> secs
    And I create a dataset
    And I wait until the dataset is ready less than <time_2> secs
    And I create a model with missing splits
    And I wait until the model is ready less than <time_3> secs
    When I create a proportional missing strategy prediction by name=<by_name> for "<data_input>"
    Then the prediction for "<objective>" is "<prediction>"
    And the confidence for the prediction is <confidence>
    And I create a local model
    And the proportional missing strategy local prediction for "<data_input>" is "<prediction>"
    And the confidence of the proportional missing strategy local prediction for "<data_input>" is <confidence>
    Then delete test data

    Examples:
      | data               |  seed  | time_1  | time_2 | time_3 | by_name | data_input           | objective | prediction     | confidence |
      | data/iris_missing2.csv   | BigML |     10      | 10     | 10     | true     |    {"petal width": 1}             | 000004    | Iris-setosa    | 0.8064     |
      | data/iris_missing2.csv   | BigML |     10      | 10     | 10     | true     |    {"petal width": 1, "petal length": 4}             | 000004    | Iris-versicolor    | 0.7847     |


  Scenario Outline: Successfully comparing scores from anomaly detectors:
    Given that I use production mode with seed="<seed>"
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I add the unitTest tag to the data source waiting less than <time_1> secs
    And I create a dataset
    And I wait until the dataset is ready less than <time_2> secs
    And I create an anomaly detector from a dataset
    And I wait until the anomaly detector is ready less than <time_3> secs
    And I create a local anomaly detector
    When I create an anomaly score for "<data_input>" by name=<by_name>
    Then the anomaly score is "<score>"
    And I create a local anomaly score for "<data_input>" by name=<by_name>
    Then the local anomaly score is <score>

  Examples:
    | data                 |  seed  | time_1  | time_2 | time_3 | by_name | data_input    | score    |
    | data/tiny_kdd.csv    | BigML  | 20      | 20     | 30     | false   | {"000020": 255.0, "000004": 183.0, "000016": 4.0, "000024": 0.04, "000025": 0.01, "000026": 0.0, "000019": 0.25, "000017": 4.0, "000018": 0.25, "00001e": 0.0, "000005": 8654.0, "000009": "0", "000023": 0.01, "00001f": 123.0}         | 0.69802  |