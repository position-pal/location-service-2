package io.github.positionpal.location.domain

import java.util.Date

type Route = List[SampledLocation]

trait Tracking:
  def route: Route
  def user: UserId
  def addSample(sample: SampledLocation): Tracking
  def +(sample: SampledLocation): Tracking = addSample(sample)

trait MonitorableTracking extends Tracking:
  def mode: RoutingMode
  def destination: GPSLocation
  def expectedArrival: Date
  override def addSample(sample: SampledLocation): MonitorableTracking
  override def +(sample: SampledLocation): MonitorableTracking = addSample(sample)

/** The mode of routing to a destination. */
enum RoutingMode:
  case Driving, Walking, Cycling

object Tracking:

  def apply(userId: UserId, route: Route = List()): Tracking = TrackingImpl(route, userId)

  def withMonitoring(
      userId: UserId,
      routingMode: RoutingMode,
      arrivalLocation: GPSLocation,
      estimatedArrival: Date,
      route: Route = List(),
  ): MonitorableTracking = MonitorableTrackingImpl(route, userId, routingMode, arrivalLocation, estimatedArrival)

  private case class TrackingImpl(override val route: Route, override val user: UserId) extends Tracking:
    override def addSample(sample: SampledLocation): Tracking = TrackingImpl(sample :: route, user)

  private case class MonitorableTrackingImpl(
      override val route: Route,
      override val user: UserId,
      override val mode: RoutingMode,
      override val destination: GPSLocation,
      override val expectedArrival: Date,
  ) extends MonitorableTracking:
    override def addSample(sample: SampledLocation): MonitorableTracking =
      MonitorableTrackingImpl(sample :: route, user, mode, destination, expectedArrival)
