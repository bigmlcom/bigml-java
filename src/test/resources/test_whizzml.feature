Feature: Whizzml Resources REST api calls
	
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
        

		Scenario Outline: Scenario: Successfully creating a whizzml library:
        Given I create a whizzml script from a excerpt of code "<source_code>"
        And I wait until the script is ready less than <time_1> secs
        And I update the script name to "<script_name>"
        When I wait until the script is ready less than <time_2> secs
        Then the script name is "<script_name>"
        Then the script code is "<source_code>"

        Examples:
        | source_code   | time_1  | time_2  | script_name |
        | (+ 1 1)   | 10      | 10      | my script |
        

		Scenario Outline: Scenario: Successfully creating a whizzml script execution:
        Given I create a whizzml script from a excerpt of code "<source_code>"
        And I wait until the script is ready less than <time_1> secs
        And I create a whizzml script execution from an existing script
        And I wait until the execution is ready less than <time_2> secs
        And I update the execution name to "<execution_name>"
        When I wait until the execution is ready less than <time_2> secs
        Then the execution name is "<execution_name>"
        Then the script id is correct and the result is "<result>"

        Examples:
        | source_code   | time_1  | time_2  | time_3  | execution_name | result    |
        | (+ 1 1)   | 10      | 10      | 10      | my execution | 2   |


    Scenario Outline: Scenario: Successfully creating a whizzml script execution from a list of scripts:
        Given I reset scripts
        Given I create a whizzml script from a excerpt of code "<source_code>"
        And I wait until the script is ready less than <time_1> secs
        And I create a whizzml script from a excerpt of code "<source_code>"
        And I wait until the script is ready less than <time_1> secs
        And I create a whizzml script execution from the last two scripts
        And I wait until the execution is ready less than <time_2> secs
        And I update the execution name to "<execution_name>"
        When I wait until the execution is ready less than <time_2> secs
        Then the execution name is "<execution_name>"
        Then the result is "<result>"

        Examples:
        | source_code   | time_1  | time_2  | time_3  | execution_name | result    |
        | (+ 1 1)   | 10      | 10      | 10      | my execution | [2,2]   |
