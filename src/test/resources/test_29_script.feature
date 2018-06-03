Feature: Testing Whizzml Script REST api calls

    Scenario Outline: Scenario: Successfully creating a whizzml library:
        Given I create a whizzml script from a excerpt of code "<source_code>"
        And I wait until the script is ready less than <time_1> secs
        And I update the script with "<param>", "<param_value>"
        And I wait until the script update is ready less than <time_2> secs
        Then the script code is "<source_code>" and the value of "<param>" is "<param_value>"

        Examples:
        | source_code   | time_1  | time_2  | param | param_value |
        | (+ 1 1)   | 10      | 10      | name  | my script |
