Feature: Test that features can be used

  Scenario: Setup WireMock in Given and try it out in then
    Given WireMock has endpint ping
    When WireMock is invoked with ping
    Then it should respond 200
    When WireMock is invoked with pang
    Then it should respond 404
