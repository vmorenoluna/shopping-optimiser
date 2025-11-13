name := "shopping-optimiser"

version := "0.1.0-SNAPSHOT"

// This is now a pure Java project
crossPaths := false
autoScalaLibrary := false

// Determine OS for JavaFX classifiers
val osName = System.getProperty("os.name").toLowerCase
val platform = if (osName.contains("win")) "win"
              else if (osName.contains("mac")) "mac"
              else "linux"

libraryDependencies ++= Seq(
  // Timefold Solver
  "ai.timefold.solver" % "timefold-solver-core" % "1.14.0",

  // Logging
  "ch.qos.logback" % "logback-classic" % "1.4.14",

  // JavaFX for UI (platform-specific)
  "org.openjfx" % "javafx-controls" % "17.0.8" classifier platform,
  "org.openjfx" % "javafx-graphics" % "17.0.8" classifier platform,
  "org.openjfx" % "javafx-base" % "17.0.8" classifier platform,

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

// Set the default main class (console version)
Compile / mainClass := Some("com.shoppingoptimiser.WardrobeOptimizerApp")

// Define multiple main classes
Compile / discoveredMainClasses := Seq(
  "com.shoppingoptimiser.WardrobeOptimizerApp",
  "com.shoppingoptimiser.WardrobeOptimizerUI"
)

// Enable JUnit 5 testing
testOptions += Tests.Argument(jupiterTestFramework, "-v")
