# Elevator routing system

This implementation imitates scheduling system for a bank of elevators. Elevator can be requested to a given floor and further dispatched to a destination floor.

## Implementation details

The imlementation is based on immutabe case classes. `Elevator` class represents the state of an elevator with a queue of destinations. `Elevator` can be dispatched to a floor. After dispatching the `Elevator` can be followed thru as it completes the dispatch.

```scala
case class Elevator(config: ElevatorConfig, lastFloor: Int, requests: Queue[ElevatorRequest])
```

`ElevatorBank` class represent a bank of elevators. One can request an elevator from the bank to a given floor mimicking people pressing up/down buttons outside of the elevators. The floor requests are queued while all the elevators are occupied and are dispatched as they become vacant.

```scala
case class ElevatorBank(elevators: Map[String, Elevator], requests: Queue[FloorRequestMessage])
```

Requesting the elevator to a given floor requires an implementation of `ElevatorCostFunction`. This cost function is mean to estimate relative cost for a given elevator to travel to the requested floor. Various implementations allow to customize the behavior of the elevator bank. An implementation geared toward efficiency can allow to minimize elevator travel time, or to dispatch particular elevator to the requester.

Provided are 3 implementations of the elevator cost function:

* `GreedyElevatorCostFunction` - will always dispatch elevator with given name.
* `SingleOccupancyElevatorCostFunctiona` - will dispatch nearest elevator and will make sure that requests are fulfilled sequentially (elevators won't be shared by multiple requestors)
* `SharedElevatorCostFunction` - will dispatch nearest elevator and will try to fulfill additional requests along the way

```scala
trait ElevatorCostFunction {
    def cost(elevator: Elevator, dispatch: ElevatorDispatchMessage): Option[Int]
}
```

### Initial implementation

Inital implementation https://github.com/akhodakivskiy/zlevator/commit/2862e70cd944529e445ee9a4b70993aad4159b99 consists of a series of functions that return updated instances of domain objects. The problem with this implementation is that the interface is hard to use and there is no meaningful feedback about the state of the system and the events that occur along the way.

```scala
bank.request(FloorRequestMessage(5, Direction.Up, SharedElevatorCostFunction))
    .request(FloorRequestMessage(2, Direction.Up, SharedElevatorCostFunction))
    .request(FloorRequestMessage(8, Direction.Up, GreedyElevatorCostFunction("b")))
    .request(FloorRequestMessage(1, Direction.Up, SingleOccupancyElevatorCostFunction))
    .moveOne
    .dispatch(ElevatorDispatchMessage("c", 4, None))
    .move(10)
```

### Scalaz based implementation

Improved imlementation is based on Scalaz `State` monad. This implementation allows to use more compact DSL as well as receive messages about the changes and events in the system.

```scala
val (bank, messages) = (for {
    m1 <- request(FloorRequestMessage(5, Direction.Up, SharedElevatorCostFunction))
    m2 <- request(FloorRequestMessage(2, Direction.Up, SharedElevatorCostFunction))
    m3 <- request(FloorRequestMessage(8, Direction.Up, GreedyElevatorCostFunction("b")))
    m4 <- request(FloorRequestMessage(1, Direction.Up, SingleOccupancyElevatorCostFunction))
    m5 <- moveOne
    m6 <- dispatch(ElevatorDispatchMessage("c", 4, None))
    m7 <- move(10)
} yield {
    m1 ::: m2 ::: m3 ::: m4 ::: m5
}).run(initBank)
```

This program will produce the following output

```
request to 5 with up direction has been queued
elevator a dispatched to floor 5
request to 2 with up direction has been queued
elevator a dispatched to floor 2
request to 8 with up direction has been queued
elevator b dispatched to floor 8
request to 1 with up direction has been queued
elevator c dispatched to floor 1
elevator c filled 1 at floor 1
elevator c dispatched to floor 4
elevator a filled 1 at floor 2
elevator c filled 1 at floor 4
elevator a filled 1 at floor 5
elevator b filled 1 at floor 8
```
