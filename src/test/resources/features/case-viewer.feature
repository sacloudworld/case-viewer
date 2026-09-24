Feature: Case Viewer

  Scenario: Successfully view a case

    Given the Case Viewer application is running
    When I login as user "sachin" with password "password123" 
    And I request case "CASE-100001"
    Then the response status should be 200
    And the response should contain case number "CASE-100001"
    And the response should contain activities