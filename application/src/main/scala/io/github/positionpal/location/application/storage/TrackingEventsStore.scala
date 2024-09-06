package io.github.positionpal.location.application.storage

import io.github.positionpal.location.domain.DrivingEvents.TrackingEvent
import io.github.positionpal.location.domain.UserId

/** The reading model projection for [[TrackingEvent]]s.
  * It encapsulates the read-side operations for querying and retrieving
  * [[TrackingEvent]]s from the underlying store.
  */
trait TrackingEventsReader[M[_]]:

  /** @return the last [[TrackingEvent]] of the given [[user]]. */
  def lastOf(user: UserId): M[Option[TrackingEvent]]

/** The writing model projection for [[TrackingEvent]]s.
  * It encapsulates the write-side operations for saving [[TrackingEvent]]s
  * to the underlying store.
  */
trait TrackingEventsWriter[M[_]]:

  /** Save the given [[trackingEvent]] in the store. */
  def save[U](trackingEvent: TrackingEvent): M[U]

/** A store for [[TrackingEvent]]s, supporting both read and write operations. */
trait TrackingEventsStore[M[_]] extends TrackingEventsReader[M] with TrackingEventsWriter[M]
