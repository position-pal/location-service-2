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

  "RealTimeUserTracker" should:
    "be initialized with an inactive state and empty tracking" in:
      val state = eventSourcedTestKit.getState()
      state.userState shouldBe Inactive
      state.route shouldBe None
      state.lastSample shouldBe None

    "accept as commands tracking events" in:
      eventSourcedTestKit
        .runCommand(StartRouting(now, UserId("1"), RoutingMode.Driving, cesenaCampusLocation, inTheFuture))

    "save the last tracking event and change its user state if a tracking event is received" in:
      val trackingEvent = Tracking(now, UserId("1"), cesenaCampusLocation)
      eventSourcedTestKit.runCommand(trackingEvent).events should contain only trackingEvent
      val state = eventSourcedTestKit.getState()
      state.userState shouldBe Active
      state.route shouldBe None
      state.lastSample shouldBe Some(trackingEvent)

    "update the routing information if a routing is started" in:
      val startRoutingEvent = StartRouting(now, UserId("1"), RoutingMode.Driving, cesenaCampusLocation, inTheFuture)
      eventSourcedTestKit.runCommand(startRoutingEvent).events should contain only startRoutingEvent
      val state = eventSourcedTestKit.getState()
      state.userState shouldBe Routing
      state.route shouldBe Some(Route(startRoutingEvent))
      state.lastSample shouldBe None

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
