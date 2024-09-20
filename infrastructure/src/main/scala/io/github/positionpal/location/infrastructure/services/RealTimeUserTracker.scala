package io.github.positionpal.location.infrastructure.services

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.Cluster
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{Entity, EntityTypeKey}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import io.github.positionpal.location.application.services.UserState
import io.github.positionpal.location.application.services.UserState.*
import io.github.positionpal.location.domain.*
import io.github.positionpal.location.domain.DrivingEvents.*

object RealTimeUserTracker:

  /** Uniquely identifies the types of this entity instances (actors) that will be managed by cluster sharding. */
  private val typeKey: EntityTypeKey[DrivingEvents] = EntityTypeKey[DrivingEvents]("RealTimeUserTracker")

  private case class State(userState: UserState, route: Option[Route], trackingEvent: Option[TrackingEvent])

  /** Configure this actor to be managed by cluster sharding.
    * @return the [[Entity]] instance that will be managed by cluster sharding.
    */
  def apply(): Entity[DrivingEvents, ShardingEnvelope[DrivingEvents]] =
    Entity(typeKey)(createBehavior =
      entityCtx => mainBehavior(entityCtx.entityId, PersistenceId(entityCtx.entityTypeKey.name, entityCtx.entityId)),
    )

  private def mainBehavior(entityId: String, persistenceId: PersistenceId): Behavior[DrivingEvents] =
    Behaviors.setup: ctx =>
      ctx.log.debug("Starting RealTimeUserTracker::{}@{}", entityId, Cluster(ctx.system).selfMember.address)
      EventSourcedBehavior(persistenceId, emptyState = State(Inactive, None, None), commandHandler = ???, eventHandler = ???)

  /*
  private val commandHandler: (State, DrivingEvents) => Effect[DrivingEvents, State] = (state, command) =>
    command match
      case StartRoutingEvent(timestamp, user, mode, arrivalPosition) => ???
      case SOSAlertEvent(timestamp, user, position) => ???
      case ev @ TrackingEvent(_, _, _) => Effect.persist(ev)

  private val eventHandler: (State, DrivingEvents) => State = (state, event) => ???

  private val trackingHandler: (State, TrackingEvent) => Effect[DrivingEvents, State] = (state, event) =>
    Effect.persist(event).thenRun {
      case State(Routing, route, _) =>
        // TODO: perform checks and based on the result
      case State(SOS, _, _) => ???
      case _ => ()
    }


  private val inactiveMode: (State, DrivingEvents) => Effect[DrivingEvents, State] = ???

  private val sosMode: (State, DrivingEvents) => Effect[DrivingEvents, State] = ???

  private val routingMode: (State, DrivingEvents) => Effect[DrivingEvents, State] = ???

  private def sendNotification(user: User, message: String): Unit =
    // TODO: Implement this proxy method to send notifications to user
    ???
   */
