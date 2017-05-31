import org.scalatest.{FunSuite, Matchers}

/**
  * Created by anton on 5/30/17.
  */
class SharedElevatorCostFunctionTest extends FunSuite with Matchers {
  val initElevators: Map[String, Elevator] = Map(
    "a" -> Elevator(ElevatorConfig("a", 0, 10), 0),
    "b" -> Elevator(ElevatorConfig("b", 0, 10), 10)
  )
  val initBank: ElevatorBank = ElevatorBank(initElevators)

  test("requests in the same direction") {
    val b1: ElevatorBank = ElevatorBank.processRequest(initBank, FloorRequestMessage(4, Direction.Up, SharedElevatorCostFunction))
    val b2: ElevatorBank = ElevatorBank.processRequest(b1, FloorRequestMessage(3, Direction.Up, SharedElevatorCostFunction))

    b2.requests shouldBe empty
    b2.elevators.get("a").map(_.requests.size) should contain (2)
    b2.elevators.get("b").map(_.requests.size) should contain (0)
  }

  test("requests in the opposite directions") {
    val b1: ElevatorBank = ElevatorBank.processRequest(initBank, FloorRequestMessage(4, Direction.Down, SharedElevatorCostFunction))
    val b2: ElevatorBank = ElevatorBank.processRequest(b1, FloorRequestMessage(3, Direction.Up, SharedElevatorCostFunction))

    b2.requests shouldBe empty
    b2.elevators.get("a").map(_.requests.size) should contain (1)
    b2.elevators.get("b").map(_.requests.size) should contain (1)
  }

  test("request without direction") {
    val b1: ElevatorBank = ElevatorBank.dispatch(initBank, ElevatorDispatchMessage("a", 4, None))
    val b2: ElevatorBank = ElevatorBank.processRequest(b1, FloorRequestMessage(3, Direction.Up, SharedElevatorCostFunction))

    b2.requests shouldBe empty
    b2.elevators.get("a").map(_.requests.size) should contain (2)
    b2.elevators.get("b").map(_.requests.size) should contain (0)
  }
}
