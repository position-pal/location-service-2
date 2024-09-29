package io.github.positionpal.location.domain

import java.util.Date

/** An event, triggered by a primary actor, driving an application use case. */
sealed trait DrivingEvent:
  /** The timestamp when the event occurred. */
  def timestamp: Date

  /** The user who triggered the event. */
  def user: UserId

/** An event triggered when a user starts routing to a destination. */
case class StartRouting(
    timestamp: Date,
    user: UserId,
    mode: RoutingMode,
    destination: GPSLocation,
    expectedArrival: Date,
) extends DrivingEvent

/** An event triggered by a user when needing help. */
case class SOSAlert(timestamp: Date, user: UserId, position: GPSLocation) extends DrivingEvent

/** An event triggered regularly on behalf of a user, tracking its position. */
case class Tracking(timestamp: Date, user: UserId, position: GPSLocation) extends DrivingEvent

/** An event triggered by a user when stopping the SOS alert. */
case class StopSOS(timestamp: Date, user: UserId) extends DrivingEvent

/** An event triggered by a user when stopping the routing. */
case class StopRouting(timestamp: Date, user: UserId) extends DrivingEvent
