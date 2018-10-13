package test

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.response._
import scala.concurrent.duration._
import java.nio.charset.StandardCharsets.UTF_8

import test.utils.CustomAuth

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

  val settingBody = exec(
    http("Request POST: json body")
      .post("/post")
      .body(StringBody("""{"value":"test"}""")).asJSON
  ).exec(
    http("Request POST: form body")
      .post("/post")
      .formParam("first", "test1")
      .formParamSeq(Seq(("second", "test2"), ("third", "test3")))
      .formParamMap(Map("mapFirst" -> "red",
                        "mapSecond" -> "green",
                        "mapThird" -> "blue"))
      .multivaluedFormParam("multiValued", Seq(1, 2, 3))
  ).exec(
    http("Request POST: multi-part body")
      .post("/post")
      .bodyPart(StringBodyPart("name", "value").fileName("text.txt"))
  )

  val customHeaders = exec(
    http("Request GET: custom headers")
      .get("/get")
      .header("Accept-Language", "ru")
      .headers(Map(
        "Referer" -> "http://en.wikipedia.org/wiki/Main_Page",
        "User-Agent" -> "Mozilla/5.0 (X11; Linux i686; rv:2.0.1) Gecko/20100101 Firefox/4.0.1"
      ))
  )

  val verifyResponse = exec(
    http("Request POST: verify response")
      .post("/post")
      .header("Accept-Language", "ru")
      .queryParam("q", "nothing")
      .formParam("first", "test1")
      .check(status.is(200))
      .check(jsonPath("$.headers.Accept-Language").exists)
      .check(jsonPath("$.headers.Accept-Language").is("ru"))
      .check(jsonPath("$.args.q").exists)
      .check(jsonPath("$.args.q").is("nothing"))
      .check(jsonPath("$.form.first").exists)
      .check(jsonPath("$.form.first").is("test1"))
  ).exec(
    http("Request POST: on get endpoint")
      .post("/get")
      .check(status.is(405))
  ).exec(
    http("Request POST: verify json body")
      .post("/post")
      .body(StringBody("""{"value":"test"}""")).asJSON
      .check(jsonPath("$.json.value").exists)
      .check(jsonPath("$.json.value").is("test"))
  ).exec(
    http("Custom GET: verify XML body")
      .get("http://repo.merproject.org/obs/home:/Kaffeine:/mer/latest_armv7hl/repodata/repomd.xml")
      .check(xpath("//rpm:data[@type='filelists']",
             List("rpm" -> "http://linux.duke.edu/metadata/repo")).exists)
  )

  val customFeatures = exec(
    http("Request GET: with signature calculator")
      .get("/get")
      .signatureCalculator(new CustomAuth)
      .check(jsonPath("$.headers.Authorization").exists)
      .check(jsonPath("$.headers.Authorization").is("TESTGEThttps://httpbin.org/get"))
  ).exec(
    http("Request GET: transform response")
      .get("/get")
      .transformResponse{
        case response => 
        new ResponseWrapper(response) {
          override val body = new StringResponseBody("{}", UTF_8)
        }
      }
      .check(jsonPath("$.headers").notExists)
  ).exec(session => {
    session.set("testKey", "testValue")
  }).exec(
    http("Request GET: with new param from session")
      .get("/get")
      .queryParam("nothing", "${testKey}")
      .check(jsonPath("$.args.nothing").is("testValue"))
  )

  val scn = scenario("TestSimulation")
    .exec(
      httpMethods,
      settingBody,
      customHeaders,
      verifyResponse,
      customFeatures
    )
    .pause(5)
  
  val scnPosts = scenario("PostsSimulation")
    .exec(
      exec(ApiRequests.getAllPosts)
        .exec(ApiRequests.getRandomPost)
        .exec(ApiRequests.getUserPosts)
        .exec(ApiRequests.postNewCommentToRandomPost)
        .exec(ApiRequests.getNewComment)
    )

  setUp(
    scn.inject(
      constantUsersPerSec(3) during (5 minutes)
    ),
    scnPosts.inject(atOnceUsers(1))
  ).protocols(httpConf)
}
