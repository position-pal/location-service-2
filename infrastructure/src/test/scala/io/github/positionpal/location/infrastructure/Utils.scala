package io.github.positionpal.location.infrastructure

import io.github.positionpal.location.domain.GPSLocation

object GeoUtils:
  val cesenaCampusLocation: GPSLocation = GPSLocation(44.1476299926484, 12.2357184467018)
  val bolognaCampusLocation: GPSLocation = GPSLocation(44.487912, 11.32885)

object TimeUtils:
  import java.util.Date

  def now: Date = Date()
  def inTheFuture: Date = Date.from(now.toInstant.plusSeconds(60))
  def inThePast: Date = Date.from(now.toInstant.minusSeconds(60))
