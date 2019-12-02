Feature: PCA REST api calls
	
	Scenario Outline: Successfully creating a PCA from a dataset:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a pca from a dataset
        And I wait until the pca is ready less than <time_3> secs
        And I update the pca name to "<pca_name>"
        When I wait until the pca is ready less than <time_4> secs
        Then the pca name is "<pca_name>"
        Then I delete the pca

        Examples:
        | data                | time_1  | time_2 | time_3 | time_4 | pca_name |
        | data/iris.csv | 50      | 50     | 100     | 100 | my new pca name |
        
    
 	Scenario Outline: Successfully creating a projection:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a pca from a dataset
        And I wait until the pca is ready less than <time_3> secs
        When I create a projection for "<data_input>"
        Then the projection is "<projection>"
        
        Examples:
        | data                | time_1  | time_2 | time_3 | time_4 | data_input | projection |
        | data/iris.csv | 50      | 50     | 100     | 100 | {"petal width": 0.5} | {"PC2": 0.1593, "PC3": -0.01286, "PC1": 0.91648, "PC6": 0.27284, "PC4": 1.29255, "PC5": 0.75196} |
          
          
	Scenario Outline: Successfully creating a batch projection:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a pca from a dataset
        And I wait until the pca is ready less than <time_3> secs
        When I create a batch projection for the dataset with the pca
        And I wait until the batch projection is ready less than <time_4> secs
        And I download the created projections file to "<local_file>"
        Then the batch projection file is like "<projections_file>"

        Examples:
          | data             | time_1  | time_2 | time_3 | time_4 | local_file | projections_file |
          | data/iris.csv |   30      | 30     | 50     | 50     | data/batch_projections.csv | data/batch_projections.csv |
