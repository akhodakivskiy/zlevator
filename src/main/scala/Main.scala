/**
  * Created by anton on 5/30/17.
  */
object Main {
  import ElevatorBank._

  def main(args: Array[String]): Unit = {
    val initElevators: Map[String, Elevator] = Map(
      "a" -> Elevator(ElevatorConfig("a", 0, 10), 0),
      "b" -> Elevator(ElevatorConfig("b", 0, 10), 0),
      "c" -> Elevator(ElevatorConfig("c", 0, 10), 0)
    )
    val initBank: ElevatorBank = ElevatorBank(initElevators)

    val (bank, messages) = (for {
      m1 <- request(FloorRequestMessage(5, Direction.Up, SharedElevatorCostFunction))
      m2 <- request(FloorRequestMessage(2, Direction.Up, SharedElevatorCostFunction))
      m3 <- request(FloorRequestMessage(8, Direction.Up, GreedyElevatorCostFunction("b")))
      m4 <- request(FloorRequestMessage(1, Direction.Up, SingleOccupancyElevatorCostFunction))
      m5 <- moveOne
      m6 <- dispatch(ElevatorDispatchMessage("c", 4, None))
      m7 <- move(10)
    } yield {
      m1 ::: m2 ::: m3 ::: m4 ::: m5 ::: (m6 :: m7)
    }).run(initBank)

    messages.foreach(println)
  }
}
