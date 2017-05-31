import org.scalatest.{FunSuite, Matchers}

/**
  * Created by anton on 5/30/17.
  */
class SingleOccupancyElevatorCostFunctionTest extends FunSuite with Matchers {
  val initElevators: Map[String, Elevator] = Map(
    "a" -> Elevator(ElevatorConfig("a", 0, 10), 0),
    "b" -> Elevator(ElevatorConfig("b", 0, 10), 10)
  )
  val initBank: ElevatorBank = ElevatorBank(initElevators)

  test("dispatch closest free elevator") {
    val b1: ElevatorBank = ElevatorBank.processRequest(initBank, FloorRequestMessage(3, Direction.Up, SingleOccupancyElevatorCostFunction))
    b1.elevators.get("a").map(_.isMoving) should contain (true)

    val b2: ElevatorBank = ElevatorBank.processRequest(initBank, FloorRequestMessage(7, Direction.Up, SingleOccupancyElevatorCostFunction))
    b2.elevators.get("b").map(_.isMoving) should contain (true)
  }

  test("requests are queued if all elevators are busy") {
    val b1: ElevatorBank = ElevatorBank.processRequest(initBank, FloorRequestMessage(3, Direction.Up, SingleOccupancyElevatorCostFunction))
    val b2: ElevatorBank = ElevatorBank.processRequest(b1, FloorRequestMessage(7, Direction.Up, SingleOccupancyElevatorCostFunction))
    val b3: ElevatorBank = ElevatorBank.processRequest(b2, FloorRequestMessage(5, Direction.Up, SingleOccupancyElevatorCostFunction))
    b3.elevators.get("a").map(_.isMoving) should contain (true)
    b3.elevators.get("b").map(_.isMoving) should contain (true)
    b3.requests.size shouldBe 1

    val b4: ElevatorBank = ElevatorBank.move(b3, 5)
    b4.requests shouldBe empty
    b4.elevators.get("a").toSeq.flatMap(_.requests) shouldBe empty
    b4.elevators.get("b").toSeq.flatMap(_.requests) shouldBe empty
  }
}
