package io.gihub.positionpal.location.experiments.actors

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityTypeKey}
import akka.cluster.typed.Cluster
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import akka.serialization.jackson.CborSerializable
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import io.gihub.positionpal.location.experiments.SerializableMessage

import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, Future}
import scala.util.Random

object User:

  trait Command extends SerializableMessage
  final case class Greet(whom: String)(val replyTo: ActorRef[Greeting]) extends Command
  final case class Greeting(whom: String, numberOfPeople: Int) extends Command

  private trait Event extends SerializableMessage
  final private case class Greeted(whom: String) extends Event

  final case class KnownPeople(names: Set[String]) extends CborSerializable:
    def add(name: String): KnownPeople = copy(names = names + name)
    def numberOfPeople: Int = names.size

  private def commandHandler(
      ctx: ActorContext[Command],
      entityId: String,
  ): (KnownPeople, Command) => Effect[Greeted, KnownPeople] =
    (_, cmd) =>
      cmd match
        case cmd: Greet =>
          println(s":::: Greeted ${cmd.whom} from $entityId actor @ ${Cluster(ctx.system).selfMember.address}")
          Effect.persist(Greeted(cmd.whom))
            .thenRun((state: KnownPeople) => cmd.replyTo ! Greeting(cmd.whom, state.numberOfPeople))

  private def eventHandler(ctx: ActorContext[Command]): (KnownPeople, Greeted) => KnownPeople =
    (state, evt) =>
      ctx.log.info("Updating state {}", state)
      state.add(evt.whom)

  val TypeKey: EntityTypeKey[Command] = EntityTypeKey[Command]("HelloWorld")

  def apply(entityId: String, persistenceId: PersistenceId): Behavior[Command] =
    Behaviors.setup: context =>
      println(s":::: Starting HelloWorld $entityId")
      EventSourcedBehavior(
        persistenceId,
        emptyState = KnownPeople(Set.empty),
        commandHandler(context, entityId),
        eventHandler(context),
      )
end User

class HelloWorldService(system: ActorSystem[?]):
  import system.executionContext

  private val sharding = ClusterSharding(system)

  // registration at startup
  sharding.init(Entity(typeKey = User.TypeKey) { entityContext =>
    User(entityContext.entityId, PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId))
  })

  implicit private val askTimeout: Timeout = Timeout(5.seconds)

  def greet(worldId: String, whom: String): Future[Int] =
    val entityRef = sharding.entityRefFor(User.TypeKey, worldId)
    val greeting = entityRef ? User.Greet(whom)
    greeting.map(_.numberOfPeople)

@main def helloWorldPersistence(): Unit =
  val actorSystem = ActorSystem(Behaviors.empty, "ClusterSystem", ConfigFactory.load("application"))
  val service = HelloWorldService(actorSystem)
  val result = service.greet("1", "Alice")
  Await.result(result, Duration.Inf)
  println(s"Number of people greeted: $result")

object PersistenceWithRelocation:

  private def config(port: Int) = ConfigFactory.parseString(s"""akka.remote.artery.canonical.port=$port""")
    .withFallback(ConfigFactory.load("application"))

  @main def node1(): Unit =
    val actorSystem = ActorSystem(Behaviors.empty, "ClusterSystem", config(2551))
    val service = HelloWorldService(actorSystem)
    for i <- 0 until 50 do
      println(s">>> Sending greeting to user$i")
      val result = service.greet(s"user${i % 5}", Random().nextString(4))
      Await.result(result, Duration.Inf)
      println(s">>> Number of people greeted: $result")
      Thread.sleep(5_000)

  @main def node2(): Unit =
    val actorSystem = ActorSystem(Behaviors.empty, "ClusterSystem", config(2552))
    HelloWorldService(actorSystem)
    Thread.sleep(Int.MaxValue)
