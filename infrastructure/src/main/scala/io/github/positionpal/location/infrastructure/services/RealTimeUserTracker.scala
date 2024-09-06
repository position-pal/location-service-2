package io.github.positionpal.location.infrastructure.services

import akka.actor.typed.Behavior

object RealTimeUserTracker:
  sealed trait Command

  def apply(): Behavior[Command] = ???
