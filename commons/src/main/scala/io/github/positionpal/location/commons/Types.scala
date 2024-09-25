package io.github.positionpal.location.commons

import cats.mtl.{Ask, Raise}

/** A type alias for a type constructor `M` expressing the
  * capability to raise an error of type `E`.
  */
type CanRaise[E] = [M[_]] =>> Raise[M, E]

type CanAsk[E] = [M[_]] =>> Ask[M, E]
