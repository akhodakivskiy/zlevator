import org.scalatest.{FunSuite, Matchers}

/**
  * Created by anton on 5/30/17.
  */
class ElevatorTest extends FunSuite with Matchers {
  val config = ElevatorConfig("a", 0, 10)
  val initElevator = Elevator(config, 0)

  test("basic dispatch and move") {
    val e = (for {
      m1 <- Elevator.dispatch(ElevatorDispatch("a", 5, None))
      m2 <- Elevator.moveOne
      m3 <- Elevator.move(4)
    } yield {
      m1 :: m2.toList ::: m3
    }).exec(initElevator)

    e.requests shouldBe empty
  }

  test("move with multiple dispatches") {
    val e = (for {
      m1 <- Elevator.dispatch(ElevatorDispatch("a", 10, None))
      m2 <- Elevator.dispatch(ElevatorDispatch("a", 5, None))
      m3 <- Elevator.move(10)
    } yield {
      m1 :: m2.toList ::: m3
    }).exec(initElevator)

    e.isMoving shouldBe false
    e.requests shouldBe empty
  }
}
