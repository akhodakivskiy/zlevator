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
    ElevatorBank.request(FloorRequestMessage(3, Direction.Up, SingleOccupancyElevatorCostFunction)).exec(initBank).elevators.get("a").map(_.isMoving) should contain (true)
    ElevatorBank.request(FloorRequestMessage(7, Direction.Up, SingleOccupancyElevatorCostFunction)).exec(initBank).elevators.get("b").map(_.isMoving) should contain (true)
  }

  test("requests are queued if all elevators are busy") {
    val b1 = (for {
      m1 <- ElevatorBank.request(FloorRequestMessage(3, Direction.Up, SingleOccupancyElevatorCostFunction))
      m2 <- ElevatorBank.request(FloorRequestMessage(7, Direction.Up, SingleOccupancyElevatorCostFunction))
      m3 <- ElevatorBank.request(FloorRequestMessage(5, Direction.Up, SingleOccupancyElevatorCostFunction))
    } yield {
      m1 ::: m2 ::: m3
    }).exec(initBank)

    b1.elevators.get("a").map(_.isMoving) should contain (true)
    b1.elevators.get("b").map(_.isMoving) should contain (true)
    b1.requests.size shouldBe 1

    val b2: ElevatorBank = ElevatorBank.move(5).exec(b1)
    b2.requests shouldBe empty
    b2.elevators.get("a").toSeq.flatMap(_.requests) shouldBe empty
    b2.elevators.get("b").toSeq.flatMap(_.requests) shouldBe empty
  }
}
