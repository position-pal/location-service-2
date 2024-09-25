package io.github.positionpal.location.infrastructure.services

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.Cluster
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{Entity, EntityTypeKey}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import io.github.positionpal.location.application.services.UserState
import io.github.positionpal.location.application.services.UserState.*
import io.github.positionpal.location.domain.*
import io.github.positionpal.location.domain.DrivingEvents.*

/*
 * Question: what about the order of reactions results?
 *  - The order of events in the journal must be preserved (otherwise routes will be inconsistent)
 *  - Order of reactions result is not important:
 *
 *                          Newest
 *    Tracking Event   Tracking Event
 *  --------|-------------|------o----*--------------------------------------> time
 *          |             |      Ʌ    Ʌ
 *          |-------------|------|----| delayed cause of the network or whatever!
 *                        |------|
 *
 * Outcome of the reaction: Continue, Success, Alert
 *
 * | Faster reaction | Delayed reaction | Outcome                                                 |
 * |-----------------|------------------|---------------------------------------------------------|
 * | Success         | Continue         | Route is terminated before hand                         |
 * | Alert           | Continue         | Route still active: sent notification before hand       |
 * | Continue        | Success          | No problem: "idempotent"                                |
 * | Continue        | Alert            | No problem: "idempotent"                                |
 * | Alert           | Success          | An alert is sent even if the route should be terminated | <= !!!
 * | Success         | Alert            | Route is terminated before triggering the alert: not a  |
 * |                 |                  | problem, it means it would have been terminated anyway  |
 */
object RealTimeUserTracker:

  /** Uniquely identifies the types of this entity instances (actors) that will be managed by cluster sharding. */
  private val typeKey: EntityTypeKey[DrivingEvents] = EntityTypeKey[DrivingEvents]("RealTimeUserTracker")

  private case class State(userState: UserState, route: Option[Route], trackingEvent: Option[Tracking])

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

  private val commandHandler: (State, DrivingEvents) => Effect[DrivingEvents, State] = (state, command) =>
    command match
      case StartRouting(timestamp, user, mode, arrivalPosition) => ???
      case SOSAlert(timestamp, user, position) => ???
      case ev @ Tracking(_, _, _) => ???

  private val eventHandler: (State, DrivingEvents) => State = (state, event) => ???

  private val trackingHandler: (State, Tracking) => Effect[DrivingEvents, State] = (state, event) =>
    state match
      case State(Routing, route, _) =>
        // Trigger the pipeline of reactions asynchronously (with pipeToSelf)
        // When the pipeline finishes its result is enqueued as a message to the actor
        //    if outcome is Continue      => Effect.none
        //    if outcome is Alert(msg)    => send notification => Effect.none
        //    if outcome is Success(msg)  => send notification => Effect.persist(StopRouting)
        //
        // Problem: how to avoid the flooding of notifications at each update of the position?
        Effect.persist(event)
      case _ =>
        // Either way the current state is SOS, Inactive or Active we persist the event in the journal
        //    Note: if state is 'Inactive' then the event handler updates it to Active
        //          if route is not 'None', then the route is updated with the new position
        Effect.persist(event)

  private val inactiveHandler: (State, DrivingEvents) => Effect[DrivingEvents, State] = ???

  private val sosHandler: (State, DrivingEvents) => Effect[DrivingEvents, State] = ???

  private val routingHandler: (State, DrivingEvents) => Effect[DrivingEvents, State] = ???
