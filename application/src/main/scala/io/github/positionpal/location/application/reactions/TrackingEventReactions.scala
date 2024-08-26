package io.github.positionpal.location.application.reactions

import cats.Monad
import io.github.positionpal.location.domain.{DrivingEvents, Route}

/** A reaction to [[DrivingEvents.TrackingEvent]]s. */
object TrackingEventReaction extends BinaryShortCircuitReaction:
  case object Continue
  enum Notification(val reason: String):
    case Alert(override val reason: String) extends Notification(reason)
    case Success(override val reason: String) extends Notification(reason)

  override type Environment = Route
  override type Event = DrivingEvents.TrackingEvent
  override type LeftOutcome = Notification
  override type RightOutcome = Continue.type

/** A [[TrackingEventReaction]] checking if the position curried by the event is near the arrival position. */
object IsArrivedCheck:
  import TrackingEventReaction.*
  import Notification.Success
  import cats.implicits.toFunctorOps
  import io.github.positionpal.location.application.geo.Distance.*
  import io.github.positionpal.location.application.geo.MapsService

  private val distanceThreshold = 50.meters
  private val successMessage = "User has arrived to the expected destination in time!"

  def apply[M[_]: Monad](mapsService: MapsService[M]): EventReaction[M] =
    on[M]: (route, event) =>
      for
        distance <- mapsService.distance(route.sourceEvent.mode)(event.position, route.sourceEvent.arrivalPosition)
        outcome =
          if distance.toMeters.value <= distanceThreshold.toMeters.value
          then Left(Success(successMessage))
          else Right(Continue)
      yield outcome

/** A [[TrackingEventReaction]] checking if the position curried by the event is continually in the same location. */
object IsContinuallyInSameLocationCheck:
  import TrackingEventReaction.*
  import Notification.Alert
  import cats.Monad

  private val samplingWindow = 5
  private val alertMessage = "User is stuck in the same position for too long"

  def apply[M[_]: Monad](): EventReaction[M] = on[M]: (route, event) =>
    val samples = route.positions.take(samplingWindow)
    if samples.size >= samplingWindow && samples.forall(_ == event.position)
    then Monad[M].pure(Left(Alert(alertMessage)))
    else Monad[M].pure(Right(Continue))

/** A [[TrackingEventReaction]] checking if the expected arrival time has expired. */
object IsArrivalTimeExpiredCheck:
  import TrackingEventReaction.*
  import Notification.Alert

  private val alertMessage = "User has not yet reached the destination within the expected time"

  def apply[M[_]: Monad](): EventReaction[M] = on[M]: (route, event) =>
    if event.timestamp.after(route.expectedArrivalTime)
    then Monad[M].pure(Left(Alert(alertMessage)))
    else Monad[M].pure(Right(Continue))
