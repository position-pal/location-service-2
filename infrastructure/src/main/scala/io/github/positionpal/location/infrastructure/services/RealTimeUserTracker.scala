package io.github.positionpal.location.infrastructure.services

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.cluster.Cluster
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{Entity, EntityTypeKey}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import cats.effect.IO
import io.github.positionpal.location.application.reactions.TrackingEventReaction.Notification
import io.github.positionpal.location.application.reactions.{ArrivalCheck, ArrivalTimeoutCheck, StationaryCheck}
import io.github.positionpal.location.application.services.UserState
import io.github.positionpal.location.application.services.UserState.*
import io.github.positionpal.location.domain.*
import io.github.positionpal.location.domain.DrivingEvents.*
import io.github.positionpal.location.infrastructure.geo.{MapboxConfigurationProvider, MapboxService}

object RealTimeUserTracker:

  /** Uniquely identifies the types of this entity instances (actors) that will be managed by cluster sharding. */
  val typeKey: EntityTypeKey[DrivingEvents] = EntityTypeKey[DrivingEvents]("RealTimeUserTracker")

  private case class State(
    userState: UserState = Inactive,
    route: Option[Route] = None,
    trackingEvent: Option[Tracking] = None,
  )

  /** Configure this actor to be managed by cluster sharding.
    * @return the [[Entity]] instance that will be managed by cluster sharding.
    */
  def apply(): Entity[DrivingEvents, ShardingEnvelope[DrivingEvents]] =
    Entity(typeKey): entityCtx =>
      mainBehavior(entityCtx.entityId, PersistenceId(entityCtx.entityTypeKey.name, entityCtx.entityId))

  private def mainBehavior(entityId: String, persistenceId: PersistenceId): Behavior[DrivingEvents] =
    Behaviors.setup: ctx =>
      ctx.log.debug("Starting RealTimeUserTracker::{}@{}", entityId, Cluster(ctx.system).selfMember.address)
      EventSourcedBehavior(persistenceId, emptyState = State(Inactive, None, None), commandHandler(ctx), eventHandler)

  private def commandHandler(ctx: ActorContext[DrivingEvents]): (State, DrivingEvents) => Effect[DrivingEvents, State] =
    (state, command) => command match
      case ev @ StartRouting(_, _, _, _) => routingHandler(state, ev)
      case ev @ Tracking(_, _, _) => trackingHandler(ctx)(state, ev)
      case _ => Effect.none

  private val eventHandler: (State, DrivingEvents) => State = (state, event) => State(Inactive, None, None)

  private val routingHandler: (State, StartRouting) => Effect[DrivingEvents, State] = (_, _) => ???

  private def trackingHandler(ctx: ActorContext[DrivingEvents]): (State, Tracking) => Effect[DrivingEvents, State] = (state, event) =>
    state match
      case State(Routing, Some(route), _) =>
        // ctx.pipeToSelf(reaction(route, event).unsafeToFuture()):
        reaction(route, event).unsafeToFuture().flatMap:
          case Right(_) => ()
          case Left(notification: Notification) => ???
          case Left(mapServiceError) => ???
        Effect.persist(event)
      case _ => Effect.persist(event)

  private def reaction(route: Route, event: Tracking) =
    for
      config <- MapboxConfigurationProvider("MAPBOX_API_KEY").configuration
      checks = ArrivalCheck(MapboxService()) >>> StationaryCheck() >>> ArrivalTimeoutCheck()
      result <- checks(route, event).value.run(config)
    yield result.flatten

  /*
  private val inactiveHandler: (State, DrivingEvents) => Effect[DrivingEvents, State] = ???

  private val sosHandler: (State, DrivingEvents) => Effect[DrivingEvents, State] = ???

  private val routingHandler: (State, DrivingEvents) => Effect[DrivingEvents, State] = ???
   */
