package io.github.positionpal.location.application.services

import io.github.positionpal.location
import io.github.positionpal.location.application.services
import io.github.positionpal.location.domain
import io.github.positionpal.location.domain.DrivingEvents

/** The real-time tracking service in charge of handling the [[DrivingEvents]]. */
trait RealTimeTrackingService[M[_]]:

  /** Handle the [[event]]. */
  def handle[U](event: DrivingEvents): M[U]

/*
object RealTimeTrackingService:

  def apply[M[_]: Async: CanAsk[Environment[M]]](): RealTimeTrackingService[M] =
    RealTimeTrackingServiceImpl[M]()

  case class Environment[M[_]](trackingEventsStore: TrackingEventsStore[M], userStateStore: UserStateStore[M])

  private class RealTimeTrackingServiceImpl[M[_]: Async: CanAsk[Environment[M]]] extends RealTimeTrackingService[M]:
    override def handle[U](event: DrivingEvents): M[U] = event match
      case StartRoutingEvent(timestamp, user, mode, arrivalPosition) => ???
      case SOSAlertEvent(timestamp, user, position) => ???
      case ev @ TrackingEvent(timestamp, user, position) => handle(ev)
    ???

//    override def handle[U](event: DrivingEvents): M[U] = event match
//      case StartRoutingEvent(timestamp, user, mode, arrivalPosition) => ???
//      case SOSAlertEvent(timestamp, user, position) => ???
//      case ev @ TrackingEvent(_, _, _) => handle(ev)

    private def handle[U](event: TrackingEvent): M[U] =
      for
        environment: Environment[M] <- summon[Ask[M, Environment[M]]].ask
        state <- environment.userStateStore.currentState(event.user)
        result = state match
          case Active | Inactive => ???
          case SOS | Routing => ???
      yield result
 */
