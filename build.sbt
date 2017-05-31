name := "zlevator"
version := "2.0"
scalaVersion := "2.12.2"
enablePlugins(JavaAppPackaging)
mainClass in Compile := Some("Main")

libraryDependencies ++= Seq(
  "org.scalaz"    %% "scalaz-core"  % "7.2.13"
, "org.scalatest" %% "scalatest"    % "3.0.1"    % "test"
)

