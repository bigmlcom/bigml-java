@localpca
Feature: LocalPca

	Scenario Outline: Successfully comparing projections for PCAs
        Given I provision a dataset from "<data>" file
        And I create a pca with "<params>"
        And I wait until the pca is ready less than <time_1> secs
        And I create a local pca
        When I create a projection for "<data_input>"
        Then the projection is "<projection>"
        And I create a local projection for "<data_input>"
        Then the local projection is "<projection>"

        Examples:
        | data          | time_1  | data_input  | projection         | params    |
        | data/iris.csv | 120  | {}  | {"PC2": 0, "PC3": 0, "PC1": 0, "PC6": 0, "PC4": 5e-05, "PC5": 0} | {}    |
        | data/iris.csv | 120  | {"petal length": 1} | {"PC2": 0.08708, "PC3": 0.20929, "PC1": 1.56084, "PC6": -1.34463, "PC4": 0.7295, "PC5": -1.00876} | {}    |
        | data/iris.csv | 120  | {"species": "Iris-versicolor"}  | {"PC2": 1.8602, "PC3": -2.00864, "PC1": -0.61116, "PC6": -0.66983, "PC4": -2.44618, "PC5": 0.43414} | {} |
        | data/iris.csv | 120  | {"petal length": 1, "sepal length": 0, "petal width": 0, "sepal width": 0, "species": "Iris-versicolor"}    | {"PC2": 7.18009, "PC3": 6.51511, "PC1": 2.78155, "PC6": 0.21372, "PC4": -1.94865, "PC5": 0.57646} | {}    |


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

        Examples:
        | data  | time_1  | time_2 | time_3 | options |  data_input | projection  | params    |
        | data/spam_tiny.csv  | 120      | 120     | 120  | {"fields": {"000001": {"optype": "text", "term_analysis": {"token_mode": "all"}}}}  | {"Message": "early"}  | {"PC40": 0.00416, "PC38": 0.08267, "PC39": 0.00033, "PC18": 0.28094, "PC19": -0.15056, "PC14": 0.20643, "PC15": 0.23931, "PC16": 0.03251, "PC17": 0.02776, "PC10": 0.1424, "PC11": 0.4059, "PC12": -0.1238, "PC13": 0.15131, "PC43": 0.29617, "PC42": 1.0091, "PC41": 0, "PC25": 0.07164, "PC24": -0.29904, "PC27": -0.1331, "PC26": -0.18572, "PC21": 0.25616, "PC20": 0.30424, "PC23": -0.45775, "PC22": -0.3362, "PC47": -0.13757, "PC49": 0.01864, "PC48": 0.04742, "PC29": -0.16286, "PC28": 0.42207, "PC32": -0.05917, "PC46": -0.05018, "PC31": -0.13973, "PC45": -0.05015, "PC36": 0.03017, "PC44": 0, "PC37": -0.06093, "PC34": 0.25821, "PC35": -0.22194, "PC33": -0.23398, "PC8": 0.01159, "PC9": -0.16042, "PC2": -0.09202, "PC3": 0.14371, "PC1": 0.65114, "PC6": -0.43034, "PC7": -0.02563, "PC4": -0.04947, "PC5": -0.07796, "PC50": -0.00769, "PC30": 0.07813} | {} |
        | data/spam_tiny.csv  | 120      | 120     | 120  | {"fields": {"000001": {"optype": "text", "term_analysis": {"token_mode": "all"}}}}  | {"Message": "mobile call"}  | {"PC40": 0.31818, "PC38": 0.06912, "PC39": -0.14342, "PC18": 0.22382, "PC19": 0.18518, "PC14": 0.89231, "PC15": 0.05046, "PC16": -0.00241, "PC17": 0.54501, "PC10": -0.26463, "PC11": 0.30251, "PC12": 1.16327, "PC13": 0.16973, "PC43": 0.11952, "PC42": 1.05499, "PC41": 0.51263, "PC25": 0.02467, "PC24": -0.65128, "PC27": 0.48916, "PC26": -0.45228, "PC21": -0.44167, "PC20": 0.76896, "PC23": 0.29398, "PC22": 0.06425, "PC47": 0.70416, "PC49": -0.30313, "PC48": 0.12976, "PC29": -0.34, "PC28": 0.17406, "PC32": -0.06411, "PC46": 0.69257, "PC31": 0.07523, "PC45": -0.03461, "PC36": 0.29732, "PC44": 0.14516, "PC37": -0.19109, "PC34": 0.58399, "PC35": 0.37608, "PC33": -0.00378, "PC8": -0.88156, "PC9": 0.38233, "PC2": -0.56685, "PC3": 0.56321, "PC1": 0.49171, "PC6": -0.09854, "PC7": -1.24639, "PC4": 1.50134, "PC5": -0.03161, "PC50": 0.17349, "PC30": -1.29612} | {} |


		Scenario Outline: Successfully comparing remote and local predictions with raw date input for PCAs
        Given I provision a dataset from "<data>" file
        And I create a pca with "<params>"
        And I wait until the pca is ready less than <time_1> secs
        And I create a local pca
        When I create a projection for "<data_input>"
        Then the projection is "<projection>"
        And I create a local projection for "<data_input>"
        Then the local projection is "<projection>"

        Examples:
        | data          | time_1  | data_input  | projection         | params    |
        | data/dates2.csv	| 120  | {"time-1":"1910-05-08T19:10:23.106","cat-0":"cat2","target-2":0.4}  | {"PC8": -1.54293, "PC9": -0.94836, "PC2": 0.78176, "PC3": -0.62082, "PC1": 0.89614, "PC10": 1.06575, "PC11": 1.3211, "PC4": 1.90088, "PC5": 0.24197, "PC7": -0.37701, "PC6": 2.25007} | {}    |
				| data/dates2.csv	| 120  | {"time-1":"1920-06-30T20:21:20.320","cat-0":"cat1","target-2":0.2}  | {"PC8": 0.3148, "PC9": -0.61742, "PC2": 0.93411, "PC3": 1.80286, "PC1": 0.36425, "PC10": 0.7364, "PC11": 2.25863, "PC4": -1.50319, "PC5": 0.17088, "PC7": 0.51738, "PC6": 0.42403} | {}    |
				| data/dates2.csv	| 120  | {"time-1":"1932-01-30T19:24:11.440","cat-0":"cat2","target-2":0.1}  | {"PC8": -0.86728, "PC9": -1.85164, "PC2": 2.13206, "PC3": 0.58449, "PC1": 0.28379, "PC10": 2.05465, "PC11": 0.44372, "PC4": 1.27236, "PC5": 0.99468, "PC7": -0.32496, "PC6": 0.52217} | {}    |
        | data/dates2.csv	| 120  | {"time-1":"1950-11-06T05:34:05.602","cat-0":"cat1" ,"target-2":0.9}  | {"PC8": 2.49563, "PC9": -0.57774, "PC2": -0.76354, "PC3": 0.19215, "PC1": 0.99197, "PC10": -1.21017, "PC11": 1.55778, "PC4": -0.24013, "PC5": -0.38492, "PC7": 1.82607, "PC6": 0.3736} | {}    |
        | data/dates2.csv	| 120  | {"time-1":"1969-7-14 17:36","cat-0":"cat2","target-2":0.9}  | {"PC8": -0.41111, "PC9": -5.32959, "PC2": -1.25322, "PC3": 2.93113, "PC1": 2.07444, "PC10": 4.8808, "PC11": 0.4185, "PC4": 3.13876, "PC5": 3.70259, "PC7": 0.55665, "PC6": 5.16873} | {}    |
        | data/dates2.csv	| 120  | {"time-1":"2001-01-05T23:04:04.693","cat-0":"cat2","target-2":0.01}  | {"PC8": -1.10654, "PC9": -0.34137, "PC2": 1.73362, "PC3": -0.34799, "PC1": 2.32583, "PC10": 0.94566, "PC11": 0.53787, "PC4": 2.77385, "PC5": -0.1017, "PC7": 0.20156, "PC6": -0.44476} | {}    |
        | data/dates2.csv	| 120  | {"time-1":"2011-04-01T00:16:45.747","cat-0":"cat2","target-2":0.32}  | {"PC8": -0.514, "PC9": 0.38349, "PC2": -0.27037, "PC3": -1.82588, "PC1": 1.05737, "PC10": 0.08607, "PC11": -0.97078, "PC4": 2.10426, "PC5": 1.86843, "PC7": 1.55632, "PC6": 0.42395} | {}    |
        | data/dates2.csv	| 120  | {"time-1":"1969-W29-1T17:36:39Z","cat-0":"cat1","target-2":0.87}  | {"PC8": 2.05525, "PC9": 1.50754, "PC2": 6.27524, "PC3": 7.74224, "PC1": 5.30354, "PC10": -6.40442, "PC11": 6.90365, "PC4": -1.44431, "PC5": 2.16179, "PC7": 1.35718, "PC6": 5.02426} | {}    |
