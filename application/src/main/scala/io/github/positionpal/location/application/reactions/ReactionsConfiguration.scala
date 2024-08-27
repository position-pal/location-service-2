package io.github.positionpal.location.application.reactions

/** Configuration for reactions.
  * @param proximityThreshold the distance threshold, expressed in meters, to consider two positions near each other
  * @param stationarySamplesThreshold the number of samples to trigger an alert if the position is stationary
  */
private case class ReactionsConfiguration(proximityThreshold: Double, stationarySamplesThreshold: Int)

object ReactionsConfiguration:
  import cats.effect.Sync
  import pureconfig.ConfigReader
  import io.github.positionpal.location.commons.ConfigProvider

  private val namespace = "reactions"

  implicit val reader: ConfigReader[ReactionsConfiguration] =
    ConfigReader.forProduct2("proximityThreshold", "stationarySamplesThreshold")(ReactionsConfiguration.apply)

  def get[M[_]: Sync]: M[ReactionsConfiguration] =
    ConfigProvider[M, ReactionsConfiguration](namespace = namespace).configuration
