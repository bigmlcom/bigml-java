Feature: Clusters
	
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
		| data/iris.csv | 10      | 10     | 40     | 10 | 000001	|
		
	
	Scenario Outline: Successfully creating models for first centroid of a cluster:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I wait until the dataset is ready less than <time_2> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
		And I create a cluster with options "<options>"
		And I wait until the cluster is ready less than <time_3> secs
		When I create a model associated to centroid "<centroid_id>"
        And I wait until the model is ready less than <time_4> secs
        
		Then the model is associated to the centroid "<centroid_id>" of the cluster
		
		Examples:
        | data	|  time_1  | time_2 | time_3 | time_4 | options	| centroid_id |
        | data/iris.csv | 10      | 10     | 40     | 10 | {"model_clusters": true}	| 000001	|	
	
	
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
        
