package io.github.positionpal.location.infrastructure.utils

import cats.effect.{IO, Resource}
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

object HTTPUtils:

  val client: Resource[IO, Client[IO]] =
    given LoggerFactory[IO] = Slf4jFactory.create[IO]
    EmberClientBuilder.default[IO].build
