package io.github.positionpal.location.application.reactions

private case class ReactionsConfiguration(distanceThreshold: Double, stationarySamplesThreshold: Int)

object ReactionsConfiguration:
  import cats.effect.Sync
  import pureconfig.ConfigReader
  import io.github.positionpal.location.commons.ConfigProvider

  private val namespace = "reactions"

  implicit val reader: ConfigReader[ReactionsConfiguration] =
    ConfigReader.forProduct2("distanceThreshold", "stationarySamplesThreshold")(ReactionsConfiguration.apply)

  def get[M[_]: Sync]: M[ReactionsConfiguration] =
    ConfigProvider[M, ReactionsConfiguration](namespace = namespace).configuration
