import scala.annotation.tailrec
import scala.collection.immutable.Queue

object Direction extends Enumeration {
  type Type = Value

  val Up = Value("up")
  val Down = Value("down")

  def fromStringOpt(name: String): Option[Type] = values.find(_.toString == name)
  def fromString(name: String): Type = fromStringOpt(name).getOrElse(throw new IllegalArgumentException(s"unknown direction: $name"))

  def unapply(name: String): Option[Type] = fromStringOpt(name)
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

  def canDispatch(dispatch: ElevatorDispatch): ElevatorState[Boolean] = State.gets { elevator =>
    elevator.config.name == dispatch.name && elevator.config.minFloor <= dispatch.floor && dispatch.floor <= elevator.config.maxFloor
  }

  def dispatch(dispatch: ElevatorDispatch): ElevatorState[List[String]] = State { elevator =>
    if (elevator.lastFloor == dispatch.floor) {
      (elevator, List(s"elevator ${elevator.config.name} is already at floor ${dispatch.floor}"))
    } else {
      val req = ElevatorRequest(dispatch.floor, dispatch.directionOpt)
      val e = elevator.copy(requests = elevator.requests.enqueue(req))

      (e, List(s"elevator ${elevator.config.name} dispatched to floor ${dispatch.floor}"))
    }
  }

  val moveOne: ElevatorState[List[String]] = State { elevator =>
    elevator.requests.headOption match {
      case None => (elevator, Nil)
      case Some(ElevatorRequest(floor, _)) =>
        val newFloor: Int = elevator.lastFloor + (if (floor > elevator.lastFloor) 1 else -1)
        val (filledRequests, pendingRequests) = elevator.requests.partition(_.floor == newFloor)
        if (filledRequests.isEmpty) {
          (elevator.copy(lastFloor = newFloor), Nil)
        } else {
          val message = s"elevator ${elevator.config.name} completed dispatch to floor $newFloor, ${pendingRequests.size} destinations remaining"
          (elevator.copy(lastFloor = newFloor, requests = pendingRequests), List(message))
        }
    }
  }

  def move(floors: Int): ElevatorState[List[String]] = State { elevator =>
    val (e, messages) = List.fill(floors)(moveOne).runTraverseS[Elevator, List[String]](elevator)(identity)
    (e, messages.flatten)
  }
}
