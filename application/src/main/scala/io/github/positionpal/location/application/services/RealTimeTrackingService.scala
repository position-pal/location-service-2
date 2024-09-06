package io.github.positionpal.location.application.services

import io.github.positionpal.location.domain.DrivingEvents

/** The real-time tracking service in charge of handling the [[DrivingEvents]]. */
trait RealTimeTrackingService[M[_]]:
  def handle(event: DrivingEvents): M[Unit]
