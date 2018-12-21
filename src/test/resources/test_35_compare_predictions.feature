Feature: Testing 
	
    Scenario Outline: Successfully comparing forecasts for Timeseries
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a time series from a dataset with "<params>"
        And I wait until the time series is ready less than <time_3> secs
        And I create a local timeseries
        When I create a forecast for "<data_input>"
        Then the forecasts are "<forecasts>"
        And I create a local forecast for "<data_input>"
        Then the local forecasts are "<forecasts>"

        Examples:
        | data                    | time_1  | time_2 | time_3 | data_input                                    | forecasts         | params    |
        | data/grades.csv         | 50      | 50     | 30000  | {"000005": {"horizon": 5}}                    | {"000005": [{"point_forecast": [73.96192, 74.04106, 74.12029, 74.1996, 74.27899], "model": "M,M,N"}]}          | {"objective_fields": ["000001", "000005"]}    |
        | data/grades.csv         | 50      | 50     | 30000  | {"000005": {"horizon": 5, "ets_models": {"names": ["M,N,N"], "criterion": "aic", "limit": 3}}}         | {"000005": [{"point_forecast":  [68.39832, 68.39832, 68.39832, 68.39832, 68.39832], "model": "M,N,N"}]}          | {"objective_fields": ["000001", "000005"]}    |
        | data/grades.csv         | 50      | 50     | 30000  | {"000005": {"horizon": 5, "ets_models": {"names": ["A,A,N"], "criterion": "aic", "limit": 3}}}                    | {"000005": [{"point_forecast": [72.46247, 72.56247, 72.66247, 72.76247, 72.86247], "model": "A,A,N"}]}          | {"objective_fields": ["000001", "000005"]}    |
        | data/grades.csv         | 50      | 50     | 30000  | {"000005": {"horizon": 5}, "000001": {"horizon": 3, "ets_models": {"criterion": "aic", "limit": 2}}}               | {"000005": [{"point_forecast": [73.96192, 74.04106, 74.12029, 74.1996, 74.27899], "model": "M,M,N"}], "000001": [{"point_forecast": [55.51577, 89.69111, 82.04935], "model": "A,N,A"}, {"point_forecast": [56.67419, 91.89657, 84.70017], "model": "A,A,A"}]}          | {"objective_fields": ["000001", "000005"]}    |


    Scenario Outline: Successfully comparing forecasts for Timeseries with seasonality "A"
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a time series from a dataset with "<params>"
        And I wait until the time series is ready less than <time_3> secs
        And I create a local timeseries
        When I create a forecast for "<data_input>"
        Then the forecasts are "<forecasts>"
        And I create a local forecast for "<data_input>"
        Then the local forecasts are "<forecasts>"

        Examples:
        | data                    | time_1  | time_2 | time_3 | data_input                                    | forecasts         | params    |
        | data/grades.csv         | 50      | 50     | 30000  | {"000005": {"horizon": 5}}                    | {"000005": [{"point_forecast": [73.96192, 74.04106, 74.12029, 74.1996, 74.27899], "model": "M,M,N"}]}          | {"objective_fields": ["000001", "000005"], "period" : 12 }    |
        | data/grades.csv         | 50      | 50     | 30000  | {"000005": {"horizon": 5, "ets_models": {"names": ["M,N,A"], "criterion": "aic", "limit": 3}}}              | {"000005": [{"point_forecast":  [67.43222, 68.24468, 64.14437, 67.5662, 67.79028], "model": "M,N,A"}]}          | {"objective_fields": ["000001", "000005"], "period" : 12 }    |
        | data/grades.csv         | 50      | 50     | 30000  | {"000005": {"horizon": 5, "ets_models": {"names": ["A,A,A"], "criterion": "aic", "limit": 3}}}              | {"000005": [{"point_forecast": [74.73553, 71.6163, 71.90264, 76.4249, 75.06982], "model": "A,A,A"}]}         | {"objective_fields": ["000001", "000005"], "period" : 12 }    |

    Scenario Outline: Successfully comparing forecasts for Timeseries with seasonality "M"
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a time series from a dataset with "<params>"
        And I wait until the time series is ready less than <time_3> secs
        And I create a local timeseries
        When I create a forecast for "<data_input>"
        Then the forecasts are "<forecasts>"
        And I create a local forecast for "<data_input>"
        Then the local forecasts are "<forecasts>"

        Examples:
        | data                    | time_1  | time_2 | time_3 | data_input                                    | forecasts         | params    |
        | data/grades.csv         | 50      | 50     | 30000  | {"000005": {"horizon": 5, "ets_models": {"names": ["M,N,M"], "criterion": "aic", "limit": 3}}}          | {"000005": [{"point_forecast":  [68.99775, 72.76777, 66.5556, 70.90818, 70.92998], "model": "M,N,M"}]}          | {"objective_fields": ["000001", "000005"], "period": 12 }    |
        | data/grades.csv         | 50      | 50     | 30000  | {"000005": {"horizon": 5, "ets_models": {"names": ["M,A,M"], "criterion": "aic", "limit": 3}}}          | {"000005": [{"point_forecast": [70.65993, 78.20652, 69.64806, 75.43716, 78.13556], "model": "M,A,M"}]}          | {"objective_fields": ["000001", "000005"], "period": 12 }    |
        | data/grades.csv         | 50      | 50     | 30000  | {"000005": {"horizon": 5, "ets_models": {"names": ["M,M,M"], "criterion": "aic", "limit": 3}}}          | {"000005": [{"point_forecast": [71.75055, 80.67195, 70.81368, 79.84999, 78.27634], "model": "M,M,M"}]}          | {"objective_fields": ["000001", "000005"], "period": 12 }    |


    Scenario Outline: Successfully comparing forecasts for Timeseries with trivial models
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a time series from a dataset with "<params>"
        And I wait until the time series is ready less than <time_3> secs
        And I create a local timeseries
        When I create a forecast for "<data_input>"
        Then the forecasts are "<forecasts>"
        And I create a local forecast for "<data_input>"
        Then the local forecasts are "<forecasts>"

        Examples:
        | data                    | time_1  | time_2 | time_3 | data_input                                    | forecasts         | params    |
        | data/grades.csv         | 50      | 50     | 30000  | {"000005": {"horizon": 5, "ets_models": {"names": ["naive"]}}}         | {"000005": [{"point_forecast": [61.39, 61.39, 61.39, 61.39, 61.39], "model": "naive"}]}          | {"objective_fields": ["000001", "000005"], "period": 1}    |
        | data/grades.csv         | 50      | 50     | 30000  | {"000005": {"horizon": 5, "ets_models": {"names": ["naive"]}}}         | {"000005": [{"point_forecast": [78.89,61.39,78.89,61.39,78.89], "model": "naive"}]}          | {"objective_fields": ["000001", "000005"], "period": 2}     |
        | data/grades.csv         | 50      | 50     | 30000  | {"000005": {"horizon": 5, "ets_models": {"names": ["mean"]}}}         | {"000005": [{"point_forecast": [69.79553,67.15821,69.79553,67.15821,69.79553], "model": "mean"}]}          | {"objective_fields": ["000001", "000005"], "period": 2}     |
        | data/grades.csv         | 50      | 50     | 30000  | {"000005": {"horizon": 5, "ets_models": {"names": ["mean"]}}}         | {"000005": [{"point_forecast": [68.45974, 68.45974, 68.45974, 68.45974, 68.45974], "model": "mean"}]}          | {"objective_fields": ["000001", "000005"], "period": 1}     |
        | data/grades.csv         | 50      | 50     | 30000  | {"000005": {"horizon": 5, "ets_models": {"names": ["drift"]}}}         | {"000005": [{"point_forecast": [61.50545, 61.6209, 61.73635, 61.8518, 61.96725], "model": "drift"}]}          | {"objective_fields": ["000001", "000005"], "period": 1}     |
        | data/grades.csv         | 50      | 50     | 30000  | {"000005": {"horizon": 5, "ets_models": {"names": ["drift"]}}}         | {"000005": [{"point_forecast": [61.50545, 61.6209, 61.73635, 61.8518, 61.96725], "model": "drift"}]}          | {"objective_fields": ["000001", "000005"], "period": 2}     |

  
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
                
        Examples:
        | data                    | time_1  | time_2 | time_3 | options	|  data_input	| projection         | params    |
        | data/spam_tiny.csv         | 120      | 120     | 120  | {"fields": {"000001": {"optype": "text", "term_analysis": {"token_mode": "all"}}}}	| {"Message": "early"}	| {"PC40": 0, "PC38": 0.08267, "PC39": -0.10633, "PC18": 0.28096, "PC19": 0.1505, "PC14": -0.20652, "PC15": 0.23924, "PC16": 0.03248, "PC17": 0.02777, "PC10": 0.14244, "PC11": 0.40589, "PC12": -0.12382, "PC13": 0.15128, "PC43": -0.48973, "PC42": 0, "PC41": 0, "PC25": 0.07161, "PC24": -0.29909, "PC27": -0.13314, "PC26": -0.18576, "PC21": -0.25621, "PC20": 0.30426, "PC23": -0.45777, "PC22": 0.3362, "PC47": -0.11743, "PC49": 0.00225, "PC48": 0.0045, "PC29": -0.16286, "PC28": 0.42213, "PC32": 0.05915, "PC46": 0.11033, "PC31": -0.13972, "PC45": 0, "PC36": 0.03019, "PC44": -0.92876, "PC37": -0.06094, "PC34": 0.2582, "PC35": 0.22196, "PC33": -0.23398, "PC8": 0.01171, "PC9": -0.16039, "PC2": -0.09201, "PC3": -0.1437, "PC1": 0.65121, "PC6": -0.43035, "PC7": -0.02564, "PC4": -0.04947, "PC5": -0.07793, "PC50": -0.02052, "PC30": 0.07813} | {}	|
        | data/spam_tiny.csv         | 120      | 120     | 120  | {"fields": {"000001": {"optype": "text", "term_analysis": {"token_mode": "all"}}}}	| {"Message": "mobile call"}	| {"PC40": 0.21048, "PC38": 0.06911, "PC39": 0.6909, "PC18": 0.22386, "PC19": -0.18525, "PC14": -0.89241, "PC15": 0.05028, "PC16": -0.00258, "PC17": 0.54497, "PC10": -0.26462, "PC11": 0.30254, "PC12": 1.16324, "PC13": 0.16976, "PC43": 0.16188, "PC42": 0.04285, "PC41": 0.18507, "PC25": 0.02459, "PC24": -0.65128, "PC27": 0.48914, "PC26": -0.45234, "PC21": 0.44167, "PC20": 0.76901, "PC23": 0.29393, "PC22": -0.06424, "PC47": -0.47623, "PC49": 0.9256, "PC48": -0.54256, "PC29": -0.34001, "PC28": 0.17412, "PC32": 0.06415, "PC46": -0.33441, "PC31": 0.07518, "PC45": 0.17592, "PC36": 0.29734, "PC44": -1.10057, "PC37": -0.19109, "PC34": 0.58401, "PC35": -0.37607, "PC33": -0.00376, "PC8": -0.88178, "PC9": 0.38177, "PC2": -0.56673, "PC3": -0.56334, "PC1": 0.49177, "PC6": -0.09859, "PC7": -1.2464, "PC4": 1.50129, "PC5": -0.03164, "PC50": -0.28806, "PC30": -1.29616} | {}	|
