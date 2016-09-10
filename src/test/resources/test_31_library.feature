Feature: Testing Whizzml Library REST api calls

    Scenario Outline: Scenario: Successfully creating a whizzml library:
        Given that I use production mode with seed="<seed>"
        Given I create a whizzml library from a excerpt of code "<source_code>"
        And I wait until the library is ready less than <time_1> secs
        And I update the library with "<param>", "<param_value>"
        And I wait until the library update is ready less than <time_2> secs
        Then the library code is "<source_code>" and the value of "<param>" is "<param_value>"

        Examples:
        | source_code                      | time_1  | time_2  | param | param_value |
        | (define (mu x) (+ x 1))          | 10      | 10      | name  | my library |
