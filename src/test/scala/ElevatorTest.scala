import org.scalatest.{FunSuite, Matchers}

/**
  * Created by anton on 5/30/17.
  */
class ElevatorTest extends FunSuite with Matchers {
  val config = ElevatorConfig("a", 0, 10)
  val initElevator = Elevator(config, 0)

  test("basic dispatch and move") {
    val e1: Elevator = Elevator.dispatch(initElevator, ElevatorDispatchMessage("a", 5, None))
    e1.isMoving shouldBe true
    e1.requests.headOption shouldBe Some(ElevatorRequest(5, None))

    val e2: Elevator = Elevator.moveOne(e1)
    e2.lastFloor shouldBe 1
    e2.isMoving shouldBe true

    val e3: Elevator = Elevator.move(e2, 4)
    e3.lastFloor shouldBe 5
    e3.isMoving shouldBe false

    // dispatching to current floor doesn't change anything
    Elevator.dispatch(initElevator, ElevatorDispatchMessage("a", 0, None)) shouldBe initElevator

    // make sure elevator name is correct in the dispatch message
    assertThrows[IllegalArgumentException] {
      Elevator.dispatch(initElevator, ElevatorDispatchMessage("b", 5, None))
    }

    // can't dispatch beyond the elevator floor bounds
    assertThrows[IllegalArgumentException] {
      Elevator.dispatch(initElevator, ElevatorDispatchMessage("a", 15, None))
    }
  }

  test("move with multiple dispatches") {
    val e1: Elevator = Elevator.dispatch(initElevator, ElevatorDispatchMessage("a", 10, None))
    val e2: Elevator = Elevator.dispatch(e1, ElevatorDispatchMessage("a", 5, None))
    val e3: Elevator = Elevator.move(e2, 10)

    e3.isMoving shouldBe false
    e3.requests shouldBe empty
  }
}
