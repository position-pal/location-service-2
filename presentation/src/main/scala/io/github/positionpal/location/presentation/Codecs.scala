package io.github.positionpal.location.presentation

import java.util.Date

import io.bullet.borer.derivation.ArrayBasedCodecs.{deriveAllCodecs, deriveCodec}
import io.bullet.borer.{Codec, Decoder, Encoder, Writer}
import io.github.positionpal.location.application.services.UserState
import io.github.positionpal.location.domain.{
  DrivingEvent,
  GPSLocation,
  Route,
  RoutingMode,
  StartRouting,
  Tracking,
  UserId,
}

/** Provides codecs for the domain model and application services. */
trait Codecs:
  given dateCoded: Codec[Date] = Codec.bimap[Long, Date](_.getTime, new Date(_))

  given userIdCodec: Codec[UserId] = deriveCodec[UserId]

  given userStateCodec: Codec[UserState] = deriveCodec[UserState]

  given gpsLocationCodec: Codec[GPSLocation] = deriveCodec[GPSLocation]

  given routingModeCodec: Codec[RoutingMode] = deriveCodec[RoutingMode]

  given drivingEventCodec: Codec[DrivingEvent] = deriveAllCodecs[DrivingEvent]

  given startRoutingCodec: Codec[StartRouting] = deriveCodec[StartRouting]

  given trackingRouting: Codec[Tracking] = deriveCodec[Tracking]

  given routeEncoder: Encoder[Route] = Encoder: (writer, route) =>
    writer.writeMapHeader(2).writeString("sourceEvent").write(route.sourceEvent).writeString("positions")
      .write(route.positions)

  given routeDecoder: Decoder[Route] = Decoder: reader =>
    reader.readMapHeader()
    var sourceEvent: Option[StartRouting] = None
    var positions: List[GPSLocation] = Nil
    for _ <- 0 until 2 do
      reader.readString() match
        case "sourceEvent" => sourceEvent = Some(reader.read[StartRouting]())
        case "positions" => positions = reader.read[List[GPSLocation]]()
        case _ => ???
    Route.withPositions(sourceEvent.get, positions)
