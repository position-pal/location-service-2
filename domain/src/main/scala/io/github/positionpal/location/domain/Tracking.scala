package io.github.positionpal.location.domain

import java.util.Date

type Route = LazyList[SampledLocation]

trait Tracking:
  def route: Route
  def user: UserId
  def addSample(sample: SampledLocation): Tracking

trait MonitorableTracking extends Tracking:
  def mode: RoutingMode
  def destination: GPSLocation
  def expectedArrival: Date
  override def addSample(sample: SampledLocation): MonitorableTracking

/** The mode of routing to a destination. */
enum RoutingMode:
  case Driving, Walking, Cycling

object Tracking:

  def apply(userId: UserId): Tracking = TrackingImpl(LazyList(), userId)

  def withMonitoring(
      userId: UserId,
      routingMode: RoutingMode,
      arrivalLocation: GPSLocation,
      estimatedArrival: Date,
  ): MonitorableTracking = MonitorableTrackingImpl(LazyList(), userId, routingMode, arrivalLocation, estimatedArrival)

  private class TrackingImpl(override val route: Route, override val user: UserId) extends Tracking:
    override def addSample(sample: SampledLocation): Tracking = TrackingImpl(sample #:: route, user)

  private class MonitorableTrackingImpl(
      override val route: Route,
      override val user: UserId,
      override val mode: RoutingMode,
      override val destination: GPSLocation,
      override val expectedArrival: Date,
  ) extends MonitorableTracking:
    override def addSample(sample: SampledLocation): MonitorableTracking =
      MonitorableTrackingImpl(sample #:: route, user, mode, destination, expectedArrival)
