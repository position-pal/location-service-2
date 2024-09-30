package io.github.positionpal.location.domain

import scala.annotation.targetName

/** The route followed by a [[User]] while going from one place to another. */
trait Route:
  /** @return the [[RoutingStarted]] event originating the route, encapsulating all the routing information. */
  def sourceEvent: RoutingStarted

  /** @return the list of positions composing this route, in reverse chronological order,
    *         i.e., from the most recent to the oldest (the most recent is in the head).
    */
  def positions: List[GPSLocation]

  /** @return a new route whose [[positions]] have been prepended the given [[sample]]. */
  @targetName("addSample") def +(sample: GPSLocation): Route

object Route:

  def apply(event: RoutingStarted): Route = RouteImpl(event, List())

  def withPositions(event: RoutingStarted, positions: List[GPSLocation]): Route = RouteImpl(event, positions)

  private case class RouteImpl(sourceEvent: RoutingStarted, positions: List[GPSLocation]) extends Route:
    @targetName("addSample")
    override def +(sample: GPSLocation): Route =
      withPositions(sourceEvent, sample +: positions)

/** The mode of routing to a destination. */
enum RoutingMode:
  case Driving, Walking, Cycling
