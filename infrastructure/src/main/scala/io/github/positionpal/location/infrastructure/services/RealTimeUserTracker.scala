package io.github.positionpal.location.infrastructure.services

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.cluster.Cluster
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{Entity, EntityTypeKey}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import io.github.positionpal.location.application.services.UserState
import io.github.positionpal.location.application.services.UserState.*
//import cats.effect.IO
//import io.github.positionpal.location.application.reactions.TrackingEventReaction.Notification
//import io.github.positionpal.location.application.reactions.{ArrivalCheck, ArrivalTimeoutCheck, StationaryCheck}
import io.github.positionpal.location.domain.*
//import io.github.positionpal.location.infrastructure.geo.{MapboxConfigurationProvider, MapboxService}

object RealTimeUserTracker:

  /** Uniquely identifies the types of this entity instances (actors) that will be managed by cluster sharding. */
  val typeKey: EntityTypeKey[Command] = EntityTypeKey[Command]("RealTimeUserTracker")

  type Command = DrivingEvent
  type Event = DrivingEvent

  final case class State(userState: UserState, route: Option[Route], trackingEvent: Option[Tracking])
  object State:
    def empty: State = State(Inactive, None, None)

  /** Configure this actor to be managed by cluster sharding.
    * @return the [[Entity]] instance that will be managed by cluster sharding.
    */
  def apply(): Entity[Command, ShardingEnvelope[Command]] =
    Entity(typeKey): entityCtx =>
      apply(entityCtx.entityId)

  def apply(entityId: String): Behavior[Command] =
    Behaviors.setup: ctx =>
      ctx.log.debug("Starting RealTimeUserTracker::{}@{}", entityId, Cluster(ctx.system).selfMember.address)
      EventSourcedBehavior(PersistenceId(typeKey.name, entityId), State.empty, commandHandler(ctx), eventHandler)

  private def commandHandler(ctx: ActorContext[Command]): (State, Command) => Effect[Event, State] =
    (state, command) => command match
      case _ =>
        ctx.log.info("Other")
        Effect.none

  private val eventHandler: (State, Event) => State = (state, event) => State.empty

  // private val routingHandler: (State, StartRouting) => Effect[DrivingEvents, State] = (_, _) => ???

/*  private def trackingHandler(ctx: ActorContext[DrivingEvents]): (State, Tracking) => Effect[DrivingEvents, State] = (state, event) =>
    state match
      case State(Routing, Some(route), _) =>
        // ctx.pipeToSelf(reaction(route, event).unsafeToFuture()):
        //        reaction(route, event).unsafeToFuture().flatMap:
        //          case Right(_) => ()
        //          case Left(notification: Notification) => ???
        //          case Left(mapServiceError) => ???
        Effect.persist(event)
      case _ => Effect.persist(event)

  private def reaction(route: Route, event: Tracking) =
    for
      config <- MapboxConfigurationProvider("MAPBOX_API_KEY").configuration
      checks = ArrivalCheck(MapboxService()) >>> StationaryCheck() >>> ArrivalTimeoutCheck()
      result <- checks(route, event).value.run(config)
    yield result.flatten*/

  /*
  private val inactiveHandler: (State, DrivingEvents) => Effect[DrivingEvents, State] = ???

  private val sosHandler: (State, DrivingEvents) => Effect[DrivingEvents, State] = ???

  private val routingHandler: (State, DrivingEvents) => Effect[DrivingEvents, State] = ???
   */
