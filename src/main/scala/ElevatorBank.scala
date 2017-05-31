import scala.annotation.tailrec
import scala.collection.immutable.Queue

case class ElevatorBank(elevators: Map[String, Elevator], requests: Queue[FloorRequestMessage] = Queue.empty)

object ElevatorBank {
  def processRequest(bank: ElevatorBank, request: FloorRequestMessage): ElevatorBank = {
    processRequests(bank.copy(requests = bank.requests.enqueue(request)))
  }

  def processRequests(bank: ElevatorBank): ElevatorBank = {
    bank.requests match {
      case Queue() => bank
      case req +: tail =>
        val costs: Map[Elevator, Int] = bank.elevators.values.flatMap { elevator =>
          val dispatch = ElevatorDispatchMessage(elevator.config.name, req.floor, Some(req.direction))
          val costOpt: Option[Int] = req.costFunction.cost(elevator, dispatch)
          costOpt.map(cost => elevator -> cost)
        }.toMap

        if (costs.isEmpty) {
          bank
        } else {
          val (elevator, _) = costs.minBy(_._2)

          val dispatch = ElevatorDispatchMessage(elevator.config.name, req.floor, Some(req.direction))
          val newElevator = Elevator.dispatch(elevator, dispatch)
          val newBank = bank.copy(elevators = bank.elevators.updated(newElevator.config.name, newElevator), tail)

          processRequests(newBank)
        }
    }
  }

  def dispatch(bank: ElevatorBank, dispatch: ElevatorDispatchMessage): ElevatorBank = {
    bank.elevators.get(dispatch.name) match {
      case None => throw new IllegalArgumentException(s"elevator '${dispatch.name} doesn't exist")
      case Some(elevator) => bank.copy(elevators = bank.elevators.updated(dispatch.name, Elevator.dispatch(elevator, dispatch)))
    }
  }

  def moveOne(bank: ElevatorBank): ElevatorBank = {
    processRequests(bank.copy(elevators = bank.elevators.mapValues(Elevator.moveOne).view.toMap))
  }

  @tailrec
  def move(bank: ElevatorBank, floors: Int): ElevatorBank = {
    floors match {
      case n if n < 0 => throw new IllegalArgumentException(s"can't move the elevator by $floors floors")
      case 0 => bank
      case 1 => moveOne(bank)
      case _ => move(moveOne(bank), floors - 1)
    }
  }
}
