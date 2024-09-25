package io.github.positionpal.location.application.reactions

import io.github.positionpal.location.domain.{DrivingEvents, Route}

/** A reaction to [[DrivingEvents.Tracking]]s. */
object TrackingEventReaction extends BinaryShortCircuitReaction:
  case object Continue
  enum Notification(val reason: String):
    case Alert(override val reason: String) extends Notification(reason)
    case Success(override val reason: String) extends Notification(reason)

  override type Environment = Route
  override type Event = DrivingEvents.Tracking
  override type LeftOutcome = Notification
  override type RightOutcome = Continue.type

import cats.Monad
import cats.effect.Sync
import cats.implicits.toFlatMapOps
import cats.implicits.toFunctorOps
import TrackingEventReaction.*
import Notification.*

/** A [[TrackingEventReaction]] checking if the position curried by the event is near the arrival position. */
object ArrivalCheck:
  import io.github.positionpal.location.application.geo.Distance.*
  import io.github.positionpal.location.application.geo.MapsService

  private val successMessage = "User has arrived at the expected destination on time!"

  def apply[M[_]: Sync: Monad](mapsService: MapsService[M]): EventReaction[M] =
    on[M]: (route, event) =>
      for
        config <- ReactionsConfiguration.get
        distance <- mapsService.distance(route.sourceEvent.mode)(event.position, route.sourceEvent.arrivalPosition)
        outcome =
          if distance.toMeters.value <= config.proximityToleranceMeters.meters.value
          then Left(Success(successMessage))
          else Right(Continue)
      yield outcome

/** A [[TrackingEventReaction]] checking if the position curried by the event is continually in the same location. */
object StationaryCheck:
  private val alertMessage = "The user has been stuck in the same position for a while."

  def apply[M[_]: Sync: Monad](): EventReaction[M] = on[M]: (route, event) =>
    for
      config <- ReactionsConfiguration.get
      samples = route.positions.take(config.stationarySamples)
      result <-
        if samples.size >= config.stationarySamples && samples.forall(_ == event.position)
        then Monad[M].pure(Left(Alert(alertMessage)))
        else Monad[M].pure(Right(Continue))
    yield result

/** A [[TrackingEventReaction]] checking if the expected arrival time has expired. */
object ArrivalTimeoutCheck:
  private val alertMessage = "User has not reached the destination within the expected time."

  def apply[M[_]: Monad](): EventReaction[M] = on[M]: (route, event) =>
    if event.timestamp.after(route.expectedArrivalTime)
    then Monad[M].pure(Left(Alert(alertMessage)))
    else Monad[M].pure(Right(Continue))
