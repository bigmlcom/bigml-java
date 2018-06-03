Feature: Inspect the information of a local model
  In order to inspect the information
  I need to create a model and a dataset first

  Scenario Outline: Successfully creating a model and translate the tree model into a set of IF-THEN rules:
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I add the unitTest tag to the data source waiting less than <time_1> secs
    And I create a dataset
    And I wait until the dataset is ready less than <time_2> secs
    And I create a model
    And I wait until the model is ready less than <time_3> secs
    And I create a local model for inspection
    And I translate the tree into IF_THEN rules
    Then I check the output is like "<expected_file>" expected file

    Examples:
    | data                   | time_1   | time_2 | time_3 | expected_file                                  |
    | data/iris.csv          |  10      | 10     | 10     | data/model/if_then_rules_iris.txt              |
    | data/iris_sp_chars.csv |  10      | 10     | 10     | data/model/if_then_rules_iris_sp_chars.txt     |
    | data/spam.csv          |  20      | 20     | 30     | data/model/if_then_rules_spam.txt              |
    | data/grades.csv        |  10      | 10     | 10     | data/model/if_then_rules_grades.txt            |
    | data/diabetes.csv      |  20      | 20     | 30     | data/model/if_then_rules_diabetes.txt          |
    | data/iris_missing2.csv |  10      | 10     | 10     | data/model/if_then_rules_iris_missing2.txt     |
    | data/tiny_kdd.csv      |  20      | 20     | 30     | data/model/if_then_rules_tiny_kdd.txt          |

  Scenario Outline: Successfully creating a model and translate the tree model into a set of IF-THEN rules:
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I add the unitTest tag to the data source waiting less than <time_1> secs
    And I update the source with "<options>" waiting less than <time_1> secs
    And I create a dataset
    And I wait until the dataset is ready less than <time_2> secs
    And I create a model
    And I wait until the model is ready less than <time_3> secs
    And I create a local model for inspection
    And I translate the tree into IF_THEN rules
    Then I check the output is like "<expected_file>" expected file


    Examples:
    | data                   | time_1   | time_2 | time_3 | options | expected_file                                  |
    | data/spam.csv          |  20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": true, "stem_words": true, "use_stopwords": false, "language": "en"}}}} | data/model/if_then_rules_spam_textanalysis_1.txt              |
    | data/spam.csv          |  20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": true, "stem_words": true, "use_stopwords": false}}}} | data/model/if_then_rules_spam_textanalysis_2.txt              |
    | data/spam.csv          |  20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": false, "use_stopwords": false, "language": "en"}}}} | data/model/if_then_rules_spam_textanalysis_3.txt              |
    | data/spam.csv          |  20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": true, "use_stopwords": true, "language": "en"}}}} | data/model/if_then_rules_spam_textanalysis_4.txt              |
    | data/spam.csv          |  20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"token_mode": "full_terms_only", "language": "en"}}}} | data/model/if_then_rules_spam_textanalysis_5.txt              |
    | data/spam.csv          |  20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": true, "stem_words": true, "use_stopwords": false, "language": "en"}}}} | data/model/if_then_rules_spam_textanalysis_6.txt              |

  Scenario Outline: Successfully creating a model with missing values and translate the tree model into a set of IF-THEN rules:
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I add the unitTest tag to the data source waiting less than <time_1> secs
    And I create a dataset
    And I wait until the dataset is ready less than <time_2> secs
    And I create a model with missing splits
    And I wait until the model is ready less than <time_3> secs
    And I create a local model for inspection
    And I translate the tree into IF_THEN rules
    Then I check the output is like "<expected_file>" expected file

    Examples:
    | data                   | time_1   | time_2 | time_3 | expected_file                                       |
    | data/iris_missing2.csv |  10      | 10     | 10     | data/model/if_then_rules_iris_missing2_MISSINGS.txt |


  Scenario Outline: Successfully creating a model and translate the tree model into a set of Java rules:
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I add the unitTest tag to the data source waiting less than <time_1> secs
    And I create a dataset
    And I wait until the dataset is ready less than <time_2> secs
    And I create a model
    And I wait until the model is ready less than <time_3> secs
    And I create a local model for inspection
    And I translate the tree into Java rules
    Then I check the output is like "<expected_file>" expected file

    Examples:
    | data                   | time_1   | time_2 | time_3 | expected_file                                  |
    | data/iris.csv          |  10      | 10     | 10     | data/model/java_rules_iris.txt              |
    | data/iris_sp_chars.csv |  10      | 10     | 10     | data/model/java_rules_iris_sp_chars.txt     |
    | data/spam.csv          |  20      | 20     | 30     | data/model/java_rules_spam.txt              |
    | data/grades.csv        |  10      | 10     | 10     | data/model/java_rules_grades.txt            |
    | data/diabetes.csv      |  20      | 20     | 30     | data/model/java_rules_diabetes.txt          |
    | data/iris_missing2.csv |  10      | 10     | 10     | data/model/java_rules_iris_missing2.txt     |
    | data/tiny_kdd.csv      |  20      | 20     | 30     | data/model/java_rules_tiny_kdd.txt          |


  Scenario Outline: Successfully creating a model and translate the tree model into a set of Java rules:
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I add the unitTest tag to the data source waiting less than <time_1> secs
    And I update the source with "<options>" waiting less than <time_1> secs
    And I create a dataset
    And I wait until the dataset is ready less than <time_2> secs
    And I create a model
    And I wait until the model is ready less than <time_3> secs
    And I create a local model for inspection
    And I translate the tree into Java rules
    Then I check the output is like "<expected_file>" expected file

    Examples:
    | data                   | time_1   | time_2 | time_3 | options | expected_file                                  |
    | data/spam.csv          |  20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": true, "stem_words": true, "use_stopwords": false, "language": "en"}}}} | data/model/java_rules_spam_textanalysis_1.txt              |
    | data/spam.csv          |  20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": true, "stem_words": true, "use_stopwords": false}}}} | data/model/java_rules_spam_textanalysis_2.txt              |
    | data/spam.csv          |  20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": false, "use_stopwords": false, "language": "en"}}}} | data/model/java_rules_spam_textanalysis_3.txt              |
    | data/spam.csv          |  20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": false, "stem_words": true, "use_stopwords": true, "language": "en"}}}} | data/model/java_rules_spam_textanalysis_4.txt              |
    | data/spam.csv          |  20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"token_mode": "full_terms_only", "language": "en"}}}} | data/model/java_rules_spam_textanalysis_5.txt              |
    | data/spam.csv          |  20      | 20     | 30     | {"fields": {"000001": {"optype": "text", "term_analysis": {"case_sensitive": true, "stem_words": true, "use_stopwords": false, "language": "en"}}}} | data/model/java_rules_spam_textanalysis_6.txt              |


  Scenario Outline: Successfully creating a model with missing values and translate the tree model into a set of Java rules:
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I add the unitTest tag to the data source waiting less than <time_1> secs
    And I create a dataset
    And I wait until the dataset is ready less than <time_2> secs
    And I create a model with missing splits
    And I wait until the model is ready less than <time_3> secs
    And I create a local model for inspection
    And I translate the tree into Java rules
    Then I check the output is like "<expected_file>" expected file

    Examples:
    | data                   | time_1   | time_2 | time_3 | expected_file                                       |
    | data/iris_missing2.csv |  10      | 10     | 10     | data/model/java_rules_iris_missing2_MISSINGS.txt |
 
 
  Scenario Outline: Successfully creating a model and check its data distribution:
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I add the unitTest tag to the data source waiting less than <time_1> secs
    And I create a dataset
    And I wait until the dataset is ready less than <time_2> secs
    And I create a model
    And I wait until the model is ready less than <time_3> secs
    And I create a local model for inspection
    Then I check the data distribution with "<expected_file>" file

    Examples:
      | data                   | time_1   | time_2 | time_3 | expected_file                                  |
      | data/iris.csv          |  10      | 10     | 10     | data/model/data_distribution_iris.txt              |
      | data/iris_sp_chars.csv |  10      | 10     | 10     | data/model/data_distribution_iris_sp_chars.txt     |
      | data/spam.csv          |  20      | 20     | 30     | data/model/data_distribution_spam.txt              |
      | data/grades.csv        |  10      | 10     | 10     | data/model/data_distribution_grades.txt            |
      | data/diabetes.csv      |  20      | 20     | 30     | data/model/data_distribution_diabetes.txt          |
      | data/iris_missing2.csv |  10      | 10     | 10     | data/model/data_distribution_iris_missing2.txt     |
      | data/tiny_kdd.csv      |  20      | 20     | 30     | data/model/data_distribution_tiny_kdd.txt          |


  Scenario Outline: Successfully creating a model and check its data distribution:
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I add the unitTest tag to the data source waiting less than <time_1> secs
    And I create a dataset
    And I wait until the dataset is ready less than <time_2> secs
    And I create a model
    And I wait until the model is ready less than <time_3> secs
    And I create a local model for inspection
    Then I check the model summary with "<expected_file>" file

    Examples:
      | data                   | time_1   | time_2 | time_3 | expected_file                                  |
      | data/iris.csv          |  10      | 10     | 10     | data/model/summarize_iris.txt              |
      | data/iris_sp_chars.csv |  10      | 10     | 10     | data/model/summarize_iris_sp_chars.txt     |
      | data/spam.csv          |  20      | 20     | 30     | data/model/summarize_spam.txt              |
      | data/grades.csv        |  10      | 10     | 10     | data/model/summarize_grades.txt            |
      | data/diabetes.csv      |  20      | 20     | 30     | data/model/summarize_diabetes.txt          |
      | data/iris_missing2.csv |  10      | 10     | 10     | data/model/summarize_iris_missing2.txt     |
      | data/tiny_kdd.csv      |  20      | 20     | 30     | data/model/summarize_tiny_kdd.txt          |
