package io.github.positionpal.location.application.geo

import scala.concurrent.duration.FiniteDuration

import io.github.positionpal.location.commons.CanRaise
import io.github.positionpal.location.domain.GPSLocation

/** The mode of routing to a destination. */
enum RoutingMode:
  case Driving, Walking, Cycling

/** An alias for the map service error. */
type MapsServiceError = String

/** A service to interact with maps and geolocation services.
  * @tparam M the effect constructor type.
  */
trait MapsService[M[_]: CanRaise[MapsServiceError]]:

  /** @return the [[Date]] of arrival to the [[destination]] from the [[origin]] using the given [[RoutingMode]]. */
  def duration(mode: RoutingMode)(origin: GPSLocation, destination: GPSLocation): M[FiniteDuration]

  /** @return the distance between the [[origin]] and the [[destination]] using the given [[RoutingMode]]. */
  def distance(mode: RoutingMode)(origin: GPSLocation, destination: GPSLocation): M[Distance]
