import scala.annotation.tailrec
import scala.collection.immutable.Queue

object Direction extends Enumeration {
  type Type = Value

  val Up = Value("up")
  val Down = Value("down")

  def fromString(name: String): Type = values.find(_.toString == name).getOrElse(throw new IllegalArgumentException(s"unknown direction: $name"))
}

case class ElevatorConfig(name: String, minFloor: Int, maxFloor: Int)

case class ElevatorRequest(floor: Int, directionOpt: Option[Direction.Type])

case class Elevator(config: ElevatorConfig, lastFloor: Int, requests: Queue[ElevatorRequest] = Queue.empty) {
  def isMoving: Boolean = requests.nonEmpty
}

object Elevator {
  def canDispatch(elevator: Elevator, dispatch: ElevatorDispatchMessage): Boolean = {
    elevator.config.name == dispatch.name && elevator.config.minFloor <= dispatch.floor && dispatch.floor <= elevator.config.maxFloor
  }

  def dispatch(elevator: Elevator, message: ElevatorDispatchMessage): Elevator = {
    if (canDispatch(elevator, message)) {
      if (elevator.lastFloor == message.floor) {
        elevator
      } else {
        val req = ElevatorRequest(message.floor, message.directionOpt)
        elevator.copy(requests = elevator.requests.enqueue(req))
      }
    } else {
      throw new IllegalArgumentException(s"can't dispatch elevator ${elevator.config.name} to ${message.floor} floor")
    }
  }

  def moveOne(elevator: Elevator): Elevator = {
    elevator.requests.headOption match {
      case None =>
        elevator
      case Some(ElevatorRequest(floor, _)) =>
        val newFloor: Int = elevator.lastFloor + (if (floor > elevator.lastFloor) 1 else -1)
        val newRequests: Queue[ElevatorRequest] = elevator.requests.filter(_.floor != newFloor)
        elevator.copy(lastFloor = newFloor, requests = newRequests)
    }
  }

  @tailrec
  def move(elevator: Elevator, floors: Int): Elevator = {
    floors match {
      case n if n < 0 => throw new IllegalArgumentException(s"can't move the elevator by $floors floors")
      case 0 => elevator
      case 1 => moveOne(elevator)
      case _ => move(moveOne(elevator), floors - 1)
    }
  }
}
