package io.github.positionpal.location.infrastructure.geo

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.github.positionpal.location.application.geo.Distance.DistanceUnit
import io.github.positionpal.location.application.geo.{Distance, MapsServiceError}
import io.github.positionpal.location.commons.EnvVariablesProvider
import io.github.positionpal.location.domain.RoutingMode
import io.github.positionpal.location.domain.RoutingMode.Driving
import io.github.positionpal.location.infrastructure.utils.*
import io.github.positionpal.location.infrastructure.utils.MapboxServiceAdapterUtils.*
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class MapboxServiceAdapterTest extends AnyFunSpec with Matchers:

  describe("The Mapbox service adapter"):
    it("should calculate the distance between two locations"):
      val distanceRequest = for
        envs <- EnvVariablesProvider[IO].configuration
        config <- clientResource.use(client => IO.pure(Configuration(client, envs("MAPBOX_API_KEY"))))
        distance <- mapboxServiceAdapter.distance(Driving)(cesenaCampus, bolognaCampus).value.run(config)
      yield distance
      val result = distanceRequest.unsafeRunSync()
      result.isRight shouldBe true
      result.map(_.value should (be >= 90273.0 and be <= 90274.0))
      result.map(_.unit shouldBe DistanceUnit.Meters)

    it("should calculate the arrival time between two locations"):
      val arrivalTimeRequest = for
        envs <- EnvVariablesProvider[IO].configuration
        config <- clientResource.use(client => IO.pure(Configuration(client, envs("MAPBOX_API_KEY"))))
        arrivalTime <- mapboxServiceAdapter.duration(Driving)(cesenaCampus, bolognaCampus).value.run(config)
      yield arrivalTime
      val result = arrivalTimeRequest.unsafeRunSync()
      result.isRight shouldBe true
      result.map(_.toMinutes should (be >= 60L))
