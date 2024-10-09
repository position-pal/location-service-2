/* package io.gihub.positionpal.location.experiments.actors

package io.gihub.positionpal.location.experiments

import zio.actors.*
import zio.actors.Actor.Stateful
import zio.{UIO, ZIO}

object ZioActors extends App:

  sealed trait Command[+A]
  case class DoubleCommand(value: Int) extends Command[Int]

  val stateful = new Stateful[Any, Unit, Command]:
    override def receive[A](state: Unit, msg: Command[A], context: Context): UIO[(Unit, A)] =
      msg match
        case DoubleCommand(value) => ZIO.succeed(((), value * 2))

  for
    system <- ActorSystem("mySystem")
    actor <- system.make("myActor", Supervisor.none, (), stateful)
    doubled <- actor ! DoubleCommand(21)
  yield doubled
 */
