package io.github.positionpal.location.infrastructure.reactions

import java.util.Date
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.positionpal.location.application.reactions.TrackingEventReaction.{Continue, Notification}
import io.github.positionpal.location.application.reactions.{IsArrivalTimeExpiredCheck, IsArrivedCheck, IsContinuallyInSameLocationCheck, TrackingEventReaction}
import io.github.positionpal.location.commons.EnvVariablesProvider
import io.github.positionpal.location.domain.*
import io.github.positionpal.location.domain.DrivingEvents.*
import io.github.positionpal.location.infrastructure.geo.{Configuration, MapboxServiceAdapter, Response}
import org.http4s.ember.client.EmberClientBuilder
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

class TrackingEventReactionsTest extends AnyFunSpec with Matchers:

  private val cesenaCampus = GPSLocation(44.1476299926484, 12.2357184467018)
  private val bolognaCampus = GPSLocation(44.487912, 11.32885)
  private val mapboxServiceAdapter = MapboxServiceAdapter()

  private given LoggerFactory[IO] = Slf4jFactory.create[IO]

  private val clientResource = EmberClientBuilder.default[IO].build

  describe("TrackingEventReactions"):
    describe("should `Continue`"):
      it("if no check is met"):
        val route = Route(
          DrivingEvents.StartRoutingEvent(Date(), UserId("test"), RoutingMode.Driving, cesenaCampus),
          Date.from(Date().toInstant.plusSeconds(60)),
        )
        val event: TrackingEvent = TrackingEvent(Date(), UserId("test"), bolognaCampus)
        checksFor(route, event).unsafeRunSync() should matchPattern { case Right(Right(Continue)) => }

    describe("should trigger a `Notification`"):
      it("if the user is stuck in the same position for too long"):
        val route = Route.withPositions(
          DrivingEvents.StartRoutingEvent(Date(), UserId("test"), RoutingMode.Driving, cesenaCampus),
          Date(),
          List.fill(20)(bolognaCampus)
        )
        val event: TrackingEvent = TrackingEvent(Date(), UserId("test"), bolognaCampus)
        checksFor(route, event).unsafeRunSync() should matchPattern { case Right(Left(Notification.Alert(_))) => }

      it("if the user has not reached the destination within the expected time"):
        val route = Route(
          DrivingEvents.StartRoutingEvent(Date(), UserId("test"), RoutingMode.Driving, cesenaCampus),
          Date.from(Date().toInstant.minusSeconds(60)),
        )
        val event: TrackingEvent = TrackingEvent(Date(), UserId("test"), bolognaCampus)
        checksFor(route, event).unsafeRunSync() should matchPattern { case Right(Left(Notification.Alert(_))) => }

      it("if the user has arrived to the expected destination in time"):
        val route = Route(
          DrivingEvents.StartRoutingEvent(Date(), UserId("test"), RoutingMode.Driving, cesenaCampus),
          Date.from(Date().toInstant.plusSeconds(60)),
        )
        val event: TrackingEvent = TrackingEvent(Date(), UserId("test"), cesenaCampus)
        checksFor(route, event).unsafeRunSync() should matchPattern { case Right(Left(Notification.Success(_))) => }

  def checksFor(route:Route, event: TrackingEvent): IO[Either[Any, Either[Notification, Continue.type]]] =
    for
      envs <- EnvVariablesProvider[IO].configuration
      config <- clientResource.use(client => IO.pure(Configuration(client, envs("MAPBOX_API_KEY"))))
      check1 = IsArrivedCheck[Response](mapboxServiceAdapter)
      check2 = IsContinuallyInSameLocationCheck[Response]()
      check3 = IsArrivalTimeExpiredCheck[Response]()
      composed = check1 >>> check2 >>> check3
      result <- composed(route, event).value.run(config)
    yield result
