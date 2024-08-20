package io.github.positionpal.location.application.geo

import java.util.Date

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.positionpal.location.domain.DrivingEvents.TrackingEvent
import io.github.positionpal.location.domain.{DrivingEvents, GPSLocation, Route, UserId}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TrackingEventReactionTest extends AnyFlatSpec with Matchers:

  import TrackingEventReaction.*

  private val route = Route(DrivingEvents.StartRoutingEvent(Date(), UserId("test"), GPSLocation(0.0, 0.0)))
  private val event: TrackingEvent = TrackingEvent(Date(), UserId("test"), GPSLocation(0.1, 0.1))

  "It" should "be possible to chain two tracking event reactions" in:
    val reaction1 = on: (_, _) =>
      IO:
        Right(Continue)
    val reaction2 = on: (_, _) =>
      IO:
        Left(TriggerNotification("Test"))
    val composed = reaction1 >>> reaction2
    composed(route, event).unsafeRunSync() shouldBe Left(TriggerNotification("Test"))

  "When composed" should "use short circuit evaluation" in:
    var sentinels = List[String]()
    val reaction1 = on: (_, _) =>
      IO:
        sentinels = sentinels :+ "reaction1"
        Left(TriggerNotification("Test"))
    val reaction2 = on: (_, _) =>
      IO:
        sentinels = sentinels :+ "reaction2"
        Right(Continue)
    val composed = reaction1 >>> reaction2
    composed(route, event).unsafeRunSync() shouldBe Left(TriggerNotification("Test"))
    sentinels shouldBe List("reaction1")
