package io.github.positionpal.location.infrastructure.services

import java.util.concurrent.Callable

import akka.actor.ActorSystem
import akka.actor.typed.javadsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity}
import akka.persistence.typed.PersistenceId
import cats.effect.Async
import io.github.positionpal.location.application.services.RealTimeTrackingService
import io.github.positionpal.location.commons.CanRaise
import io.github.positionpal.location.domain.DrivingEvents
import io.github.positionpal.location.infrastructure.utils.startup

object RealTimeTrackingService:

  def apply[M[_]: Async: CanRaise[Error]](): RealTimeTrackingService[M] & Callable[M[ActorSystem]] =
    ActorBasedRealTimeTrackingService[M]()

  type Error = String

  private class ActorBasedRealTimeTrackingService[M[_]: Async: CanRaise[Error]]
      extends RealTimeTrackingService[M]
      with Callable[M[ActorSystem]]:

    override def call(): M[ActorSystem] =
      // Sets up the necessary infrastructure for cluster sharding.
      val sharding: ClusterSharding = ClusterSharding(startup()(Behaviors.empty))
      sharding.init(
        Entity(RealTimeUserTracker.TypeKey)(createBehavior =
          entityContext =>
            RealTimeUserTracker(
              entityContext.entityId,
              PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId),
            ),
        ),
      )
      ???

    override def handle(event: DrivingEvents): M[Unit] = ???
