package io.github.positionpal.location.infrastructure.utils

import cats.effect.{IO, Resource}
import io.github.positionpal.location.infrastructure.geo.MapboxServiceAdapter
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

object MapboxServiceAdapterUtils:
  given LoggerFactory[IO] = Slf4jFactory.create[IO]
  val mapboxServiceAdapter = MapboxServiceAdapter()
  val clientResource: Resource[IO, Client[IO]] = EmberClientBuilder.default[IO].build
