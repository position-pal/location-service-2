package io.github.positionpal.location.domain

import scala.annotation.targetName

/** The route followed by a [[User]] while going from one place to another. */
trait Route:
  /** @return the [[DrivingEvents.StartRoutingEvent]] originating the route. */
  def event: DrivingEvents.StartRoutingEvent

  /** @return the list of positions of the route. */
  def positions: List[GPSLocation]

  /** @return a new route whose [[positions]] are the concatenation of this route's and the given [[route]]'s. */
  @targetName("addSample") def +(route: Route): Route

object Route:
  def apply(event: DrivingEvents.StartRoutingEvent): Route = RouteImpl(event, List())

  private def withInitialPositions(event: DrivingEvents.StartRoutingEvent, positions: List[GPSLocation]): Route =
    RouteImpl(event, positions)

  private case class RouteImpl(
      event: DrivingEvents.StartRoutingEvent,
      positions: List[GPSLocation],
  ) extends Route:
    @targetName("addSample")
    override def +(route: Route): Route =
      withInitialPositions(event, positions ++ route.positions)
