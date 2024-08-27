package io.github.positionpal.location.infrastructure.reactions

import java.util.Date

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.positionpal.location.application.reactions.TrackingEventReaction.{Continue, Notification}
import io.github.positionpal.location.application.reactions.{
  ArrivalCheck,
  ArrivalTimeoutCheck,
  StationaryCheck,
  TrackingEventReaction,
}
import io.github.positionpal.location.commons.EnvVariablesProvider
import io.github.positionpal.location.domain.*
import io.github.positionpal.location.domain.DrivingEvents.*
import io.github.positionpal.location.infrastructure.geo.Configuration
import io.github.positionpal.location.infrastructure.utils.*
import io.github.positionpal.location.infrastructure.utils.MapboxServiceAdapterUtils.*
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class TrackingEventReactionsTest extends AnyFunSpec with Matchers:

  describe("TrackingEventReactions"):
    describe("should `Continue`"):
      it("if no check is met"):
        val route = Route(StartRoutingEvent(now, UserId("test"), RoutingMode.Driving, cesenaCampus), inTheFuture)
        val event: TrackingEvent = TrackingEvent(now, UserId("test"), bolognaCampus)
        checksFor(route, event).unsafeRunSync() should matchPattern { case Right(Right(Continue)) => }

    describe("should trigger a `Notification`"):
      it("if the user is stuck in the same position for too long"):
        val route = Route.withPositions(
          StartRoutingEvent(now, UserId("test"), RoutingMode.Driving, cesenaCampus),
          inTheFuture,
          List.fill(20)(bolognaCampus),
        )
        val event: TrackingEvent = TrackingEvent(Date(), UserId("test"), bolognaCampus)
        checksFor(route, event).unsafeRunSync() should matchPattern { case Right(Left(Notification.Alert(_))) => }

      it("if the user has not reached the destination within the expected time"):
        val route = Route(StartRoutingEvent(now, UserId("test"), RoutingMode.Driving, cesenaCampus), inThePast)
        val event: TrackingEvent = TrackingEvent(Date(), UserId("test"), bolognaCampus)
        checksFor(route, event).unsafeRunSync() should matchPattern { case Right(Left(Notification.Alert(_))) => }

      it("if the user has arrived to the expected destination in time"):
        val route = Route(StartRoutingEvent(now, UserId("test"), RoutingMode.Driving, cesenaCampus), inTheFuture)
        val event: TrackingEvent = TrackingEvent(Date(), UserId("test"), cesenaCampus)
        checksFor(route, event).unsafeRunSync() should matchPattern { case Right(Left(Notification.Success(_))) => }

  private def checksFor(route: Route, event: TrackingEvent): IO[Either[Any, Either[Notification, Continue.type]]] =
    for
      envs <- EnvVariablesProvider[IO].configuration
      config <- clientResource.use(client => IO.pure(Configuration(client, envs("MAPBOX_API_KEY"))))
      composed = ArrivalCheck(mapboxServiceAdapter) >>> StationaryCheck() >>> ArrivalTimeoutCheck()
      result <- composed(route, event).value.run(config)
    yield result

  private def now: Date = Date()

  private def inTheFuture: Date = Date.from(now.toInstant.plusSeconds(60))

  private def inThePast: Date = Date.from(now.toInstant.minusSeconds(60))
