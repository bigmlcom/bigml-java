Feature: Create TimeSeries

    Scenario Outline: Successfully creating forecasts from a dataset:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create time series from a dataset
        And I wait until the time series is ready less than <time_3> secs
        And I update the time series name to "<time_series_name>"
        When I wait until the time series is ready less than <time_4> secs
        Then the time series name is "<time_series_name>"
        And I create a forecast for "<input_data>"
        Then the forecasts are "<forecast_points>"

        Examples:
        | data             | time_1  | time_2 | time_3 | time_4 | time_series_name | input_data | forecast_points   |
        | data/grades.csv | 100      | 100     | 200     | 500 | my new time series name | {"000005": {"horizon": 5}}   | {"000005":[{"model":"A,N,N","lower_bound":[30.11219,30.11219,30.11219,30.11219,30.11219],"time_range":{"interval":1,"start":80,"interval_unit":"index","end":84},"upper_bound":[106.89267,106.89267,106.89267,106.89267,106.89267],"point_forecast":[68.50243,68.50243,68.50243,68.50243,68.50243]}]}    |
