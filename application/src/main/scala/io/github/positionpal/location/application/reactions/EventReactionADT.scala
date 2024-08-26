package io.github.positionpal.location.application.reactions

import scala.annotation.targetName

import cats.Monad
import io.github.positionpal.location.domain.DrivingEvents

/** An abstract data type for effectful event reactions. */
trait EventReactionADT:
  /** The type of the reaction's environment, i.e., its context. */
  type Environment

  /** The type of the event triggering the reaction. */
  type Event <: DrivingEvents

  /** The outcome of the reaction application, i.e., the result of the reaction. */
  type Outcome

  import cats.data.ReaderT

  /** A computational reaction to an [[Event]] within a given [[Environment]],
    * abstracted over an effect `F` (e.g., `IO`, `Task`, etc.).
    */
  opaque type EventReaction[F[_]] = ReaderT[F, (Environment, Event), Outcome]

  /** Creates an [[EventReaction]] from the provided effectful [[reaction]] function.
    * @tparam F the effect type of the reaction, constrained by [[Async]] to support asynchronous,
    *           non-blocking computations, such as interacting with an API or a database.
    * @return the created [[EventReaction]].
    */
  def on[F[_]](reaction: ((Environment, Event)) => F[Outcome]): EventReaction[F] = ReaderT(reaction)

  extension [F[_]: Monad](reaction: EventReaction[F])
    /** Executes the event reaction by applying the provided [[environment]] and [[event]], resulting
      * in an [[Outcome]] wrapped in the effect type `F[_]`. The computation is deferred and executed
      * within the effect context.
      * @param environment the [[Environment]] (context) in which the event reaction computation occurs
      * @param event the [[Event]] triggering the reaction
      * @return an effect `F` containing the resulting [[Outcome]] of processing the event.
      */
    def apply(environment: Environment, event: Event): F[Outcome] = reaction.run((environment, event))

    /** Composes two [[EventReaction]]s by chaining their execution, running this reaction first,
      * followed by the provided [[other]] reaction, within the effectful context `F`.
      * @param other the [[EventReaction]] to execute after this one
      * @return a new [[EventReaction]] with the combined behavior of the two reactions.
      */
    @targetName("andThen") def >>>(other: EventReaction[F]): EventReaction[F]

/** A trait representing an event reaction that produces two possible outcomes: a "left" (failure)
  * and a "right" (success) outcome, following a short-circuit evaluation strategy: once the left
  * outcome is produced, no further reactions are processed, stopping the chain of reactions at the
  * first failure.
  */
trait BinaryShortCircuitReaction extends EventReactionADT:
  /** The type of the left outcome, representing a failure. */
  type LeftOutcome

  /** The type of the right outcome, representing a success. */
  type RightOutcome

  override type Outcome = Either[LeftOutcome, RightOutcome]

  import cats.implicits.toFlatMapOps

  extension [F[_]: Monad](reaction: EventReaction[F])
    @targetName("andThen")
    def >>>(other: EventReaction[F]): EventReaction[F] = on: (env, event) =>
      reaction(env, event).flatMap:
        case Right(_) => other(env, event)
        case l @ Left(_) => Monad[F].pure(l)
