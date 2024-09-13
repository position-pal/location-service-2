package io.github.positionpal.location.infrastructure.services

import java.util.concurrent.Callable

import akka.actor.ActorSystem
import cats.effect.Async
import io.github.positionpal.location.application.services.RealTimeTrackingService
import io.github.positionpal.location.commons.CanRaise
import io.github.positionpal.location.domain.DrivingEvents

object RealTimeTrackingService:

  def apply[M[_]: Async: CanRaise[Error]](): RealTimeTrackingService[M] & Callable[M[ActorSystem]] =
    ActorBasedRealTimeTrackingService[M]()

  type Error = String

  private class ActorBasedRealTimeTrackingService[M[_]: Async: CanRaise[Error]]
      extends RealTimeTrackingService[M]
      with Callable[M[ActorSystem]]:

    override def call(): M[ActorSystem] =
      // val sharding: ClusterSharding = ClusterSharding(startup()(Behaviors.empty))
      // sharding.init(RealTimeUserTracker())
      ???

    override def handle(event: DrivingEvents): M[Unit] = ???
