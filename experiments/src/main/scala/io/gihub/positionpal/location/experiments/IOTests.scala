package io.gihub.positionpal.location.experiments

import cats.effect.IO
import cats.effect.unsafe.IORuntime

import scala.concurrent.{Await, ExecutionContext, Future}

object IOTests extends App:

  val ioComputation =
    for
      _ <- IO(println("Type something:"))
      input <- IO(scala.io.StdIn.readLine())
      _ = println("Hello World :)")
      _ <- IO(println(s"You typed: $input"))
    yield ()

  // An implicit ExecutionContext is required for Future
  implicit val runtime: IORuntime = IORuntime.global
  implicit val ec: ExecutionContext = ExecutionContext.global

  val result: Future[Unit] = ioComputation.unsafeToFuture()
  Await.result(result, scala.concurrent.duration.Duration.Inf)
