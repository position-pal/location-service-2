package io.github.positionpal.location.application.services

import io.github.positionpal.location.application.geo.MapsService
import io.github.positionpal.location.application.storage.TrackingEventsWriter
import io.github.positionpal.location.domain.DrivingEvents

trait RealTimeTrackingServiceComponent[M[_]]:
  context: TrackingEventsWriter[M] & MapsService[M] =>

  /** The real-time tracking service in charge of handling the [[DrivingEvents]]. */
  trait RealTimeTrackingService:
    /** Handle the [[event]]. */
    def handle[U](event: DrivingEvents): M[U]

  object RealTimeTrackingService:
    def apply(): RealTimeTrackingService = RealTimeTrackingServiceImpl()

    private class RealTimeTrackingServiceImpl extends RealTimeTrackingService:
      override def handle[U](event: DrivingEvents): M[U] = ???
