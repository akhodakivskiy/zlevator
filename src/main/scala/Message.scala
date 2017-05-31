

/**
  * Created by anton on 5/30/17.
  */
trait Message

case class ElevatorDispatchMessage(name: String, floor: Int, directionOpt: Option[Direction.Type]) extends Message
case class FloorRequestMessage(floor: Int, direction: Direction.Type, costFunction: ElevatorCostFunction) extends Message
