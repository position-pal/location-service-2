#Feature: Users real-time tracking
#
#  Background:
#    Given I'm a logged user
#    And I'm in a group with other users
#
#  Scenario: User can track other users in their groups in real-time
#    When I track the other group's users
#    Then I should see their location in real-time or their last known location if they offline
#
#  Scenario: User is able to share their location with other group members
#    When I start sharing my location
#    Then they should see my location in real-time
#      * my last known location should be updated
#      * my state should be updated to "Active"
#
#  Scenario: User can stop sharing their location with other group members
#    When I stop sharing my location with that group
#    Then the group's memembers should not see my location anymore
#    And my state should be updated to "Inactive"
#
#  Scenario: User can activate the routing mode
#    When I activate the routing mode
#    Then my state should be updated to "Routing"
#      * the group's members should receive a notification
#      * the app should start recording the route
#
#  Scenario: User's route in routing mode is recorded
#    Given I'm in routing mode
#    When a position update is received
#    Then it is added to the route
