package io.github.positionpal.location.commons

import cats.effect.Sync

/** A generic provider of configuration [[C]]. */
trait ConfigurationProvider[F[_], C]:

  /** @return an effectful computation providing the configuration [[C]]. */
  def configuration: F[C]

/** A provider of environment variables. */
class EnvVariablesProvider[F[_]: Sync] extends ConfigurationProvider[F, Map[String, String]]:
  def configuration: F[Map[String, String]] = Sync[F].delay(sys.env)
