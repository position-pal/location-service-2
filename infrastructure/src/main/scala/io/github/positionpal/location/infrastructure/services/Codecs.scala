package io.github.positionpal.location.infrastructure.services

import io.bullet.borer.derivation.ArrayBasedCodecs.{deriveAllCodecs, deriveCodec}
import io.bullet.borer.{Codec, Decoder, Encoder, Writer}
import io.github.positionpal.location.application.services.UserState
import io.github.positionpal.location.domain.*

import java.util.Date

trait Codecs:
  given dateCoded: Codec[Date] = Codec.bimap[Long, Date](_.getTime, new Date(_))
  given userIdCodec: Codec[UserId] = deriveCodec[UserId]
  given userStateCodec: Codec[UserState] = deriveCodec[UserState]
  given gpsLocationCodec: Codec[GPSLocation] = deriveCodec[GPSLocation]
  given routingModeCodec: Codec[RoutingMode] = deriveCodec[RoutingMode]
  given drivingEventCodec: Codec[DrivingEvent] = deriveAllCodecs[DrivingEvent]
  given startRoutingCodec: Codec[StartRouting] = deriveCodec[StartRouting]
  given trackingRouting: Codec[Tracking] = deriveCodec[Tracking]
  given routeEncoder: Encoder[Route] = Encoder { (writer, route) =>
    writer.writeMapHeader(2)
      .writeString("sourceEvent").write(route.sourceEvent)
      .writeString("positions").write(route.positions)
  }
  given routeDecoder: Decoder[Route] = Decoder { reader =>
    reader.readMapHeader()
    var sourceEvent: StartRouting = null
    var positions: List[GPSLocation] = Nil
    for _ <- 0 until 2 do
      reader.readString() match {
        case "sourceEvent" => sourceEvent = reader.read[StartRouting]()
        case "positions" => positions = reader.read[List[GPSLocation]]()
        case other => ???
      }
    Route.withPositions(sourceEvent, positions)
  }
  // given myStateCodec: Codec[MyState] = deriveCodec[MyState]
end Codecs

//case class MyState(userState: UserState, route: Option[Route], sample: Option[Tracking]) extends Serializable
//
//class MyBorerAkkaSerializer extends CborAkkaSerializer[Serializable] with Codecs {
//
//  override def identifier: Int = 19923
//
//  register[MyState]()
//}
//
//@main def test(): Unit =
//  import cats.effect.unsafe.implicits.global
//  AkkaUtils.startup(ConfigFactory.load("akka.conf"))(Behaviors.empty)
//    .use: system =>
//      val serialization: Serialization = SerializationExtension(system)
//      val state = MyState(
//        UserState.Inactive,
//        Some(Route(StartRouting(Date(), UserId("1"), Driving, GPSLocation(1993843.545, 244.33), Date()))),
//        Some(Tracking(Date(), UserId("1"), GPSLocation(120.3, 18.45)))
//      )
//      IO.println(state)
//      *> IO.println(serialization.serialize(state))
//      *> IO.println(serialization.deserialize(serialization.serialize(state).get, classOf[MyState]))
//    .unsafeRunSync()
