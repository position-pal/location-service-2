package io.github.positionpal.location.infrastructure.reactions

import java.util.Date

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.positionpal.location.application.reactions.*
import io.github.positionpal.location.application.reactions.TrackingEventReaction.{Continue, Notification}
import io.github.positionpal.location.commons.*
import io.github.positionpal.location.domain.*
import io.github.positionpal.location.infrastructure.GeoUtils.*
import io.github.positionpal.location.infrastructure.TimeUtils.*
import io.github.positionpal.location.infrastructure.geo.MapboxService
import io.github.positionpal.location.infrastructure.utils.*
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class TrackingEventReactionsTest extends AnyFunSpec with Matchers:

  describe("TrackingEventReactions"):
    describe("should `Continue`"):
      it("if no check is met"):
        val tracking = Tracking.withMonitoring(UserId("test"), RoutingMode.Driving, cesenaCampusLocation, inTheFuture)
        val event: SampledLocation = SampledLocation(now, UserId("test"), bolognaCampusLocation)
        checksFor(tracking, event).unsafeRunSync() should matchPattern { case Right(Right(Continue)) => }

    describe("should trigger a `Notification`"):
      it("if the user is stuck in the same position for too long"):
        val tracking = List.fill(20)(SampledLocation(now, UserId("test"), bolognaCampusLocation)).foldLeft(
          Tracking.withMonitoring(UserId("test"), RoutingMode.Driving, cesenaCampusLocation, inTheFuture),
        )((tracking, sample) => tracking.addSample(sample))
        val event: SampledLocation = SampledLocation(Date(), UserId("test"), bolognaCampusLocation)
        checksFor(tracking, event).unsafeRunSync() should matchPattern { case Right(Left(Notification.Alert(_))) => }

      it("if the user has not reached the destination within the expected time"):
        val tracking = Tracking.withMonitoring(UserId("test"), RoutingMode.Driving, cesenaCampusLocation, inThePast)
        val event: SampledLocation = SampledLocation(Date(), UserId("test"), bolognaCampusLocation)
        checksFor(tracking, event).unsafeRunSync() should matchPattern { case Right(Left(Notification.Alert(_))) => }

      it("if the user has arrived to the expected destination in time"):
        val tracking = Tracking.withMonitoring(UserId("test"), RoutingMode.Driving, cesenaCampusLocation, inTheFuture)
        val event: SampledLocation = SampledLocation(Date(), UserId("test"), cesenaCampusLocation)
        checksFor(tracking, event).unsafeRunSync() should matchPattern { case Right(Left(Notification.Success(_))) => }

  private def checksFor(tracking: MonitorableTracking, event: SampledLocation) =
    for
      envs <- EnvVariablesProvider[IO].configuration
      config <- HTTPUtils.clientRes.use(client => IO.pure(MapboxService.Configuration(client, envs("MAPBOX_API_KEY"))))
      check = ArrivalCheck(MapboxService()) >>> StationaryCheck() >>> ArrivalTimeoutCheck()
      result <- check(tracking, event).value.run(config)
    yield result
