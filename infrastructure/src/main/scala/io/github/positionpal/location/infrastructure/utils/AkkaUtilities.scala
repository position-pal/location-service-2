package io.github.positionpal.location.infrastructure.utils

import akka.actor.typed.{ActorSystem, Behavior}
import com.typesafe.config.ConfigFactory

def startup[T](fileName: String = "akka-cluster")(root: => Behavior[T]): ActorSystem[T] =
  val config = ConfigFactory.load(fileName)
  ActorSystem(root, "ClusterSystem", config)
