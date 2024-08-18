package io.github.positionpal.location.application.geo

import cats.Monad
import io.github.positionpal.location.domain.DrivingEvents

trait EventReactionADT[F[_]: Monad]:
  type Environment
  type Event <: DrivingEvents
  type BadOutcome
  type GoodOutcome

  import cats.data.ReaderT

  opaque type EventReaction = ReaderT[F, (Environment, Event), Either[BadOutcome, GoodOutcome]]

  def on(reaction: ((Environment, Event)) => F[Either[BadOutcome, GoodOutcome]]): EventReaction =
    ReaderT(reaction)

  extension (reaction: EventReaction)
    def >>>(other: EventReaction): EventReaction =
      reaction.flatMap:
        case Right(_) => other
        case l @ Left(_) => ReaderT.liftF(Monad[F].pure(l))

    def apply(env: Environment, event: Event): F[Either[BadOutcome, GoodOutcome]] = reaction.run((env, event))
