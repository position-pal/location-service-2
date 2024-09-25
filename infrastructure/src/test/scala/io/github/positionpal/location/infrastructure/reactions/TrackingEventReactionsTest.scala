package io.github.positionpal.location.infrastructure.reactions

import java.util.Date

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.positionpal.location.application.reactions.*
import io.github.positionpal.location.application.reactions.TrackingEventReaction.{Continue, Notification}
import io.github.positionpal.location.commons.*
import io.github.positionpal.location.domain.*
import io.github.positionpal.location.domain.DrivingEvents.*
import io.github.positionpal.location.infrastructure.geo.MapboxService
import io.github.positionpal.location.infrastructure.utils.*
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class TrackingEventReactionsTest extends AnyFunSpec with Matchers:

  describe("TrackingEventReactions"):
    describe("should `Continue`"):
      it("if no check is met"):
        val route = Route(StartRouting(now, UserId("test"), RoutingMode.Driving, cesenaCampus), inTheFuture)
        val event: Tracking = Tracking(now, UserId("test"), bolognaCampus)
        checksFor(route, event).unsafeRunSync() should matchPattern { case Right(Right(Continue)) => }

    describe("should trigger a `Notification`"):
      it("if the user is stuck in the same position for too long"):
        val route = Route.withPositions(
          StartRouting(now, UserId("test"), RoutingMode.Driving, cesenaCampus),
          inTheFuture,
          List.fill(20)(bolognaCampus),
        )
        val event: Tracking = Tracking(Date(), UserId("test"), bolognaCampus)
        checksFor(route, event).unsafeRunSync() should matchPattern { case Right(Left(Notification.Alert(_))) => }

      it("if the user has not reached the destination within the expected time"):
        val route = Route(StartRouting(now, UserId("test"), RoutingMode.Driving, cesenaCampus), inThePast)
        val event: Tracking = Tracking(Date(), UserId("test"), bolognaCampus)
        checksFor(route, event).unsafeRunSync() should matchPattern { case Right(Left(Notification.Alert(_))) => }

      it("if the user has arrived to the expected destination in time"):
        val route = Route(StartRouting(now, UserId("test"), RoutingMode.Driving, cesenaCampus), inTheFuture)
        val event: Tracking = Tracking(Date(), UserId("test"), cesenaCampus)
        checksFor(route, event).unsafeRunSync() should matchPattern { case Right(Left(Notification.Success(_))) => }

  private def checksFor(route: Route, event: Tracking): IO[Either[Any, Either[Notification, Continue.type]]] =
    for
      envs <- EnvVariablesProvider[IO].configuration
      config <- HTTPUtils.clientRes.use(client => IO.pure(MapboxService.Configuration(client, envs("MAPBOX_API_KEY"))))
      check = ArrivalCheck(MapboxService()) >>> StationaryCheck() >>> ArrivalTimeoutCheck()
      result <- check(route, event).value.run(config)
    yield result

  private def now: Date = Date()

  private def inTheFuture: Date = Date.from(now.toInstant.plusSeconds(60))

  private def inThePast: Date = Date.from(now.toInstant.minusSeconds(60))
