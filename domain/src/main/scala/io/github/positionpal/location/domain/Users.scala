package io.github.positionpal.location.domain

/** An identifier uniquely identifying a [[User]]. */
final case class UserId(id: String)

/** An identifier uniquely identifying a group of [[User]]s. */
final case class GroupId(id: String)

/** A user of the system, identified by a [[UserId]] and belonging to a set of [[GroupId]]s. */
case class User(userId: UserId, inGroups: Set[GroupId])
