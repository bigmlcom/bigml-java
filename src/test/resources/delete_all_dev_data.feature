Feature: Delete all test data
    
    Scenario: Successfully deleting all data in production mode
 		Given that I use production mode
        Then delete all test data

    Scenario: Successfully deleting all data in development mode
        Given that I use development mode
        Then delete all test data

  	