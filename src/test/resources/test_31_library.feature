Feature: Testing Whizzml Library REST api calls

    Scenario Outline: Scenario: Successfully creating a whizzml library:
        Given I create a whizzml library from a excerpt of code "<source_code>"
        And I wait until the library is ready less than <time_1> secs
        And I update the library name to "<library_name>"
        When I wait until the library is ready less than <time_2> secs
        Then the library name is "<library_name>"
        Then the library code is "<source_code>"
        
        Examples:
        | source_code                      | time_1  | time_2  | library_name |
        | (define (mu x) (+ x 1))          | 10      | 10      | my library |
