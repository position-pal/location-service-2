package io.github.positionpal.location.domain

import java.util.Date

import scala.annotation.targetName

/** The route followed by a [[User]] while going from one place to another. */
trait Route:
  /** @return the [[DrivingEvents.StartRouting]] originating the route. */
  def sourceEvent: DrivingEvents.StartRouting

  /** @return the expected arrival time to the destination. */
  def expectedArrivalTime: Date

  /** @return the list of positions composing this route, in reverse chronological order,
    *         i.e., from the most recent to the oldest (the most recent is in the head).
    */
  def positions: List[GPSLocation]

  /** @return a new route whose [[positions]] have been prepended the given [[sample]]. */
  @targetName("addSample") def +(sample: GPSLocation): Route

object Route:
  import DrivingEvents.StartRouting

  def apply(event: StartRouting, expectedArrivalTime: Date): Route =
    RouteImpl(event, expectedArrivalTime, List())

  def withPositions(event: StartRouting, expectedArrivalTime: Date, positions: List[GPSLocation]): Route =
    RouteImpl(event, expectedArrivalTime, positions)

  private case class RouteImpl(
      sourceEvent: DrivingEvents.StartRouting,
      expectedArrivalTime: Date,
      positions: List[GPSLocation],
  ) extends Route:
    @targetName("addSample")
    override def +(sample: GPSLocation): Route =
      withPositions(sourceEvent, expectedArrivalTime, sample +: positions)

/** The mode of routing to a destination. */
enum RoutingMode:
  case Driving, Walking, Cycling
