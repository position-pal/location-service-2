package io.github.positionpal.location.application.geo

import io.github.positionpal.location.domain.{DrivingEvents, Route}

/** A reaction to [[DrivingEvents.TrackingEvent]]s. */
object TrackingEventReaction extends BinaryShortCircuitReaction:
  case object Continue
  final case class TriggerNotification(reason: String)

  override type Environment = Route
  override type Event = DrivingEvents.TrackingEvent
  override type LeftOutcome = TriggerNotification
  override type RightOutcome = Continue.type
