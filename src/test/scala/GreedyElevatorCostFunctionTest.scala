import org.scalatest.{FunSuite, Matchers}

/**
  * Created by anton on 5/30/17.
  */
class GreedyElevatorCostFunctionTest extends FunSuite with Matchers {
  val initElevators: Map[String, Elevator] = Map(
    "a" -> Elevator(ElevatorConfig("a", 0, 10), 0),
    "b" -> Elevator(ElevatorConfig("b", 0, 10), 0)
  )
  val initBank: ElevatorBank = ElevatorBank(initElevators)

  test("process request with greedy elevator cost function") {
    val costFunction = GreedyElevatorCostFunction("a")
    val b1 = ElevatorBank.processRequest(initBank, FloorRequestMessage(5, Direction.Up, costFunction))
    b1.elevators.get("a").map(_.isMoving) shouldBe Some(true)
    b1.elevators.get("a").map(_.requests.size) shouldBe Some(1)
    b1.elevators.get("b").map(_.isMoving) shouldBe Some(false)
    b1.elevators.get("b").map(_.requests.size) shouldBe Some(0)

    val b2: ElevatorBank = ElevatorBank.moveOne(b1)
    b2.elevators.get("a").map(_.isMoving) shouldBe Some(true)
    b2.elevators.get("a").map(_.requests.size) shouldBe Some(1)
    b2.elevators.get("b").map(_.isMoving) shouldBe Some(false)
    b2.elevators.get("b").map(_.requests.size) shouldBe Some(0)

    val b3: ElevatorBank = ElevatorBank.move(b2, 4)
    b3.elevators.get("a").map(_.isMoving) shouldBe Some(false)
    b3.elevators.get("a").map(_.requests.size) shouldBe Some(0)
    b3.elevators.get("b").map(_.isMoving) shouldBe Some(false)
    b3.elevators.get("b").map(_.requests.size) shouldBe Some(0)

    val b4: ElevatorBank = ElevatorBank.dispatch(b3, ElevatorDispatchMessage("a", 7, None))
    b4.elevators.get("a").map(_.isMoving) shouldBe Some(true)
    b4.elevators.get("a").map(_.requests.size) shouldBe Some(1)
    b4.elevators.get("b").map(_.isMoving) shouldBe Some(false)
    b4.elevators.get("b").map(_.requests.size) shouldBe Some(0)

    val b5: ElevatorBank = ElevatorBank.move(b4, 2)
    b5.elevators.get("a").map(_.isMoving) shouldBe Some(false)
    b5.elevators.get("a").map(_.requests.size) shouldBe Some(0)
    b5.elevators.get("b").map(_.isMoving) shouldBe Some(false)
    b5.elevators.get("b").map(_.requests.size) shouldBe Some(0)
  }
}
