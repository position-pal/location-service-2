package io.github.positionpal.location.infrastructure.services

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.cluster.Cluster
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{Entity, EntityTypeKey}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import io.bullet.borer.Codec
import io.bullet.borer.derivation.ArrayBasedCodecs.deriveCodec
import io.github.positionpal.location.application.services.UserState
import io.github.positionpal.location.application.services.UserState.*
import io.github.positionpal.location.domain.*

object RealTimeUserTracker:

  /** Uniquely identifies the types of this entity instances (actors) that will be managed by cluster sharding. */
  val typeKey: EntityTypeKey[Command] = EntityTypeKey[Command]("RealTimeUserTracker")

  type Command = DrivingEvent
  type Event = DrivingEvent

  final case class State(
    userState: UserState,
    route: Option[Route],
    lastSample: Option[Tracking]
  ) extends Serializable
  object State:
    def empty: State = State(UserState.Inactive, None, None)

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
      case ev: StartRouting => State(Routing, Some(Route(ev)), state.lastSample)
      case SOSAlert(timestamp, user, position) => ???
      case ev: Tracking => State(Active, None, Some(ev))
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

class BorerAkkaSerializer extends CborAkkaSerializer with Codecs:
  override def identifier: Int = 19923

  given stateCodec: Codec[RealTimeUserTracker.State] = deriveCodec[RealTimeUserTracker.State]

  register[DrivingEvent]()
  register[RealTimeUserTracker.State]()
