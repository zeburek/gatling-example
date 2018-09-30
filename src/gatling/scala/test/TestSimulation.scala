package test

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class TestSimulation extends Simulation {
  val httpConf = http.baseURL("https://httpbin.org")

  val httpMethods = exec(
    http("Request GET")
      .get("/get")
  ).exec(
    http("Request POST")
      .post("/post")
  ).exec(
    http("Request PUT")
      .put("/put")
  ).exec(
    http("Request PATCH")
      .patch("/patch")
  ).exec(
    http("Request DELETE")
      .delete("/delete")
  )

  val scn = scenario("BasicSimulation")
    .exec(
      httpMethods
    )
    .pause(5)

  setUp(
    scn.inject(atOnceUsers(1))
  ).protocols(httpConf)
}