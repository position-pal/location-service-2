package io.github.positionpal.location.infrastructure.services

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import com.typesafe.config.{Config, ConfigFactory}
import io.github.positionpal.location.domain.{RoutingMode, StartRouting, Tracking, UserId}
import io.github.positionpal.location.infrastructure.GeoUtils.*
import io.github.positionpal.location.infrastructure.TimeUtils.*
import io.github.positionpal.location.infrastructure.services.RealTimeUserTracker.*
import org.scalatest.BeforeAndAfterEach
import org.scalatest.wordspec.AnyWordSpecLike

class RealTimeUserTrackerTest
    extends ScalaTestWithActorTestKit(RealTimeUserTrackerTest.config)
    with AnyWordSpecLike
    with BeforeAndAfterEach:

  private val eventSourcedTestKit =
    EventSourcedBehaviorTestKit[RealTimeUserTracker.Command, RealTimeUserTracker.Event, RealTimeUserTracker.State](
      system,
      RealTimeUserTracker("testUser"),
    )

  override protected def beforeEach(): Unit =
    super.beforeEach()
    eventSourcedTestKit.clear()

  "RealTimeUserTracker" should:
    "be initialized with an inactive state and empty tracking" in:
      eventSourcedTestKit.getState() shouldBe Inactive()

    "accept as commands tracking events" in:
      eventSourcedTestKit
        .runCommand(StartRouting(now, UserId("1"), RoutingMode.Driving, cesenaCampusLocation, inTheFuture))

    "save the last tracking event and change its user state if a tracking event is received" in:
      val trackingEvent = Tracking(now, UserId("1"), cesenaCampusLocation)
      eventSourcedTestKit.runCommand(trackingEvent).events should contain only trackingEvent
      eventSourcedTestKit.getState() shouldBe Active(trackingEvent)

    "update the routing information if a routing is started" in:
      val startRoutingEvent = StartRouting(now, UserId("1"), RoutingMode.Driving, cesenaCampusLocation, inTheFuture)
      eventSourcedTestKit.runCommand(startRoutingEvent).events should contain only startRoutingEvent
      eventSourcedTestKit.getState() shouldBe Routing(startRoutingEvent, List.empty)

object RealTimeUserTrackerTest:
  val config: Config = ConfigFactory.parseString("""
      akka.actor.provider = "cluster"
      akka.remote.artery.canonical {
        hostname = "127.0.0.1"
        port = 0
      }
      akka.actor {
        serializers {
          jackson-cbor = "akka.serialization.jackson.JacksonCborSerializer"
        }
        serialization-bindings {
          "io.github.positionpal.location.domain.DrivingEvent" = jackson-cbor
          "io.github.positionpal.location.infrastructure.services.RealTimeUserTracker$State" = jackson-cbor
        }
      }
    """).withFallback(EventSourcedBehaviorTestKit.config).resolve()
