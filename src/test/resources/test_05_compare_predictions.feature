Feature: Create Predictions
  In order to compare a remote prediction with a local prediction
  I need to create a model first
  Then I need to create a local model


  Scenario Outline: Successfully comparing predictions:
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I add the unitTest tag to the data source waiting less than <time_1> secs
    And I create a dataset
    And I wait until the dataset is ready less than <time_2> secs
    And I create a model
    And I wait until the model is ready less than <time_3> secs
    And I create a local model
    When I create a prediction for "<data_input>"
    Then the prediction for "<objective>" is "<prediction>"
    Then the local prediction for "<data_input>" is "<prediction>"
    Then delete test data

    Examples:
      | data                |  time_1  | time_2 | time_3 |  data_input    | objective | prediction  |
      | data/iris.csv |   50      | 50     | 50     | {"petal width": 0.5}                   | 000004    | Iris-setosa |
      | data/iris.csv |   50      | 50     | 50     | {"petal length": 6, "petal width": 2}  | 000004    | Iris-virginica |
      | data/iris.csv |   50      | 50     | 50     | {"petal length": 4, "petal width": 1.5}| 000004    | Iris-versicolor |
      | data/iris_sp_chars.csv |  50      | 50     | 50     | {"pétal.length": 4, "pétal&width\u0000": 1.5}| 000004    | Iris-versicolor |


  Scenario Outline: Successfully comparing predictions with text options:
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I update the source with "<options>" waiting less than <time_1> secs
    And I add the unitTest tag to the data source waiting less than <time_1> secs
    And I create a dataset
    And I wait until the dataset is ready less than <time_2> secs
    And I create a model
    And I wait until the model is ready less than <time_3> secs
    And I create a local model
    When I create a prediction for "<data_input>"
    Then the prediction for "<objective>" is "<prediction>"
    And the local prediction for "<data_input>" is "<prediction>"
    Then delete test data

    Examples:
      | data             |   time_1  | time_2 | time_3 |    options | data_input                             | objective | prediction  |
      | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": true, "stem_words": true, "use_stopwords": false, "language": "en"}}}} |{"Message": "Mobile call"}             | 000000    | ham    |
      | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": true, "stem_words": true, "use_stopwords": false, "language": "en"}}}} |{"Message": "A normal message"}        | 000000    | ham     |
      | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": false, "use_stopwords": false, "language": "en"}}}} |{"Message": "Mobile calls"}          | 000000    | spam   |
      | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": false, "use_stopwords": false, "language": "en"}}}} |{"Message": "A normal message"}       | 000000    | ham     |
      | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": true, "use_stopwords": true, "language": "en"}}}}|{"Message": "Mobile call"}            | 000000    | spam    |
      | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": true, "use_stopwords": true, "language": "en"}}}} |{"Message": "A normal message"}       | 000000    | ham     |
      | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"token_mode": "full_terms_only", "language": "en"}}}} |{"Message": "FREE for 1st week! No1 Nokia tone 4 ur mob every week just txt NOKIA to 87077 Get txting and tell ur mates. zed POBox 36504 W45WQ norm150p/tone 16+"}       | 000000    | spam     |
      | data/spam.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"token_mode": "full_terms_only", "language": "en"}}}} |{"Message": "Ok"}       | 000000    | ham     |
      #| data/text_missing.csv |    20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"token_mode": "all", "language": "en"}}, "000000": {"optype": "text", "term_analysis": {"token_mode": "all", "language": "en"}}}} |{}       | 000003    | swap     |


  Scenario Outline: Successfully comparing predictions with proportional missing strategy:
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I add the unitTest tag to the data source waiting less than <time_1> secs
    And I create a dataset
    And I wait until the dataset is ready less than <time_2> secs
    And I create a model
    And I wait until the model is ready less than <time_3> secs
    And I create a local model
    When I create a proportional missing strategy prediction for "<data_input>"
    Then the prediction for "<objective>" is "<prediction>"
    And the confidence for the prediction is <confidence>
    And I create a local model
    Then the proportional missing strategy local prediction for "<data_input>" is "<prediction>"
    Then the confidence of the proportional missing strategy local prediction for "<data_input>" is <confidence>
    Then delete test data

    Examples:
      | data              | time_1  | time_2  | time_3  | data_input  | objective | prediction      | confidence        |
      | data/iris.csv     |   50    |   50    |   50    |   {}  |   000004  |   Iris-setosa   | 0.2629           |


  Scenario Outline: Successfully comparing predictions with proportional missing strategy:
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I add the unitTest tag to the data source waiting less than <time_1> secs
    And I create a dataset
    And I wait until the dataset is ready less than <time_2> secs
    And I create a model
    And I wait until the model is ready less than <time_3> secs
    And I create a local model
    When I create a proportional missing strategy prediction for "<data_input>"
    Then the numerical prediction for "<objective>" is <prediction>
    And the confidence for the prediction is <confidence>
    Then the numerical prediction of proportional missing strategy local prediction for "<data_input>" is <prediction>
    Then the confidence of the proportional missing strategy local prediction for "<data_input>" is <confidence>
    Then delete test data

    Examples:
      | data               | time_1  | time_2 | time_3 | data_input           | objective | prediction     | confidence |
      | data/grades.csv |   50      | 50     | 50     | {}                   | 000005    | 68.62224       | 27.5358    |
      | data/grades.csv |   50      | 50     | 50     | {"Midterm": 20}      | 000005    | 40.46667      | 54.89713   |
      | data/grades.csv |   50      | 50     | 50     | {"Midterm": 20, "Tutorial": 90, "TakeHome": 500}     | 000005    | 28.06      | 25.65806   |


  Scenario Outline: Successfully comparing predictions with proportional missing strategy for missing_splits models:
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I add the unitTest tag to the data source waiting less than <time_1> secs
    And I create a dataset
    And I wait until the dataset is ready less than <time_2> secs
    And I create a model with missing splits
    And I wait until the model is ready less than <time_3> secs
    And I create a local model
    When I create a proportional missing strategy prediction for "<data_input>"
    Then the prediction for "<objective>" is "<prediction>"
    And the confidence for the prediction is <confidence>
    And the proportional missing strategy local prediction for "<data_input>" is "<prediction>"
    And the confidence of the proportional missing strategy local prediction for "<data_input>" is <confidence>
    Then delete test data

    Examples:
      | data               | time_1  | time_2 | time_3 | data_input  | objective | prediction     | confidence |
      | data/iris_missing2.csv   |     50      | 50     | 50     | {"petal width": 1}             | 000004    | Iris-setosa    | 0.8064     |
      | data/iris_missing2.csv   |     50      | 50     | 50     | {"petal width": 1, "petal length": 4}             | 000004    | Iris-versicolor    | 0.7847     |



