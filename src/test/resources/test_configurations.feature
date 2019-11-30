Feature: Testing Configuration REST api calls
    In order to test the Configuration API
    I need to create a configuration

    Scenario Outline: Successfully creating a configuration:
        Given I create a configuration with "<options>"
        And I wait until the configuration is ready less than <time_1> secs
        And the configuration name is <name>
        And I update the configuration with "<params>"
        And the configuration name is <new_name>
        And I delete the configuration

        Examples:
        | time_1  | options                | name   |   params                            |   new_name             |
        |   50    | {"name": "my configuration", "configurations": {"ensemble": {"number_of_models": 10}, "any": {"tags": ["faq"]}}}    | "my configuration"    |   {"name": "my new configuration"}    | "my new configuration"   |
