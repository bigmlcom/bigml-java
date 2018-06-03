Feature: Testing Whizzml Execution REST api calls

    Scenario Outline: Scenario: Successfully creating a whizzml script execution:
        Given I create a whizzml script from a excerpt of code "<source_code>"
        And I wait until the script is ready less than <time_1> secs
        And I create a whizzml script execution from an existing script
        And I wait until the execution is ready less than <time_2> secs
        And I update the execution with "<param>", "<param_value>"
        And I wait until the execution update is ready less than <time_3> secs
        Then the script id is correct, the value of "<param>" is "<param_value>" and the result is "<result>"

        Examples:
        | source_code   | time_1  | time_2  | time_3  | param | param_value | result    |
        | (+ 1 1)   | 10      | 10      | 10      | name  | my execution | 2   |


    Scenario Outline: Scenario: Successfully creating a whizzml script execution from a list of scripts:
        Given I reset scripts
        Given I create a whizzml script from a excerpt of code "<source_code>"
        And I wait until the script is ready less than <time_1> secs
        And I create a whizzml script from a excerpt of code "<source_code>"
        And I wait until the script is ready less than <time_1> secs
        And I create a whizzml script execution from the last two scripts
        And I wait until the execution is ready less than <time_2> secs
        And I update the execution with "<param>", "<param_value>"
        And I wait until the execution update is ready less than <time_3> secs
        Then the value of "<param>" is "<param_value>" and the result is "<result>"

        Examples:
        | source_code   | time_1  | time_2  | time_3  | param | param_value | result    |
        | (+ 1 1)   | 10      | 10      | 10      | name  | my execution | [2,2]   |
