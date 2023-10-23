ThisBuild / organization := "DAPEX"

ThisBuild / version := "0.1.0"

lazy val commonSettings = Seq(
  scalaVersion := "2.13.10",
  libraryDependencies ++= Dependencies.all,
  scalacOptions ++= Scalac.options,
  resolvers += Resolver.githubPackages("TheDiscProg"),
  githubOwner := "TheDiscProg",
  githubRepository := "db-service",
  addCompilerPlugin(
    ("org.typelevel" %% "kind-projector" % "0.13.2").cross(CrossVersion.full)
  ),
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
  publishConfiguration := publishConfiguration.value.withOverwrite(true),
  publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)
)

lazy val base = (project in file("base"))
  .settings(
    commonSettings,
    name := "db-service-base",
    coverageExcludedPackages := Seq(
      "<empty>",
      ".*.entities.*"
    ).mkString(";")
  )

lazy val guardrail = (project in file("guardrail"))
  .settings(
    commonSettings,
    name := "db-service-guardrail",
    Compile / guardrailTasks := List(
      ScalaServer(
        file("swagger.yaml"),
        pkg = "dapex.guardrail",
        framework = "http4s",
        tracing = false,
        imports = List(
          "eu.timepit.refined.types.string.NonEmptyString"
        )
      )
    ),
    coverageExcludedPackages := Seq(
      "<empty>",
      ".*guardrail.*"
    ).mkString(";")
  )
  .dependsOn(base % "test->test; compile->compile")

lazy val root = (project in file("."))
  .enablePlugins(
    ScalafmtPlugin,
    JavaAppPackaging,
    UniversalPlugin,
    DockerPlugin
  )
  .settings(
    commonSettings,
    name := "db-service",
    Compile / doc / sources := Seq.empty,
    scalacOptions ++= Scalac.options,
    coverageExcludedPackages := Seq(
      "<empty>"
    ).mkString(";"),
    coverageExcludedFiles := Seq(
      "<empty>",
      ".*MainApp.*",
      ".*AppServer.*"
    ).mkString(";"),
    coverageFailOnMinimum := true,
    coverageMinimumStmtTotal := 92,
    coverageMinimumBranchTotal := 100,
    Compile / mainClass := Some("dapex.MainApp"),
    Docker / packageName := "db-service",
    Docker / dockerUsername := Some("ramindur"),
    Docker / defaultLinuxInstallLocation := "/opt/db-service",
    dockerBaseImage := "eclipse-temurin:17-jdk-jammy",
    dockerExposedPorts ++= Seq(8004),
    dockerExposedVolumes := Seq("/opt/docker/.logs", "/opt/docker/.keys")
  )
  .dependsOn(base % "test->test; compile->compile")
  .dependsOn(guardrail % "test->test; compile->compile")
  .aggregate(base, guardrail)

lazy val integrationTest = (project in file("it"))
  .enablePlugins(ScalafmtPlugin)
  .settings(
    commonSettings,
    name := "db-service-integration-test",
    publish / skip := true,
    libraryDependencies ++= Dependencies.it,
    parallelExecution := false
  )
  .dependsOn(base % "test->test; compile->compile")
  .dependsOn(guardrail % "test->test; compile->compile")
  .dependsOn(root % "test->test; compile->compile")
  .aggregate(base, guardrail, root)

// Put here as database repository tests may hang but remove for none db applications
parallelExecution := false

addCommandAlias("formatAll", ";scalafmt;test:scalafmt;integrationTest/test:scalafmt;")
addCommandAlias("testAll", ";clean;integrationTest/clean;test;integrationTest/test;")
addCommandAlias("cleanAll", ";clean;integrationTest/clean")
addCommandAlias("itTest", ";integrationTest/clean;integrationTest/test")
addCommandAlias("clntst", ";clean;scalafmt;test:scalafmt;test;")
addCommandAlias("cvrtst", ";clean;integrationTest/clean;scalafmt;integrationTest/test:scalafmt;test:scalafmt;coverage;test;integrationTest/test;coverageReport;")