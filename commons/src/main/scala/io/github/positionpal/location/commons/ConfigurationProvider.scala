package io.github.positionpal.location.commons

import cats.effect.IO

/** A generic provider of configuration [[C]]. */
trait ConfigurationProvider[C]:

  /** @return an effectful computation providing the configuration [[C]]. */
  def configuration: IO[C]

/** A provider of environment variables. */
object EnvVariablesProvider extends ConfigurationProvider[Map[String, String]]:
  def configuration: IO[Map[String, String]] = IO(sys.env)
