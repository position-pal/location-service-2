package io.gihub.positionpal.location.experiments.kafka

object Entitities:
  case class User(id: Int, name: String, group: Int)
  case class SampledLocation(userId: Int, lat: Double, lon: Double)

object EntitiesData:
  val users = Set(
    Entitities.User(1, "Alice", 1),
    Entitities.User(2, "Bob", 1),
    Entitities.User(3, "Charlie", 2),
    Entitities.User(4, "David", 2),
    Entitities.User(5, "Eve", 3),
    Entitities.User(6, "Frank", 3),
    Entitities.User(7, "Grace", 4),
    Entitities.User(8, "Hank", 4),
    Entitities.User(9, "Ivy", 5),
    Entitities.User(10, "Jack", 5),
  )
