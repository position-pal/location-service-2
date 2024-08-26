package io.github.positionpal.location.infrastructure.geo

import scala.concurrent.duration.{DurationDouble, FiniteDuration}

import cats.data.{EitherT, ReaderT}
import cats.effect.IO
import cats.implicits.catsSyntaxEither
import io.circe.Json
import io.github.positionpal.location.application.geo.Distance.meters
import io.github.positionpal.location.application.geo.{Distance, MapsService, MapsServiceError}
import io.github.positionpal.location.domain.RoutingMode.Driving
import io.github.positionpal.location.domain.{GPSLocation, RoutingMode}
import org.http4s.Uri
import org.http4s.circe.jsonOf
import org.http4s.client.Client
import org.http4s.implicits.uri

/** Configuration for the [[MapboxServiceAdapter]].
  * @param client the [[Client]] to use it for HTTP requests
  * @param accessToken the access token to authenticate with the Mapbox service
  */
case class Configuration(client: Client[IO], accessToken: String)

type IOWithContext[E] = ReaderT[IO, Configuration, E]
type Response[E] = EitherT[IOWithContext, MapsServiceError, E]

/** A [[MapService]] adapter interacting with the Mapbox service.
  * @see <a href="https://docs.mapbox.com/api/navigation/directions/">Mapbox Directions API</a>.
  */
class MapboxServiceAdapter extends MapsService[Response]:

  override def duration(mode: RoutingMode)(origin: GPSLocation, destination: GPSLocation): Response[FiniteDuration] =
    request[FiniteDuration](mode, origin, destination)(_.extractDuration)

  override def distance(mode: RoutingMode)(origin: GPSLocation, destination: GPSLocation): Response[Distance] =
    request[Distance](mode, origin, destination)(_.extractDistance)

  private def request[T](
      mode: RoutingMode,
      origin: GPSLocation,
      destination: GPSLocation,
  )(extract: Json => Either[MapsServiceError, T]): Response[T] =
    EitherT:
      ReaderT: config =>
        config.client.expect(directionsApiRoute(mode, origin, destination, config.accessToken))(jsonOf[IO, Json])
          .attempt.map(_.leftMap(_.getMessage).flatMap(extract(_)))

  private def directionsApiRoute(mode: RoutingMode, origin: GPSLocation, destination: GPSLocation, token: String): Uri =
    val smode = mode.toString.toLowerCase.appendedAll(if mode == Driving then "-traffic" else "")
    uri"https://api.mapbox.com/directions/v5/mapbox/".addPath(smode)
      .addSegment(s"${origin.longitude},${origin.latitude};${destination.longitude},${destination.latitude}")
      .withQueryParam("access_token", token)

  extension (json: Json)
    private def extractDistance: Either[String, Distance] =
      json.routes.downField("distance").as[Double].map(_.meters).leftMap(_.message)

    private def extractDuration: Either[String, FiniteDuration] =
      json.routes.downField("duration").as[Double].map(_.seconds).leftMap(_.message)

    private def routes = json.hcursor.downField("routes").downArray
