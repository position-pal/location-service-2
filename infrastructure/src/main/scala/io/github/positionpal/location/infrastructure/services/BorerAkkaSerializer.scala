package io.github.positionpal.location.infrastructure.services

import io.bullet.borer.Codec
import io.bullet.borer.derivation.ArrayBasedCodecs.deriveCodec
import io.github.positionpal.location.domain.DrivingEvent
import io.github.positionpal.location.presentation.{BorerCborAkkaSerializer, Codecs}

/** Custom Akka serializer for the RealTimeUserTracker actor. */
class BorerAkkaSerializer extends BorerCborAkkaSerializer with Codecs:
  override def identifier: Int = 19923

  // given stateCodec: Codec[RealTimeUserTracker.State] = deriveCodec[RealTimeUserTracker.State]
  given ignoreCoded: Codec[RealTimeUserTracker.Ignore.type] = deriveCodec[RealTimeUserTracker.Ignore.type]

  register[RealTimeUserTracker.Ignore.type]()
  register[DrivingEvent]()
  // register[RealTimeUserTracker.State]()
