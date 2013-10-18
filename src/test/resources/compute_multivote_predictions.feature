Feature: Compute MultiVote predictions
    In order compute combined predictions
    I need to create a MultiVote object

    Scenario Outline: Successfully computing predictions combinations:
        Given I create a MultiVote for the set of predictions in file <predictions>
        When I compute the prediction with confidence using method "<method>"
        Then the combined prediction is "<prediction>"
        And the confidence for the combined prediction is <confidence>

        Examples:
        | predictions               | method       | prediction    | confidence            |
        | data/predictions_c.json| 0            | a             | 0.450471270879        |
        | data/predictions_c.json| 1            | a             | 0.552021302649        |
        | data/predictions_c.json| 2            | a             | 0.403632421178        |
        