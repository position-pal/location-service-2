package io.github.positionpal.location.infrastructure.utils

import akka.actor.BootstrapSetup
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import cats.effect.{IO, Resource}
import com.typesafe.config.ConfigFactory

//def startup[T](fileName: String = "akka-cluster")(root: => Behavior[T]): ActorSystem[T] =
//  val config = ConfigFactory.load(fileName)
//  ActorSystem(root, "ClusterSystem", config)

def startup[T](behavior: => Behavior[T]): Resource[IO, ActorSystem[T]] =
  Resource:
    for
      ec <- IO.executionContext
      config = BootstrapSetup(ConfigFactory.load("akka-cluster")).withDefaultExecutionContext(ec)
      system <- IO(ActorSystem(behavior, "ClusterSystem", config))
      cancel = IO.fromFuture(IO(system.whenTerminated)).void
    yield (system, cancel)

def startupUntyped[T](behavior: Behavior[T]): Resource[IO, akka.actor.ActorSystem] =
  Resource:
    for
      ec <- IO.executionContext
      system <- IO(akka.actor.ActorSystem.apply("ClusterSystem", None, None, Some(ec)))
      cancel = IO.fromFuture(IO(system.whenTerminated)).void
    yield (system, cancel)

@main def testActorSystemStartup(): Unit =
  import cats.effect.unsafe.implicits.global
  println("hello world akka from cats effect")
  val actorSystemRes = startup(NopeActor())
  actorSystemRes.use { actorSystem =>
    IO(actorSystem ! "Hello")
  }.unsafeRunSync()

@main def testUntypedActorSystem(): Unit =
  import cats.effect.unsafe.implicits.global
  println("hello world akka from cats effect")
  val actorSystemRes = startupUntyped(NopeActor())
  actorSystemRes.use { actorSystem =>
    IO(println(actorSystem))
  }.unsafeRunSync()

@main def test2(): Unit =
  val actorSystem = ActorSystem(NopeActor(), "ClusterSystem", ConfigFactory.load("akka-cluster"))
  actorSystem ! "Hello"

object NopeActor:

  def apply(): Behavior[String] =
    Behaviors.setup { context =>
      context.log.info("NopeActor started")
      Behaviors.receiveMessage { message =>
        context.log.info("Received message: {}", message)
        Behaviors.same
      }
    }
