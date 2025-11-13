name := "shopping-optimiser"

version := "0.1.0-SNAPSHOT"

// This is now a pure Java project
crossPaths := false
autoScalaLibrary := false

libraryDependencies ++= Seq(
  // Timefold Solver
  "ai.timefold.solver" % "timefold-solver-core" % "1.14.0",

  // Logging
  "ch.qos.logback" % "logback-classic" % "1.4.14",

  // Testing
  "org.junit.jupiter" % "junit-jupiter-api" % "5.10.1" % Test,
  "org.junit.jupiter" % "junit-jupiter-engine" % "5.10.1" % Test,
  "net.aichler" % "jupiter-interface" % "0.11.1" % Test,
  "ai.timefold.solver" % "timefold-solver-test" % "1.14.0" % Test
)

// Java configuration
javacOptions ++= Seq("-source", "11", "-target", "11")

// Java options for running
fork := true
javaOptions ++= Seq(
  "-Xms512m",
  "-Xmx2g"
)

// Set the main class
Compile / mainClass := Some("com.shoppingoptimiser.WardrobeOptimizerApp")

// Enable JUnit 5 testing
testOptions += Tests.Argument(jupiterTestFramework, "-v")
