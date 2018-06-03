Feature: Testing projects REST api calls
    In order to test the project API
    I need to create a project

    Scenario Outline: Successfully creating a project:
        Given I create a project with name <name>
        And I wait until the project is ready less than <time_1> secs
        And I check the project name <name>
        And I update the project with "<params>"
        And I check the project name <new_name>
        And I delete the project
        Then delete test data

        Examples:
        | time_1  | name                |   params                            |   new_name             |
        |   50    | "my project"    |   {"name": "my new project"}    | "my new project"   |
