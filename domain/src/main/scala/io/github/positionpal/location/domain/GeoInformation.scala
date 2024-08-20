package io.github.positionpal.location.domain

/** The latitude of a [[GPSLocation]]. */
type Latitude = Double

/** The longitude of a [[GPSLocation]]. */
type Longitude = Double

/** A GPS location, identified by a [[Latitude]] and a [[Longitude]]. */
final case class GPSLocation(latitude: Latitude, longitude: Longitude)

/** A type class providing the ability to locate an object of type [[A]]. */
trait Geolocatable[A]:

  /** @return the GPS location of the object [[a]]. */
  def location(a: A): GPSLocation

object Geolocatable:
  extension [A: Geolocatable](a: A) def location: GPSLocation = summon[Geolocatable[A]].location(a)
