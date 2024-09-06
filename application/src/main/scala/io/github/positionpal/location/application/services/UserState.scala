package io.github.positionpal.location.application.services

/** The [[io.github.positionpal.location.domain.User]] state information. */
enum UserState:
  case Active
  case Inactive
  case SOS
  case Routing
