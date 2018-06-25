Feature: Testing projects REST api calls
    In order to test the project API
    I need to create a project

    Scenario Outline: Successfully creating a project:
        Given I create a project with "<options>"
        And I wait until the project is ready less than <time_1> secs
        And the project name is <name>
        And I update the project with "<params>"
        And the project name is <new_name>
        And I delete the project
        Then delete test data

        Examples:
        | time_1  | options                |   name   |   params                            |   new_name             |
        |   50    | {"name": "my project"}    | "my project"	|  {"name": "my new project"}    | "my new project"   |
