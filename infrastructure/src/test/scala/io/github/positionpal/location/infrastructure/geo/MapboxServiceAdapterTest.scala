package io.github.positionpal.location.infrastructure.geo

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.positionpal.location.application.geo.Distance.DistanceUnit
import io.github.positionpal.location.application.geo.{Distance, MapsServiceError}
import io.github.positionpal.location.commons.EnvVariablesProvider
import io.github.positionpal.location.domain.RoutingMode
import io.github.positionpal.location.domain.RoutingMode.Driving
import io.github.positionpal.location.infrastructure.utils.*
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class MapboxServiceAdapterTest extends AnyFunSpec with Matchers:

  private val mapboxService = MapboxService.Adapter()

  describe("The Mapbox service adapter"):
    it("should calculate the distance between two locations"):
      val distanceRequest = for
        envs <- EnvVariablesProvider[IO].configuration
        config <- HTTPUtils.client.use(client => IO.pure(MapboxService.Configuration(client, envs("MAPBOX_API_KEY"))))
        distance <- mapboxService.distance(Driving)(cesenaCampus, bolognaCampus).value.run(config)
      yield distance
      val result = distanceRequest.unsafeRunSync()
      result.isRight shouldBe true
      result.map(_.value should (be >= 80_000.0 and be <= 100_000.0))
      result.map(_.unit shouldBe DistanceUnit.Meters)

    it("should calculate the arrival time between two locations"):
      val arrivalTimeRequest = for
        envs <- EnvVariablesProvider[IO].configuration
        config <- HTTPUtils.client.use(client => IO.pure(MapboxService.Configuration(client, envs("MAPBOX_API_KEY"))))
        arrivalTime <- mapboxService.duration(Driving)(cesenaCampus, bolognaCampus).value.run(config)
      yield arrivalTime
      val result = arrivalTimeRequest.unsafeRunSync()
      result.isRight shouldBe true
      result.map(_.toMinutes should (be >= 60L))
