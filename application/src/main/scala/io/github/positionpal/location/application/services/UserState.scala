package io.github.positionpal.location.application.services

/** The [[io.github.positionpal.location.domain.User]] state information. */
enum UserState:
  /** The user is online and is continuously sending location updates. */
  case Active

  /** The user is not sending location updates. */
  case Inactive

  /** The user requested help. */
  case SOS

  /** The user is currently routing to a destination. */
  case Routing
