package io.github.positionpal.location.infrastructure.services

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.cluster.Cluster
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{Entity, EntityTypeKey}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import io.github.positionpal.location.application.services.UserState.*
import io.github.positionpal.location.domain.*

object RealTimeUserTracker:

  /** Uniquely identifies the types of this entity instances (actors) that will be managed by cluster sharding. */
  val typeKey: EntityTypeKey[Command] = EntityTypeKey[Command]("RealTimeUserTracker")

  type Command = DrivingEvent
  type Event = DrivingEvent

  sealed trait State
  object State:
    def empty: State = Inactive()

  case class Inactive() extends State
  case class Active(lastTrace: Tracking) extends State
  case class Routing(startRouting: StartRouting, tracing: List[Tracking]) extends State

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

  private val eventHandler: (State, Event) => State = (state, event) =>
    event match
      case ev: StartRouting => Routing(ev, List.empty)
      case SOSAlert(timestamp, user, position) => ???
      case ev: Tracking => Active(ev)
      case StopSOS(timestamp, user) => ???
      case StopRouting(timestamp, user) => ???

  private def commandHandler(ctx: ActorContext[Command]): (State, Command) => Effect[Event, State] =
    (state, command) =>
      command match
        case ev: Tracking => trackingHandler(ctx)(state, ev)
        case ev: StartRouting => routingHandler(state, ev)
        case _ => Effect.none

  private val routingHandler: (State, StartRouting) => Effect[DrivingEvent, State] =
    (_, event) => Effect.persist(event)

  private def trackingHandler(ctx: ActorContext[DrivingEvent]): (State, Tracking) => Effect[DrivingEvent, State] =
    (state, event) =>
      ctx.log.info("Tracking")
      state match
        case _ => Effect.persist(event)

  /*
          case State(Routing, Some(route), _) =>
          ctx.log.info("Routing")
          // ctx.pipeToSelf(reaction(route, event).unsafeToFuture()):
          //        reaction(route, event).unsafeToFuture().flatMap:
          //          case Right(_) => ()
          //          case Left(notification: Notification) => ???
          //          case Left(mapServiceError) => ???
          // Effect.persist(event)
          ???

  private def reaction(route: Route, event: Tracking) =
    for
      config <- MapboxConfigurationProvider("MAPBOX_API_KEY").configuration
      checks = ArrivalCheck(MapboxService()) >>> StationaryCheck() >>> ArrivalTimeoutCheck()
      result <- checks(route, event).value.run(config)
    yield result.flatten
   */

  /*
  private val inactiveHandler: (State, DrivingEvents) => Effect[DrivingEvents, State] = ???

  private val sosHandler: (State, DrivingEvents) => Effect[DrivingEvents, State] = ???

  private val routingHandler: (State, DrivingEvents) => Effect[DrivingEvents, State] = ???
   */
