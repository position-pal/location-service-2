package io.github.positionpal.location.application.reactions

/** [[TrackingEventReaction]]s' configuration.
  * @param proximityToleranceMeters the distance, expressed in meters, to consider two
  *                                 positions approximately in the same location
  * @param stationarySamples the number of samples to consider for the stationary check
  */
private case class ReactionsConfiguration(proximityToleranceMeters: Double, stationarySamples: Int)

object ReactionsConfiguration:
  import cats.effect.Sync
  import pureconfig.ConfigReader
  import io.github.positionpal.location.commons.ConfigProvider

  private val namespace = "reactions"

  implicit val reader: ConfigReader[ReactionsConfiguration] =
    ConfigReader.forProduct2("proximityToleranceMeters", "stationarySamples")(ReactionsConfiguration.apply)

  def get[M[_]: Sync]: M[ReactionsConfiguration] =
    ConfigProvider[M, ReactionsConfiguration](namespace = namespace).configuration
