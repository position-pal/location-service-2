package io.gihub.positionpal.location.experiments.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import cats.effect.IO

import scala.concurrent.duration.DurationInt
import scala.util.{Random, Success}

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

@main def nopeMain(): Unit =
  import cats.effect.unsafe.implicits.global
  val actorSystemRes = startup(NopeActor())
  actorSystemRes.use { actorSystem =>
    IO(actorSystem.log.info("Hello from cats effects dispatcher"))
      *> IO(actorSystem ! NopeActor.Message("Hello World"))
      *> IO.sleep(5.seconds)
      *> IO(actorSystem ! NopeActor.Message("Another message"))
      *> IO.sleep(5.seconds)
      *> IO(actorSystem.terminate())
  }.unsafeRunSync()
