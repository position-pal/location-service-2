package io.github.positionpal.location.infrastructure.services

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import com.typesafe.config.{Config, ConfigFactory}
import io.github.positionpal.location.application.services.UserState
import io.github.positionpal.location.application.services.UserState.{Active, Inactive, Routing}
import io.github.positionpal.location.domain.*
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

  private val user = UserId("user-test")

  "RealTimeUserTracker" when:
    "initialized" should:
      "have an empty state" in:
        eventSourcedTestKit.getState() shouldMatch (Inactive, None, None)

      "accept tracking events" in:
        eventSourcedTestKit
          .runCommand(RoutingStarted(now, user, RoutingMode.Driving, cesenaCampusLocation, inTheFuture))

    "in active state" should:
      "update the last location sample" in:
        val tracking = Tracking(now, user, cesenaCampusLocation)
        eventSourcedTestKit.runCommand(tracking).events should contain only tracking
        eventSourcedTestKit.getState() shouldMatch (Active, None, Some(tracking))

      "transition to routing mode if a routing is started" in:
        val tracking = Tracking(now, user, cesenaCampusLocation)
        eventSourcedTestKit.runCommand(tracking)
        val startRoutingEvent = RoutingStarted(now, user, RoutingMode.Driving, cesenaCampusLocation, inTheFuture)
        eventSourcedTestKit.runCommand(startRoutingEvent).events should contain only startRoutingEvent
        eventSourcedTestKit.getState() shouldMatch (Routing, Some(Route(startRoutingEvent)), Some(tracking))

    "in routing state" should:
      "track the user position" in:
        val trace = Tracking(now, user, GPSLocation(44.139, 12.243))
          :: Tracking(now, user, GPSLocation(44.140, 12.244))
          :: Tracking(now, user, GPSLocation(44.141, 12.245))
          :: Nil
        eventSourcedTestKit
          .runCommand(RoutingStarted(now, user, RoutingMode.Driving, cesenaCampusLocation, inTheFuture))
        trace.foreach(eventSourcedTestKit.runCommand(_))
        println(eventSourcedTestKit.getState())
        Thread.sleep(5_000)

  extension (s: State)
    infix def shouldMatch(userState: UserState, route: Option[Route], lastSample: Option[Tracking]): Unit =
      s.userState shouldBe userState
      s.route shouldBe route
      s.lastSample shouldBe lastSample

//    "handle the tracking event on routing" in:
//      eventSourcedTestKit.runCommand(RoutingStarted(now, user, RoutingMode.Driving, cesenaCampusLocation, now))
//      Thread.sleep(1000)
//      val result = eventSourcedTestKit.runCommand(locationSampleAtCesena)
//      println(result.events)
//      println(result.state)
//      Thread.sleep(5_000)

object RealTimeUserTrackerTest:
  val config: Config = ConfigFactory.parseString("""
      akka.actor.provider = "cluster"
      akka.remote.artery.canonical {
        hostname = "127.0.0.1"
        port = 0
      }
      akka.actor {
        serializers {
          borer-json = "io.github.positionpal.location.infrastructure.services.BorerAkkaSerializer"
        }
        serialization-bindings {
          "io.github.positionpal.location.infrastructure.services.Serializable" = borer-json
          "io.github.positionpal.location.domain.DrivingEvent" = borer-json
        }
      }
    """).withFallback(EventSourcedBehaviorTestKit.config).resolve()
