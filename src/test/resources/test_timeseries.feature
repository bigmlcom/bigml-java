Feature: TimeSeries

    Scenario Outline: Successfully creating forecasts from a dataset:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create a time series from a dataset
        And I wait until the time series is ready less than <time_3> secs
        And I update the time series name to "<time_series_name>"
        When I wait until the time series is ready less than <time_4> secs
        Then the time series name is "<time_series_name>"
        And I create a forecast for "<input_data>"
        Then the forecasts are "<forecast_points>"

        Examples:
        | data             | time_1  | time_2 | time_3 | time_4 | time_series_name | input_data | forecast_points   |
        | data/grades.csv | 100      | 100     | 200     | 500 | my new time series name | {"000005": {"horizon": 5}}   | {"000005": [{"point_forecast": [73.96192, 74.04106, 74.12029, 74.1996, 74.27899], "model": "M,M,N"}]}    |

    
    Scenario Outline: Successfully comparing forecasts
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


    Scenario Outline: Successfully comparing forecasts with seasonality "A"
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


    Scenario Outline: Successfully comparing forecasts with seasonality "M"
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


    Scenario Outline: Successfully comparing forecasts with trivial models
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
    