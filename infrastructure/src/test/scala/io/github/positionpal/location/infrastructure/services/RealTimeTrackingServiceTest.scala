package io.github.positionpal.location.infrastructure.services

import cats.effect.IO
import com.typesafe.config.ConfigFactory
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class RealTimeTrackingServiceTest extends AnyFunSpec with Matchers:

  val realTimeTrackingService = RealTimeTrackingService()

  import cats.effect.unsafe.implicits.global

  describe("RealTimeTrackingService"):
    ignore("should be able to get created"):
      realTimeTrackingService.cluster.run(ConfigFactory.load("akka-cluster")).use: _ =>
        IO.unit
      .unsafeRunSync()
