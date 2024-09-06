package io.github.positionpal.location.application.services

import io.github.positionpal.location.domain.{GPSLocation, Route, UserId}

/** A service to query and retrieve the tracking information of a user. */
trait UserTrackingInfoService[M[_]]:
  def lastKnownLocation(userId: UserId): M[Option[GPSLocation]]
  def currentState(userId: UserId): M[UserState]
  def currentActiveRoute(userId: UserId): M[Option[Route]]
