package io.gihub.positionpal.location.experiments.kafka

import io.gihub.positionpal.location.experiments.kafka.Entitities.GPSLocation
import org.apache.kafka.clients.admin.{AdminClient, NewTopic}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import scala.util.Random
import scala.jdk.CollectionConverters.*

@main def runMockedProducer(): Unit =
  MockedProducer.run()

object MockedProducer:

  private val producer: KafkaProducer[String, String] = KafkaProducer(Configurations.producerConfiguration.asJava)
  private val admin = AdminClient.create(Configurations.producerConfiguration.asJava)
  private val topics = EntitiesData.users.map(_.group)
    .map(g => NewTopic(Configurations.sampledLocationsTopic + g, 3, 1.toShort))
  private val result = admin.createTopics(topics.asJava)
  result.all().get()
  println("Topics created")

  def run(): Unit =
    while true do
      produceLocation()
      Thread.sleep(5_000)

  private def produceLocation(): Unit =
    Random.shuffle(EntitiesData.users).foreach: user =>
      val location = GPSLocation(Random.nextDouble(), Random.nextDouble())
      println("Producing location: " + location + " for user: " + user)
      producer.send(
        ProducerRecord[String, String](Configurations.sampledLocationsTopic + user.group, s"${user.id}", s"$location"),
        (metadata, exception) =>
          if exception != null then
            println(s"Failed to produce record: $exception")
          else
            println(s"Produced record: $metadata")
      )