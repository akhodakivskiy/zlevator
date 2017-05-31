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
    val b = (for {
      m1 <- ElevatorBank.request(FloorRequestMessage(5, Direction.Up, costFunction))
      m2 <- ElevatorBank.moveOne
      m3 <- ElevatorBank.move(4)
      m4 <- ElevatorBank.dispatch(ElevatorDispatchMessage("a", 7, None))
      m5 <- ElevatorBank.move(2)
    } yield {
      m1 ::: m2 ::: m3 ::: (m4 :: m5)
    }).exec(initBank)

    b.elevators.get("a").map(_.isMoving) shouldBe Some(false)
    b.elevators.get("a").map(_.requests.size) shouldBe Some(0)
    b.elevators.get("b").map(_.isMoving) shouldBe Some(false)
    b.elevators.get("b").map(_.requests.size) shouldBe Some(0)
  }
}
