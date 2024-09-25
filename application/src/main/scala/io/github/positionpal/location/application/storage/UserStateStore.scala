package io.github.positionpal.location.application.storage

import io.github.positionpal.location.application.services.UserState
import io.github.positionpal.location.domain.UserId

trait UserStateReader[M[_]]:
  def currentState(userId: UserId): M[UserState]

trait UserStateWriter[M[_]]:
  def update[U](currentState: UserState): M[U]

trait UserStateStore[M[_]] extends UserStateReader[M] with UserStateWriter[M]
