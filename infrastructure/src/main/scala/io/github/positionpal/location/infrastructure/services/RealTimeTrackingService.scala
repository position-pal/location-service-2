package io.github.positionpal.location.infrastructure.services

import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import cats.data.ReaderT
import cats.effect.{IO, Resource}
import com.typesafe.config.Config
import io.github.positionpal.location.application.services.RealTimeTrackingService
import io.github.positionpal.location.domain.DrivingEvents
import io.github.positionpal.location.infrastructure.utils.AkkaUtils

object RealTimeTrackingService:

  def apply(): ActorBasedRealTimeTrackingService = ActorBasedRealTimeTrackingService()

  class ActorBasedRealTimeTrackingService extends RealTimeTrackingService[IO]:

    val cluster: ReaderT[[A] =>> Resource[IO, A], Config, ClusterSharding] =
      ReaderT: config =>
        for
          system <- AkkaUtils.startup(config)(Behaviors.empty)
          cluster <- Resource.eval(IO(ClusterSharding(system)))
          _ <- Resource.eval(IO(cluster.init(RealTimeUserTracker())))
        yield cluster

    override def handle[Unit](event: DrivingEvents): IO[Unit] = ???
