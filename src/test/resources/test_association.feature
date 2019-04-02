Feature: Assocaition

    Scenario Outline: Successfully creating associations from a dataset:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create an association from a dataset
        And I wait until the association is ready less than <time_3> secs
        And I update the association name to "<association_name>"
        When I wait until the association is ready less than <time_4> secs
        Then the association name is "<association_name>"
        Then I delete the association

        Examples:
        | data                | time_1  | time_2 | time_3 | time_4 | association_name |
        | data/iris.csv | 10      | 10     | 20     | 20 | my new association name |


    Scenario Outline: Successfully creating local association object:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create an association from a dataset
        And I wait until the association is ready less than <time_3> secs
        And I create a local association
        When I get the rules for "<item_list>" and the first rule is "<JSON_rule>"

        Examples:
        | data                | time_1  | time_2 | time_3 | item_list | JSON_rule   |
        | data/tiny_mushrooms.csv | 10      | 10     | 50   | Edible |{"p_value":5.26971E-31,"leverage":0.24986,"lhs":[0,21,16,7],"lhs_cover":[0.488,122],"rhs_cover":[0.488,122],"confidence":1,"lift":2.04918,"id":"000002","rhs":[19],"support":[0.488,122]} |


    Scenario Outline: Successfully creating local association object:
        Given I create a data source uploading a "<data>" file
        And I wait until the source is ready less than <time_1> secs
        And I create a dataset
        And I wait until the dataset is ready less than <time_2> secs
        And I create an association with search strategy "<strategy>" from a dataset
        And I wait until the association is ready less than <time_3> secs
        And I create a local association
        When I get the rules for "<item_list>" and the first rule is "<JSON_rule>"

        Examples:
        | data                | time_1  | time_2 | time_3 | item_list | JSON_rule   | strategy   |
        | data/tiny_mushrooms.csv | 10      | 10     | 50   | Edible | {"p_value":2.08358E-17,"leverage":0.07885,"lhs":[0],"lhs_cover":[0.888,222],"rhs_cover":[0.704,176],"confidence":0.79279,"lift":1.12613,"id":"000007","rhs":[11],"support":[0.704,176]} | lhs_cover |
