Feature: Create TimeSeries

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
        | data/grades.csv | 100      | 100     | 200     | 500 | my new time series name | {"000005": {"horizon": 5}}   | {"000005":[{"lower_bound":[31.6734,32.9738,34.0285,34.3258,34.5127],"time_range":{"interval_unit":"index","start":80,"end":84,"interval":1,"descending":false},"model":"M,M,N","upper_bound":[114.057,112.761,114.422,114.02,116.168],"point_forecast":[73.96192,74.04106,74.12029,74.1996,74.27899]}]}    |
