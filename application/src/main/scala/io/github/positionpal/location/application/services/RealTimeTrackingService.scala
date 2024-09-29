package io.github.positionpal.location.application.services

import io.github.positionpal.location
import io.github.positionpal.location.application.services
import io.github.positionpal.location.domain.DrivingEvent

/** The real-time tracking service in charge of handling the [[DrivingEvent]]. */
trait RealTimeTrackingService[M[_]]:

  /** Handle the [[event]]. */
  def handle[U](event: DrivingEvent): M[U]
