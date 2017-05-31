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
    val b = (for {
      m1 <- ElevatorBank.request(FloorRequest(4, Direction.Up, SharedElevatorCostFunction))
      m2 <- ElevatorBank.request(FloorRequest(3, Direction.Up, SharedElevatorCostFunction))
    } yield {
      m1 :: m2
    }).exec(initBank)

    b.requests shouldBe empty
    b.elevators.get("a").map(_.requests.size) should contain (2)
    b.elevators.get("b").map(_.requests.size) should contain (0)
  }

  test("requests in the opposite directions") {
    val b = (for {
      m1 <- ElevatorBank.request(FloorRequest(4, Direction.Down, SharedElevatorCostFunction))
      m2 <- ElevatorBank.request(FloorRequest(3, Direction.Up, SharedElevatorCostFunction))
    } yield {
      m1 :: m2
    }).exec(initBank)

    b.requests shouldBe empty
    b.elevators.get("a").map(_.requests.size) should contain (1)
    b.elevators.get("b").map(_.requests.size) should contain (1)
  }

  test("request without direction") {
    val b = (for {
      m1 <- ElevatorBank.dispatch(ElevatorDispatch("a", 4, None))
      m2 <- ElevatorBank.request(FloorRequest(3, Direction.Up, SharedElevatorCostFunction))
    } yield {
      m1 :: m2
    }).exec(initBank)

    b.requests shouldBe empty
    b.elevators.get("a").map(_.requests.size) should contain (2)
    b.elevators.get("b").map(_.requests.size) should contain (0)
  }
}
