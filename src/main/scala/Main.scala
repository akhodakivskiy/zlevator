import scala.io.StdIn

/**
  * Created by anton on 5/30/17.
  */
object Main {
  import ElevatorBank._

  def main(args: Array[String]): Unit = {
    println(
      """
        |available commands:
        |
        |add <name> <min floor> <max floor>
        |request <floor <up|down> <greedy <elevator name>|single|shared]>
        |dispatch <name> <floor>
        |move
        |move <steps>
      """.stripMargin)

    var bank: ElevatorBank = ElevatorBank(Map.empty)

    print("> ")

    while (true) {
      val line = StdIn.readLine()

      val stateOpt: Option[ElevatorBankState[List[String]]] = line match {
        case AddElevator(a) => Some(ElevatorBank.add(a))
        case FloorRequest(r) => Some(ElevatorBank.request(r))
        case ElevatorDispatch(d) => Some(ElevatorBank.dispatch(d))
        case Move(m) => Some(ElevatorBank.move(m.floors))
        case "move" => Some(ElevatorBank.moveOne)
        case _ => None
      }

      stateOpt match {
        case Some(state) =>
          val (b, messages) = state.run(bank)
          messages.foreach(println)
          bank = b
        case None =>
          println(s"error: can't interpret command '$line'")
      }

      print("> ")
    }
  }
}
