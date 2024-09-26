package io.gihub.positionpal.location.experiments

import scala.concurrent.duration.DurationInt
import scala.util.{Random, Success}

import akka.actor.BootstrapSetup
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import cats.effect.{IO, Resource}
import com.typesafe.config.ConfigFactory
import io.gihub.positionpal.location.experiments.NopeActor.Message

@main def testWithoutIO(): Unit =
  val actorSystem = ActorSystem(NopeActor(), "ClusterSystem", ConfigFactory.load("application"))
  actorSystem ! Message("Hello")

@main def testActorSystemStartup(): Unit =
  import cats.effect.unsafe.implicits.global
  val actorSystemRes = startup(NopeActor())
  actorSystemRes.use { actorSystem =>
    IO(actorSystem.log.info("Hello from cats effects dispatcher"))
      *> IO(actorSystem ! Message("Hello World"))
      *> IO.sleep(5.seconds)
      *> IO(actorSystem ! Message("Another message"))
      *> IO.sleep(5.seconds)
      *> IO(actorSystem.terminate())
  }.unsafeRunSync()

def startup[T](behavior: => Behavior[T]): Resource[IO, ActorSystem[T]] =
  Resource:
    for
      ec <- IO.executionContext
      config = BootstrapSetup(ConfigFactory.load("application")).withDefaultExecutionContext(ec)
      _ <- IO(println(s">> execution context: ${config.defaultExecutionContext}"))
      system <- IO(ActorSystem(behavior, "ClusterSystem", config))
      cancel = IO.fromFuture(IO(system.whenTerminated)).void
    yield (system, cancel)

object NopeActor:

  sealed trait Command
  case class Message(message: String) extends Command
  private case class Result(newState: Int) extends Command

  import cats.effect.unsafe.implicits.global

  def apply(myState: Int = 0): Behavior[Command] =
    Behaviors.setup: context =>
      context.log.info("NopeActor started")
      Behaviors.receiveMessage:
        case Message(message) =>
          context.log.info("Received message: {}. My current state is: {}", message, myState)
          context.log.info("Roll a dice asynchronously")
          context.pipeToSelf[Int](rollADice().unsafeToFuture()):
            case Success(value) => Result(value)
            case _ => Result(-1)
          context.log.info("Dice rolling started...")
          Behaviors.same
        case Result(value) =>
          context.log.info("Received rolled dice result: {}", value)
          NopeActor(value)

  private def rollADice(limit: Int = Int.MaxValue): IO[Int] =
    IO.sleep(3.seconds) *> IO(Random().nextInt(limit))
