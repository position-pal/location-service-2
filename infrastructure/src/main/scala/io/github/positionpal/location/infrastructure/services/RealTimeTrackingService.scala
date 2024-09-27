package io.github.positionpal.location.infrastructure.services

import java.util.Date

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import cats.data.ReaderT
import cats.effect.{IO, Resource}
import com.typesafe.config.Config
import io.github.positionpal.location.application.services.RealTimeTrackingService
import io.github.positionpal.location.domain.DrivingEvents.Tracking
import io.github.positionpal.location.domain.{DrivingEvents, GPSLocation, UserId}
import io.github.positionpal.location.infrastructure.utils.AkkaUtils

object RealTimeTrackingService:

  def apply(): ActorBasedRealTimeTrackingService = ActorBasedRealTimeTrackingService()

  class ActorBasedRealTimeTrackingService extends RealTimeTrackingService[IO]:

    val cluster: ReaderT[[A] =>> Resource[IO, A], Config, ActorSystem[DrivingEvents]] =
      ReaderT: config =>
        for
          system <- AkkaUtils.startup(config)(Behaviors.empty)
          cluster <- Resource.eval(IO(ClusterSharding(system)))
          _ <- Resource.eval(IO(cluster.init(RealTimeUserTracker())))
        yield system

    override def handle[Unit](event: DrivingEvents): IO[Unit] = ???

@main def testRealTimeTrackingService(): Unit =
  import com.typesafe.config.ConfigFactory
  import cats.effect.unsafe.implicits.global
  import scala.concurrent.duration.DurationInt
  val service = RealTimeTrackingService()
  val cluster: Resource[IO, ActorSystem[DrivingEvents]] = service.cluster.run(ConfigFactory.load("akka.conf"))
  cluster.use: system =>
    IO.println(s"System: $system")
      *> IO.println(s"Cluster Sharding: ${ClusterSharding(system)}")
      *> IO(
        ClusterSharding(system).entityRefFor(RealTimeUserTracker.typeKey, "1") ! Tracking(
          Date(),
          UserId("1"),
          GPSLocation(0.0, 0.0),
        ),
      )
      *> IO.sleep(10.seconds)
      *> IO(system.terminate())
  .unsafeRunSync()
