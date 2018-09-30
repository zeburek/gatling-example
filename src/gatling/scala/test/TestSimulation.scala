package test

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class TestSimulation extends Simulation {
    val httpConf = http.baseUrl("https://httpbin.org")

    val httpMethods = exec(
        http("Request GET")
            .get("/get")
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