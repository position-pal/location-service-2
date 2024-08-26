package io.github.positionpal.location.domain

import scala.annotation.targetName

/** The route followed by a [[User]] while going from one place to another. */
trait Route:
  /** @return the [[DrivingEvents.StartRoutingEvent]] originating the route. */
  def sourceEvent: DrivingEvents.StartRoutingEvent

  /** @return the list of positions of the route. */
  def positions: List[GPSLocation]

  /** @return a new route whose [[positions]] have been prepended the given [[sample]]. */
  @targetName("addSample") def +(sample: GPSLocation): Route

object Route:
  def apply(event: DrivingEvents.StartRoutingEvent): Route = RouteImpl(event, List())

  private def withInitialPositions(event: DrivingEvents.StartRoutingEvent, positions: List[GPSLocation]): Route =
    RouteImpl(event, positions)

  private case class RouteImpl(
      sourceEvent: DrivingEvents.StartRoutingEvent,
      positions: List[GPSLocation],
  ) extends Route:
    @targetName("addSample")
    override def +(sample: GPSLocation): Route =
      withInitialPositions(sourceEvent, sample +: positions)

/** The mode of routing to a destination. */
enum RoutingMode:
  case Driving, Walking, Cycling
