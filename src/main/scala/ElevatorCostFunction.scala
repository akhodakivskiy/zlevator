import scala.annotation.tailrec
import scala.collection.immutable.Queue
import scala.util.matching.Regex

/**
  * Created by anton on 5/30/17.
  */
trait ElevatorCostFunction {
  def cost(elevator: Elevator, dispatch: ElevatorDispatch): Option[Int]
}

object ElevatorCostFunction {
  def floorsToTravel(elevator: Elevator): Int = {
    @tailrec
    def inner(e: Elevator, totalCost: Int): Int = {
      if (!e.isMoving) {
        totalCost
      } else {
        inner(Elevator.moveOne.exec(e), totalCost + 1)
      }
    }

    inner(elevator, 0)
  }

  val GreedyPattern: Regex = "(\\w+)".r

  def unapply(name: String): Option[ElevatorCostFunction] = {
    name match {
      case GreedyPattern(name) => Some(GreedyElevatorCostFunction(name))
      case "single" => Some(SingleOccupancyElevatorCostFunction)
      case "shared" => Some(SharedElevatorCostFunction)
      case _ => None
    }
  }
}

case class GreedyElevatorCostFunction(name: String) extends ElevatorCostFunction {
  def cost(elevator: Elevator, dispatch: ElevatorDispatch): Option[Int] = {
    if (Elevator.canDispatch(dispatch).run(elevator)._2 && !elevator.isMoving) Some(0) else None
  }
}

object SingleOccupancyElevatorCostFunction extends ElevatorCostFunction {

  def cost(elevator: Elevator, dispatch: ElevatorDispatch): Option[Int] = {
    if (!elevator.isMoving && Elevator.canDispatch(dispatch).run(elevator)._2) {
      val e = Elevator.dispatch(dispatch).exec(elevator)
      Some(ElevatorCostFunction.floorsToTravel(e))
    } else {
      None
    }
  }
}

object SharedElevatorCostFunction extends ElevatorCostFunction {
  def cost(elevator: Elevator, dispatch: ElevatorDispatch): Option[Int] = {
    def baseLineCost: Option[Int] = {
      val e = Elevator.dispatch(dispatch).exec(elevator)
      Some(ElevatorCostFunction.floorsToTravel(e))
    }

    if (Elevator.canDispatch(dispatch).run(elevator)._2) {
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