package io.github.positionpal.location.infrastructure.services

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import com.typesafe.config.{Config, ConfigFactory}
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
  private val trackingEvent = Tracking(now, user, cesenaCampusLocation)

  "RealTimeUserTracker" should:
    "be initialized with an inactive state and empty tracking" in:
      val state = eventSourcedTestKit.getState()
      state.userState shouldBe Inactive
      state.route shouldBe None
      state.lastSample shouldBe None

    "accept as commands tracking events" in:
      eventSourcedTestKit.runCommand(StartRouting(now, user, RoutingMode.Driving, cesenaCampusLocation, inTheFuture))

    "save the last tracking event and change its user state if a tracking event is received" in:
      eventSourcedTestKit.runCommand(trackingEvent).events should contain only trackingEvent
      val state = eventSourcedTestKit.getState()
      state.userState shouldBe Active
      state.route shouldBe None
      state.lastSample shouldBe Some(trackingEvent)

    "update the routing information if a routing is started" in:
      eventSourcedTestKit.runCommand(trackingEvent)
      val startRoutingEvent = StartRouting(now, user, RoutingMode.Driving, cesenaCampusLocation, inTheFuture)
      eventSourcedTestKit.runCommand(startRoutingEvent).events should contain only startRoutingEvent
      val state = eventSourcedTestKit.getState()
      state.userState shouldBe Routing
      state.route shouldBe Some(Route(startRoutingEvent))
      state.lastSample shouldBe Some(trackingEvent)

    "handle the tracking event on routing" in:
      eventSourcedTestKit.runCommand(StartRouting(now, user, RoutingMode.Driving, cesenaCampusLocation, now))
      Thread.sleep(1000)
      val result = eventSourcedTestKit.runCommand(trackingEvent)
      println(result.events)
      println(result.state)
      Thread.sleep(5_000)

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
