package io.github.positionpal.location.infrastructure.services

/** Marker trait for remote serializable messages exchanged between distributed actors.
  * Every message class exchanged between distributed actors should extend this trait
  * (which is referenced as binding in the Akka configuration).
  */
trait Serializable
