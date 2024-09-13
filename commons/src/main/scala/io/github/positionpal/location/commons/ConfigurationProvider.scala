package io.github.positionpal.location.commons

import scala.reflect.ClassTag

import cats.effect.Sync

/** A generic provider of configuration [[C]]. */
trait ConfigurationProvider[F[_], C]:

  /** @return an effectful computation providing the configuration [[C]]. */
  def configuration: F[C]

/** A provider of environment variables. */
class EnvVariablesProvider[F[_]: Sync] extends ConfigurationProvider[F, Map[String, String]]:
  def configuration: F[Map[String, String]] = Sync[F].blocking(sys.env)

/** A provider of configuration [[C]] taken from a .conf file.
  * @note the [[ConfigReader]] must be provided using scala 2 implicits, like this:
  * {{{
  *   case class MyConfig(somefield: Int, anotherfield: String)
  *   implicit val reader: ConfigReader[MyConfig] =
  *     ConfigReader.forProduct2("somefield", "anotherfield")(MyConfig.apply)
  * }}}
  */
class ConfigProvider[F[_]: Sync, C](
    fileName: String = "application.conf",
    namespace: String = "",
)(using cr: pureconfig.ConfigReader[C], ct: ClassTag[C])
    extends ConfigurationProvider[F, C]:
  def configuration: F[C] =
    import pureconfig.*
    import pureconfig.module.catseffect.syntax.*
    ConfigSource.resources(fileName).at(namespace).loadF[F, C]()
