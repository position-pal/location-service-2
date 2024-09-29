package io.github.positionpal.location.application.storage

import io.github.positionpal.location.domain.{Tracking, UserId}

/** The reading model projection for [[Tracking]]s.
  * It encapsulates the read-side operations for querying and retrieving
  * [[Tracking]]s from the underlying store.
  */
trait TrackingEventsReader[M[_]]:

  /** @return the last [[Tracking]] of the given [[user]]. */
  def lastOf(user: UserId): M[Option[Tracking]]

/** The writing model projection for [[Tracking]]s.
  * It encapsulates the write-side operations for saving [[Tracking]]s
  * to the underlying store.
  */
trait TrackingEventsWriter[M[_]]:

  /** Save the given [[trackingEvent]] in the store. */
  def save[U](trackingEvent: Tracking): M[U]

/** A store for [[Tracking]]s, supporting both read and write operations. */
trait TrackingEventsStore[M[_]] extends TrackingEventsReader[M] with TrackingEventsWriter[M]
