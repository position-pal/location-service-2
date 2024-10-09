package io.gihub.positionpal.location.experiments.actors

trait SerializableMessage

enum SerializableEvent:
  case MyEvent1(val x: Int) extends SerializableEvent
  case MyEvent2(val y: String) extends SerializableEvent
