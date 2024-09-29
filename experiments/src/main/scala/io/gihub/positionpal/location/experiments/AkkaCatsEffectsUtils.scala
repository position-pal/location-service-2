package io.gihub.positionpal.location.experiments

import akka.actor.BootstrapSetup
import akka.actor.typed.{ActorSystem, Behavior}
import cats.effect.{IO, Resource}
import com.typesafe.config.ConfigFactory
import io.gihub.positionpal.location.experiments.NopeActor.Message

def startup[T](behavior: => Behavior[T]): Resource[IO, ActorSystem[T]] =
  Resource:
    for
      ec <- IO.executionContext
      config = BootstrapSetup(ConfigFactory.load("application")).withDefaultExecutionContext(ec)
      _ <- IO(println(s">> execution context: ${config.defaultExecutionContext}"))
      system <- IO(ActorSystem(behavior, "ClusterSystem", config))
      cancel = IO.fromFuture(IO(system.whenTerminated)).void
    yield (system, cancel)

/** Just to have a reference of the dispatchers used by Akka.
  * Use this to compare with the cats effects dispatcher with a monitoring tool.
  */
@main def simpleWithoutIO(): Unit =
  val actorSystem = ActorSystem(NopeActor(), "ClusterSystem", ConfigFactory.load("application"))
  actorSystem ! Message("Hello")
