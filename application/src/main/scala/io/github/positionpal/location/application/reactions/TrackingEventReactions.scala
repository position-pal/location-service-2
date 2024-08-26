package io.github.positionpal.location.application.reactions

import cats.Monad
import io.github.positionpal.location.domain.{DrivingEvents, Route}

/** A reaction to [[DrivingEvents.TrackingEvent]]s. */
object TrackingEventReaction extends BinaryShortCircuitReaction:
  case object Continue
  final case class TriggerNotification(reason: String)

  override type Environment = Route
  override type Event = DrivingEvents.TrackingEvent
  override type LeftOutcome = TriggerNotification
  override type RightOutcome = Continue.type

/** A [[TrackingEventReaction]] checking if the position curried by the event is near the arrival position. */
object IsArrivedCheck:
  import cats.implicits.toFunctorOps
  import io.github.positionpal.location.application.geo.Distance.*
  import io.github.positionpal.location.application.geo.MapsService
  import TrackingEventReaction.*

  private val arrivalThreshold = 50.meters

  def apply[M[_]: Monad](mapsService: MapsService[M]): EventReaction[M] =
    on[M]: (route, event) =>
      for
        distance <- mapsService.distance(route.sourceEvent.mode)(event.position, route.sourceEvent.arrivalPosition)
        outcome =
          if distance.toMeters.value <= arrivalThreshold.toMeters.value
          then Left(TriggerNotification("Arrived"))
          else Right(Continue)
      yield outcome

object IsContinuallyInSameLocationCheck:
  import TrackingEventReaction.*
  import cats.Monad

  private val samplingWindow = 5

  def apply[M[_]: Monad](): EventReaction[M] = on[M]: (route, event) =>
    if route.positions.take(samplingWindow).forall(_ == event.position)
    then Monad[M].pure(Left(TriggerNotification("Stuck")))
    else Monad[M].pure(Right(Continue))
