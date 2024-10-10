package io.github.positionpal.location.presentation

import java.util.Date

import io.bullet.borer.derivation.ArrayBasedCodecs.{deriveAllCodecs, deriveCodec}
import io.bullet.borer.{Codec, Decoder, Encoder, Writer}
import io.github.positionpal.location.application.services.UserState
import io.github.positionpal.location.domain.*

/** Provides codecs for the domain model and application services. */
trait ModelCodecs:
  given dateCoded: Codec[Date] = Codec.bimap[Long, Date](_.getTime, new Date(_))

  given userIdCodec: Codec[UserId] = deriveCodec[UserId]

  given userStateCodec: Codec[UserState] = deriveCodec[UserState]

  given gpsLocationCodec: Codec[GPSLocation] = deriveCodec[GPSLocation]

  given routingModeCodec: Codec[RoutingMode] = deriveCodec[RoutingMode]

  given drivingEventCodec: Codec[DrivingEvent] = deriveAllCodecs[DrivingEvent]

  given routingStartedCodec: Codec[RoutingStarted] = deriveCodec[RoutingStarted]

  given sampledLocationCodec: Codec[SampledLocation] = deriveCodec[SampledLocation]

  given trackingCodec: Codec[Tracking] =
    import cats.implicits._
    Codec[Tracking](
      Encoder[Tracking]: (writer, tracking) =>
        writer.writeMapHeader(2).writeString("user").write(tracking.user).writeString("route").write(tracking.route),
      Decoder[Tracking]: reader =>
        val length = reader.readMapHeader().toInt
        (0 until length).foldLeft((Option.empty[UserId], Option.empty[Route])): (data, _) =>
          reader.readString() match
            case "user" => (Some(reader.read[UserId]()), data._2)
            case "route" => (data._1, Some(reader.read[Route]()))
            case _ => reader.unexpectedDataItem(expected = "`user` or `route`")
        .tupled.map(t => Tracking(t._1, t._2)).get,
    )

  given monitorableTrackingCodec: Codec[MonitorableTracking] =
    import cats.implicits._
    Codec[MonitorableTracking](
      Encoder[MonitorableTracking]: (writer, tracking) =>
        writer.writeMapHeader(5).writeString("user").write(tracking.user).writeString("route").write(tracking.route)
          .writeString("mode").write(tracking.mode).writeString("destination").write(tracking.destination)
          .writeString("expectedArrival").write(tracking.expectedArrival),
      Decoder[MonitorableTracking]: reader =>
        val length = reader.readMapHeader().toInt
        val result = (0 until length).foldLeft(
          (
            Option.empty[UserId],
            Option.empty[RoutingMode],
            Option.empty[GPSLocation],
            Option.empty[Date],
            Option.empty[Route],
          ),
        ): (data, _) =>
          reader.readString() match
            case "user" => (Some(reader.read[UserId]()), data._2, data._3, data._4, data._5)
            case "mode" => (data._1, Some(reader.read[RoutingMode]()), data._3, data._4, data._5)
            case "destination" => (data._1, data._2, Some(reader.read[GPSLocation]()), data._4, data._5)
            case "expectedArrival" => (data._1, data._2, data._3, Some(reader.read[Date]()), data._5)
            case "route" => (data._1, data._2, data._3, data._4, Some(reader.read[Route]()))
            case _ =>
              reader.unexpectedDataItem(expected = "`user`, `route`, `mode`, `destination` or `expectedArrival`")
        result.tupled.map(t => Tracking.withMonitoring(t._1, t._2, t._3, t._4, t._5)).get,
    )
