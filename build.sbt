name := """bowling-scorer"""

version := "1.0"

scalaVersion := "2.12.4"

libraryDependencies +=  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.6"

libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.2"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"

libraryDependencies += "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % "1.1.6" % "test"