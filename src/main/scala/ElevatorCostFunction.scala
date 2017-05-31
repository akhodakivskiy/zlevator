import scala.annotation.tailrec
import scala.collection.immutable.Queue

/**
  * Created by anton on 5/30/17.
  */
trait ElevatorCostFunction {
  def cost(elevator: Elevator, dispatch: ElevatorDispatchMessage): Option[Int]
}

object ElevatorCostFunction {
  def floorsToTravel(elevator: Elevator): Int = {
    @tailrec
    def inner(e: Elevator, totalCost: Int): Int = {
      if (!e.isMoving) {
        totalCost
      } else {
        inner(Elevator.moveOne(e), totalCost + 1)
      }
    }

    inner(elevator, 0)
  }
}

case class GreedyElevatorCostFunction(name: String) extends ElevatorCostFunction {
  def cost(elevator: Elevator, dispatch: ElevatorDispatchMessage): Option[Int] = {
    if (Elevator.canDispatch(elevator, dispatch) && !elevator.isMoving) Some(0) else None
  }
}

object SingleOccupancyElevatorCostFunction extends ElevatorCostFunction {

  def cost(elevator: Elevator, dispatch: ElevatorDispatchMessage): Option[Int] = {
    if (!elevator.isMoving && Elevator.canDispatch(elevator, dispatch)) {
      val e = Elevator.dispatch(elevator, dispatch)
      Some(ElevatorCostFunction.floorsToTravel(e))
    } else {
      None
    }
  }
}

object SharedElevatorCostFunction extends ElevatorCostFunction {
  def cost(elevator: Elevator, dispatch: ElevatorDispatchMessage): Option[Int] = {
    def baseLineCost: Option[Int] = {
      val e = Elevator.dispatch(elevator, dispatch)
      Some(ElevatorCostFunction.floorsToTravel(e))
    }

    if (Elevator.canDispatch(elevator, dispatch)) {
      elevator.requests.headOption match {
        case None => baseLineCost
        case Some(request) =>
          val elevatorDirection: Direction.Type = if (elevator.lastFloor > request.floor) Direction.Down else Direction.Up
          val dispatchDirection: Direction.Type = if (elevator.lastFloor > dispatch.floor) Direction.Down else Direction.Up
          val requestDirectionOpt: Option[Direction.Type] = request.directionOpt

          if (elevatorDirection == dispatchDirection && (requestDirectionOpt.contains(elevatorDirection) || requestDirectionOpt.isEmpty)) {
            Some(Math.abs(elevator.lastFloor - dispatch.floor))
          } else {
            None
          }
      }
    } else {
      None
    }
  }
}