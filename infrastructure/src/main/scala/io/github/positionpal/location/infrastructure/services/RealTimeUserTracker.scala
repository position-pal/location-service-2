package io.github.positionpal.location.infrastructure.services

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.Cluster
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import io.github.positionpal.location.domain
import io.github.positionpal.location.domain.DrivingEvents

object RealTimeUserTracker:

  /** Uniquely identifies the types of this entity instances (actors) that will be managed by cluster sharding. */
  val TypeKey: EntityTypeKey[DrivingEvents] = EntityTypeKey[DrivingEvents]("RealTimeUserTracker")

  // private type State = (Option[Route], Option[TrackingEvent])

  def apply(entityId: String, persistenceId: PersistenceId): Behavior[DrivingEvents] =
    Behaviors.setup: ctx =>
      ctx.log.debug("Starting RealTimeUserTracker::{}@{}", entityId, Cluster(ctx.system).selfMember.address)
      EventSourcedBehavior(persistenceId, emptyState = (None, None), commandHandler = ???, eventHandler = ???)
