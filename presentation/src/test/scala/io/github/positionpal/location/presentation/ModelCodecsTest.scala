package io.github.positionpal.location.presentation

import java.util.Date

import io.bullet.borer.Cbor
import io.github.positionpal.location.domain.{
  GPSLocation,
  MonitorableTracking,
  RoutingMode,
  SampledLocation,
  Tracking,
  UserId,
}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ModelCodecsTest extends AnyFlatSpec with Matchers with ModelCodecs:

  "`Tracking`" should "be correctly serialized and deserialized" in:
    val user = UserId("test-user")
    val route = List(
      SampledLocation(Date(), user, GPSLocation(44.139, 12.243)),
      SampledLocation(Date(), user, GPSLocation(44.140, 12.244)),
    )
    val tracking = Tracking(user, route)
    val serialized = Cbor.encode(tracking).toByteArray
    val deserialized = Cbor.decode(serialized).to[Tracking].value
    deserialized.user shouldBe user
    deserialized.route shouldBe route

  "`MonitorableTracking`" should "be correctly serialized and deserialized" in:
    val user = UserId("test-user")
    val route = List(
      SampledLocation(Date(), user, GPSLocation(44.139, 12.243)),
      SampledLocation(Date(), user, GPSLocation(44.140, 12.244)),
    )
    val expectedArrival = Date()
    val destination = GPSLocation(44.141, 12.245)
    val tracking = Tracking.withMonitoring(user, RoutingMode.Driving, destination, expectedArrival, route)
    val serialized = Cbor.encode(tracking).toByteArray
    val deserialized = Cbor.decode(serialized).to[MonitorableTracking].value
    deserialized.user shouldBe user
    deserialized.route shouldBe route
    deserialized.destination shouldBe destination
    deserialized.mode shouldBe RoutingMode.Driving
    deserialized.expectedArrival shouldBe expectedArrival
