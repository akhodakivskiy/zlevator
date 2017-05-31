import scala.util.matching.Regex

/**
  * Created by anton on 5/30/17.
  */
object IntValue {
  def unapply(str: String): Option[Int] = {
    try {
      Some(Integer.parseInt(str))
    } catch {
      case _: NumberFormatException => None
    }
  }
}

case class AddElevator(name: String, minFloor: Int, maxFloor: Int)
case class ElevatorDispatch(name: String, floor: Int, directionOpt: Option[Direction.Type])
case class FloorRequest(floor: Int, direction: Direction.Type, costFunction: ElevatorCostFunction)

object AddElevator {
  val Pattern: Regex = "add (\\w+) (\\d+) (\\d+)".r

  def unapply(line: String): Option[AddElevator] = {
    line match {
      case Pattern(name, IntValue(minFloor), IntValue(maxFloor)) if minFloor <= maxFloor => Some(AddElevator(name, minFloor, maxFloor))
      case _ => None
    }
  }
}

object ElevatorDispatch {
  val Pattern1: Regex = "dispatch (\\w+) (\\d+)".r
  val Pattern2: Regex = "dispatch (\\w+) (\\d+) (\\w+)".r
  def unapply(line: String): Option[ElevatorDispatch] = {
    line match {
      case Pattern1(name, IntValue(floor)) => Some(ElevatorDispatch(name, floor, None))
      case Pattern2(name, IntValue(floor), Direction(direction)) => Some(ElevatorDispatch(name, floor, Some(direction)))
      case _ => None
    }
  }
}

object FloorRequest {
  val Pattern: Regex = "request (\\d+) (\\w+) (\\w+)".r
  def unapply(line: String): Option[FloorRequest] = {
    line match {
      case Pattern(IntValue(floor), Direction(direction), ElevatorCostFunction(cost)) => Some(FloorRequest(floor, direction, cost))
      case _ => None
    }
  }
}

case class Move(floors: Int)

object Move {
  val Pattern: Regex = "move (\\d+)".r
  def unapply (name: String): Option[Move] = {
    name match {
      case Pattern(IntValue(floors)) => Some(Move(floors))
      case _ => None
    }
  }
}
