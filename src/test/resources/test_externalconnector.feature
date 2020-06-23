Feature: ExternalConnector REST api calls

    Scenario Outline: Successfully creating an external connector
        Given I create an externalconnector for "<source>" with "<connection_info>"
        And I wait until the externalconnector is ready less than <time_1> secs
        And I update the externalconnector with "<params>"
        And the externalconnector name is <new_name>
        And I create a source from the external connector id
        And I delete the externalconnector

        Examples:
        | time_1 | source 	| connection_info	| params |   new_name             |
        | 50     | mysql 	| {"database": "sorex_araneus_core_97_1", "host": "ensembldb.ensembl.org", "user": "anonymous", "use_ssl": "false", "password": "", "port": 5306, "verify_certs": "false"} | {"name": "my new externalconnector"}    | "my new externalconnector"   |
