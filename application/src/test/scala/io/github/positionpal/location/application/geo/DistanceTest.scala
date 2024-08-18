package io.github.positionpal.location.application.geo

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DistanceTest extends AnyFlatSpec with Matchers:

  import io.github.positionpal.location.application.geo.Distance.{meters, kilometers}

  "It" should "be possible to obtain a `Distance` object from `Double`s" in:
    val distance = 10.meters
    distance shouldBe Distance(10.0, Distance.DistanceUnit.Meters)

  "It" should "be possible to convert a `Distance` object to kilometers" in:
    val distance = 10.meters.toKilometers
    distance shouldBe Distance(0.01, Distance.DistanceUnit.Kilometers)

  "It" should "be possible to convert a `Distance` object to meters" in:
    val distance = 0.01.kilometers.toMeters
    distance shouldBe Distance(10.0, Distance.DistanceUnit.Meters)
