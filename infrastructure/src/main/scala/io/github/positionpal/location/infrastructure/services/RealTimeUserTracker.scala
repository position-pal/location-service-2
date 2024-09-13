package io.github.positionpal.location.infrastructure.services

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.Cluster
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{Entity, EntityTypeKey}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import io.github.positionpal.location.domain
import io.github.positionpal.location.domain.DrivingEvents

object RealTimeUserTracker:

  /** Uniquely identifies the types of this entity instances (actors) that will be managed by cluster sharding. */
  private val typeKey: EntityTypeKey[DrivingEvents] = EntityTypeKey[DrivingEvents]("RealTimeUserTracker")

  // private type State = (Option[Route], Option[TrackingEvent])

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
      EventSourcedBehavior(persistenceId, emptyState = (None, None), commandHandler = ???, eventHandler = ???)
