package io.github.positionpal.location.infrastructure.utils

import akka.actor.BootstrapSetup
import akka.actor.typed.{ActorSystem, Behavior}
import cats.effect.{IO, Resource}
import com.typesafe.config.Config

object AkkaUtils:

  def startup[T](
      configuration: Config,
      name: String = "ClusterSystem",
  )(behavior: => Behavior[T]): Resource[IO, ActorSystem[T]] =
    Resource:
      for
        ec <- IO.executionContext
        config = BootstrapSetup(configuration).withDefaultExecutionContext(ec)
        system <- IO(ActorSystem(behavior, name, config))
        cancel = IO.fromFuture(IO(system.whenTerminated)).void
      yield (system, cancel)
