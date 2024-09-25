package io.github.positionpal.location.domain

import java.util.Date

/** An event, triggered by a primary actor, driving an application use case.
  * @param timestamp the timestamp when the event occurred
  * @param user the user who triggered the event
  */
enum DrivingEvents:

  /** An event triggered when a user starts routing to a destination. */
  case StartRouting(
      timestamp: Date,
      user: UserId,
      mode: RoutingMode,
      arrivalPosition: GPSLocation,
  ) extends DrivingEvents

  /** An event triggered by a user when needing help. */
  case SOSAlert(
      timestamp: Date,
      user: UserId,
      position: GPSLocation,
  ) extends DrivingEvents

  /** An event triggered regularly on behalf of a user, tracking its position. */
  case Tracking(
      timestamp: Date,
      user: UserId,
      position: GPSLocation,
  ) extends DrivingEvents

  /** An event triggered by a user when stopping the SOS alert. */
  case StopSOS(
      timeStamp: Date,
      user: UserId,
  )

enum DrivenEvents:
  case StopRouting(
      timestamp: Date,
      user: UserId,
  ) extends DrivenEvents
