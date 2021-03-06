import scala.annotation.tailrec
import scala.collection.immutable.Queue

case class ElevatorBank(elevators: Map[String, Elevator], requests: Queue[FloorRequest] = Queue.empty)

object ElevatorBank {
  import scalaz._
  import Scalaz._

  type ElevatorBankState[T] = State[ElevatorBank, T]

  def add(a: AddElevator): ElevatorBankState[List[String]] = State { bank =>
    val elevator = Elevator(ElevatorConfig(a.name, a.minFloor, a.maxFloor), 0)
    val b: ElevatorBank = bank.copy(elevators = bank.elevators.updated(a.name, elevator))
    (b, List(s"elevator ${a.name} is added"))
  }

  def request(req: FloorRequest): ElevatorBankState[List[String]] = {
    for {
      msg <- State { bank: ElevatorBank =>
        val message = s"request to ${req.floor} with ${req.direction} direction has been queued"
        (bank.copy(requests = bank.requests.enqueue(req)), message)
      }
      messages <- processRequests
    } yield {
      msg :: messages
    }
  }

  val processRequests: ElevatorBankState[List[String]] = State { bank: ElevatorBank =>
    bank.requests match {
      case Queue() =>
        (bank, Nil)
      case req +: tail =>
        val costs: Map[Elevator, Int] = bank.elevators.values.flatMap { elevator =>
          val dispatch = ElevatorDispatch(elevator.config.name, req.floor, Some(req.direction))
          val costOpt: Option[Int] = req.costFunction.cost(elevator, dispatch)
          costOpt.map(cost => elevator -> cost)
        }.toMap

        if (costs.isEmpty) {
          (bank, Nil)
        } else {
          val (elevator, _) = costs.minBy(_._2)

          val dispatch = ElevatorDispatch(elevator.config.name, req.floor, Some(req.direction))
          val (newElevator, m1) = Elevator.dispatch(dispatch).run(elevator)
          val b1 = bank.copy(elevators = bank.elevators.updated(newElevator.config.name, newElevator), tail)

          val (b2, m2) = processRequests.run(b1)

          (b2, m1 ::: m2)
        }
    }
  }

  def dispatch(dispatch: ElevatorDispatch): ElevatorBankState[List[String]] = State { bank =>
    bank.elevators.get(dispatch.name) match {
      case None =>
        (bank, List(s"elevator ${dispatch.name} doesn't exist"))
      case Some(elevator) =>
        val (e, msgs) = Elevator.dispatch(dispatch).run(elevator)
        (bank.copy(elevators = bank.elevators.updated(dispatch.name, e)), msgs)
    }
  }

  val moveOne: ElevatorBankState[List[String]] = {
    for {
      m1 <- State { bank: ElevatorBank =>
        val elevators: Map[String, (Elevator, List[String])] = bank.elevators.mapValues { elevator =>
            val (e, msgs) = Elevator.moveOne.run(elevator)
            (e, msgs)
        }.view.toMap

        (bank.copy(elevators = elevators.mapValues(_._1).view.toMap), elevators.values.flatMap(_._2).toList)
      }
      m2 <- processRequests
    } yield {
      m1 ::: m2
    }
  }

  def move(floors: Int): ElevatorBankState[List[String]] = State { bank =>
    val (e, messages) = List.fill(floors)(moveOne).runTraverseS[ElevatorBank, List[String]](bank)(identity)
    (e, messages.flatten)
  }
}
