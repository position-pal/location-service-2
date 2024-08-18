package io.github.positionpal.location.application.geo

/** A distance record, identified by a [[value]] and a [[unit]] of measurement. */
final case class Distance(value: Double, unit: Distance.DistanceUnit = Distance.DistanceUnit.Meters)

object Distance:
  enum DistanceUnit:
    case Meters, Kilometers

  extension (d: Double)
    def meters: Distance = Distance(d, DistanceUnit.Meters)
    def kilometers: Distance = Distance(d, DistanceUnit.Kilometers)

  extension (d: Distance)
    def toKilometers: Distance = d.unit match
      case DistanceUnit.Meters => Distance(d.value / 1_000, DistanceUnit.Kilometers)
      case _ => d

    def toMeters: Distance = d.unit match
      case DistanceUnit.Kilometers => Distance(d.value * 1_000, DistanceUnit.Meters)
      case _ => d
