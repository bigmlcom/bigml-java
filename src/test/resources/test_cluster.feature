Feature: Cluster REST api
	
	Scenario Outline: Successfully creating datasets for first centroid of a cluster:
			Given I create a data source uploading a "<data>" file
      And I wait until the source is ready less than <time_1> secs
      And I create a dataset
      And I wait until the dataset is ready less than <time_2> secs
      And I create a cluster
      And I wait until the cluster is ready less than <time_3> secs
			When I create a dataset associated to centroid "<centroid_id>"
      And I wait until the dataset is ready less than <time_4> secs
      Then the dataset is associated to the centroid "<centroid_id>" of the cluster
		
			Examples:
			| data	|  time_1  | time_2 | time_3 | time_4 | centroid_id |
			| data/iris.csv | 30      | 30     | 40     | 30 | 000001	|
		
	
	Scenario Outline: Successfully creating models for first centroid of a cluster:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
				And I create a cluster with options "<options>"
				And I wait until the cluster is ready less than <time_3> secs
				When I create a model associated to centroid "<centroid_id>"
        And I wait until the model is ready less than <time_4> secs
				Then the model is associated to the centroid "<centroid_id>" of the cluster
		
				Examples:
        | data	|  time_1  | time_2 | time_3 | time_4 | options	| centroid_id |
        | data/iris.csv | 30      | 30     | 40     | 30 | {"model_clusters": true}	| 000001	|	
	