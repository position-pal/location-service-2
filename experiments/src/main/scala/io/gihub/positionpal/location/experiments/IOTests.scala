package io.gihub.positionpal.location.experiments

import cats.effect.IO

object IOTests extends App:

  val ioComputation =
    for
      _ <- IO(println("Type something:"))
      input <- IO(scala.io.StdIn.readLine())
      _ = println("Hello World :)")
      _ <- IO(println(s"You typed: $input"))
    yield ()

  import cats.effect.unsafe.implicits.global
  ioComputation.unsafeRunSync()
