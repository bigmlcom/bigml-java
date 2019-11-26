Feature: LocalPca
	
	Scenario Outline: Successfully comparing projections for PCAs   
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a pca with "<params>"
        And I wait until the pca is ready less than <time_3> secs
        And I create a local pca
        When I create a projection for "<data_input>"
        Then the projection is "<projection>"
        And I create a local projection for "<data_input>"
        Then the local projection is "<projection>"
        Then delete test data
                
        Examples:
        | data                    | time_1  | time_2 | time_3 | data_input  | projection         | params    |
        | data/iris.csv         | 50      | 50     | 120  | {}  | {"PC2": 0, "PC3": 0, "PC1": 0, "PC6": 0, "PC4": 5e-05, "PC5": 0} | {}    |
        | data/iris.csv         | 50      | 50     | 120  | {"petal length": 1} | {"PC2": 0.08708, "PC3": 0.20929, "PC1": 1.56084, "PC6": -1.34463, "PC4": 0.7295, "PC5": -1.00876} | {}    |
        | data/iris.csv         | 50      | 50     | 120  | {"species": "Iris-versicolor"}  | {"PC2": 1.8602, "PC3": -2.00864, "PC1": -0.61116, "PC6": -0.66983, "PC4": -2.44618, "PC5": 0.43414} | {} |
        | data/iris.csv         | 50      | 50     | 120  | {"petal length": 1, "sepal length": 0, "petal width": 0, "sepal width": 0, "species": "Iris-versicolor"}    | {"PC2": 7.18009, "PC3": 6.51511, "PC1": 2.78155, "PC6": 0.21372, "PC4": -1.94865, "PC5": 0.57646} | {}    |


    Scenario Outline: Successfully comparing projections for PCAs with text options   
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I update the source with "<options>" waiting less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a pca with "<params>"
        And I wait until the pca is ready less than <time_3> secs
        When I create a projection for "<data_input>"
        Then the projection is "<projection>"
        And I create a local pca
        And I create a local projection for "<data_input>"
        Then the local projection is "<projection>"
        Then delete test data
                
        Examples:
        | data	| time_1  | time_2 | time_3 | options	|  data_input	| projection	| params    |
        | data/spam_tiny.csv	| 120      | 120     | 120  | {"fields": {"000001": {"optype": "text", "term_analysis": {"token_mode": "all"}}}}	| {"Message": "early"}	| {"PC40": 0.00416, "PC38": 0.08267, "PC39": 0.00033, "PC18": 0.28094, "PC19": -0.15056, "PC14": 0.20643, "PC15": 0.23931, "PC16": 0.03251, "PC17": 0.02776, "PC10": 0.1424, "PC11": 0.4059, "PC12": -0.1238, "PC13": 0.15131, "PC43": 0.29617, "PC42": 1.0091, "PC41": 0, "PC25": 0.07164, "PC24": -0.29904, "PC27": -0.1331, "PC26": -0.18572, "PC21": 0.25616, "PC20": 0.30424, "PC23": -0.45775, "PC22": -0.3362, "PC47": -0.13757, "PC49": 0.01864, "PC48": 0.04742, "PC29": -0.16286, "PC28": 0.42207, "PC32": -0.05917, "PC46": -0.05018, "PC31": -0.13973, "PC45": -0.05015, "PC36": 0.03017, "PC44": 0, "PC37": -0.06093, "PC34": 0.25821, "PC35": -0.22194, "PC33": -0.23398, "PC8": 0.01159, "PC9": -0.16042, "PC2": -0.09202, "PC3": 0.14371, "PC1": 0.65114, "PC6": -0.43034, "PC7": -0.02563, "PC4": -0.04947, "PC5": -0.07796, "PC50": -0.00769, "PC30": 0.07813} | {}	|
        | data/spam_tiny.csv	| 120      | 120     | 120  | {"fields": {"000001": {"optype": "text", "term_analysis": {"token_mode": "all"}}}}	| {"Message": "mobile call"}	| {"PC40": 0.31818, "PC38": 0.06912, "PC39": -0.14342, "PC18": 0.22382, "PC19": 0.18518, "PC14": 0.89231, "PC15": 0.05046, "PC16": -0.00241, "PC17": 0.54501, "PC10": -0.26463, "PC11": 0.30251, "PC12": 1.16327, "PC13": 0.16973, "PC43": 0.11952, "PC42": 1.05499, "PC41": 0.51263, "PC25": 0.02467, "PC24": -0.65128, "PC27": 0.48916, "PC26": -0.45228, "PC21": -0.44167, "PC20": 0.76896, "PC23": 0.29398, "PC22": 0.06425, "PC47": 0.70416, "PC49": -0.30313, "PC48": 0.12976, "PC29": -0.34, "PC28": 0.17406, "PC32": -0.06411, "PC46": 0.69257, "PC31": 0.07523, "PC45": -0.03461, "PC36": 0.29732, "PC44": 0.14516, "PC37": -0.19109, "PC34": 0.58399, "PC35": 0.37608, "PC33": -0.00378, "PC8": -0.88156, "PC9": 0.38233, "PC2": -0.56685, "PC3": 0.56321, "PC1": 0.49171, "PC6": -0.09854, "PC7": -1.24639, "PC4": 1.50134, "PC5": -0.03161, "PC50": 0.17349, "PC30": -1.29612} | {}	|
