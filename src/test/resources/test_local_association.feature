Feature: LocalAssociation

    Scenario Outline: Successfully creating local association object:
        Given I provision a dataset from "<data>" file
        And I create an association from a dataset
        And I wait until the association is ready less than <time_1> secs
        And I create a local association
        When I get the rules for "<item>" and the first rule is "<JSON_rule>"

        Examples:
        | data  | time_1 | item | JSON_rule   |
        | data/tiny_mushrooms.csv | 50  | Edible | {"p_value":5.26971E-31,"leverage":0.24986,"lhs":[0,21,16,7],"lhs_cover":[0.488,122],"rhs_cover":[0.488,122],"confidence":1,"lift":2.04918,"id":"000002","rhs":[19],"support":[0.488,122]} |


    Scenario Outline: Successfully creating local association object:
        Given I provision a dataset from "<data>" file
        And I create an association with search strategy "<strategy>" from a dataset
        And I wait until the association is ready less than <time_1> secs
        And I create a local association
        When I get the rules for "<item>" and the first rule is "<JSON_rule>"

        Examples:
        | data  | time_1  | item | JSON_rule   | strategy   |
        | data/tiny_mushrooms.csv | 50   | Edible | {"p_value":2.08358E-17,"leverage":0.07885,"lhs":[0],"lhs_cover":[0.888,222],"rhs_cover":[0.704,176],"confidence":0.79279,"lift":1.12613,"id":"000007","rhs":[11],"support":[0.704,176]} | lhs_cover |


	Scenario Outline: Successfully comparing scores from associations:
        Given I provision a dataset from "<data>" file
        And I create an association from a dataset with "<params>"
        And I wait until the association is ready less than <time_1> secs
        And I create a local association
        And I create a local association set for "<data_input>"
        Then the local association set is "<set>"

      Examples:
        | data	| time_1  | data_input	| set	| params	|
        | data/iris.csv	| 1000	| {}	| []  | {}	|
        | data/iris.csv	| 1000	| {}	| [{"score":0.12,"item":{"complementId":null,"count":50,"name":"Iris-versicolor","complement":false,"fieldId":"000004"},"rules":["00000d"]}]  | {"default_numeric_value": "mean"}	|
