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
  import scalaz._
  import Scalaz._

  type ElevatorState[T] = State[Elevator, T]

  def canDispatch(dispatch: ElevatorDispatchMessage): ElevatorState[Boolean] = State.gets { elevator =>
    elevator.config.name == dispatch.name && elevator.config.minFloor <= dispatch.floor && dispatch.floor <= elevator.config.maxFloor
  }

  def dispatch(dispatch: ElevatorDispatchMessage): ElevatorState[String] = State { elevator =>
    if (elevator.lastFloor == dispatch.floor) {
      (elevator, s"elevator ${elevator.config.name} is already at floor ${dispatch.floor}")
    } else {
      val req = ElevatorRequest(dispatch.floor, dispatch.directionOpt)
      val e = elevator.copy(requests = elevator.requests.enqueue(req))

      (e, s"elevator ${elevator.config.name} dispatched to floor ${dispatch.floor}")
    }
  }

  val moveOne: ElevatorState[Option[String]] = State { elevator =>
    elevator.requests.headOption match {
      case None => (elevator, None)
      case Some(ElevatorRequest(floor, _)) =>
        val newFloor: Int = elevator.lastFloor + (if (floor > elevator.lastFloor) 1 else -1)
        val (filledRequests, pendingRequests) = elevator.requests.partition(_.floor == newFloor)
        if (filledRequests.isEmpty) {
          (elevator.copy(lastFloor = newFloor), None)
        } else {
          val message = s"elevator ${elevator.config.name} filled ${filledRequests.size} at floor $newFloor"
          (elevator.copy(lastFloor = newFloor, requests = pendingRequests), Some(message))
        }
    }
  }

  def move(floors: Int): ElevatorState[List[String]] = State { elevator =>
    val (e, messages) = List.fill(floors)(moveOne).runTraverseS[Elevator, Option[String]](elevator)(identity)
    (e, messages.flatten)
  }
}
