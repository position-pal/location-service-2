package io.github.positionpal.location.application.storage

import io.github.positionpal.location.domain.{SampledLocation, UserId}

/** The reading model projection for [[SampledLocation]]s.
  * It encapsulates the read-side operations for querying and retrieving
  * [[SampledLocation]]s from the underlying store.
  */
trait TrackingEventsReader[M[_]]:

  /** @return the last [[SampledLocation]] of the given [[user]]. */
  def lastOf(user: UserId): M[Option[SampledLocation]]

/** The writing model projection for [[SampledLocation]]s.
  * It encapsulates the write-side operations for saving [[SampledLocation]]s
  * to the underlying store.
  */
trait TrackingEventsWriter[M[_]]:

  /** Save the given [[trackingEvent]] in the store. */
  def save[U](trackingEvent: SampledLocation): M[U]

/** A store for [[SampledLocation]]s, supporting both read and write operations. */
trait TrackingEventsStore[M[_]] extends TrackingEventsReader[M] with TrackingEventsWriter[M]
