package io.github.positionpal.location.application.reactions

import io.github.positionpal.location.application.geo.RoutingMode.Driving
import io.github.positionpal.location.application.geo.{Distance, MapsService}
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
  import TrackingEventReaction.*
  import cats.effect.Async
  import cats.implicits.toFunctorOps
  import io.github.positionpal.location.application.geo.Distance.*

  private val arrivalThreshold = 50.meters

  def apply[M[_]: Async](mapsService: MapsService[M]): EventReaction[M] =
    on[M]: (route, event) =>
      for
        distance: Distance <- mapsService.distance(Driving)(event.position, route.event.arrivalPosition)
        outcome =
          if distance.toMeters.value <= arrivalThreshold.toMeters.value
          then Left(TriggerNotification("Arrived"))
          else Right(Continue)
      yield outcome

object IsContinuallyInSameLocationCheck:
  import TrackingEventReaction.*
  import cats.Monad
  import cats.effect.Async

  private val samplingWindow = 5

  def apply[M[_]: Async](): EventReaction[M] = on[M]: (route, event) =>
    if route.positions.take(samplingWindow).forall(_ == event.position)
    then Monad[M].pure(Left(TriggerNotification("Stuck")))
    else Monad[M].pure(Right(Continue))
