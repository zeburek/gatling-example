package test

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object ApiRequests {

  val getAllPosts = http("Get all posts")
    .get(s"${Default.apiUrl}/posts")
    .check(jsonPath("$..id").findAll.saveAs("allPostsIds"))

  val getRandomPost = http("Get random post")
    .get(s"${Default.apiUrl}/posts/" + "${allPostsIds.random()}")
    .check(jsonPath("$..userId").find.saveAs("selectedUser"))

  val getUserPosts = http("Get user posts")
    .get(s"${Default.apiUrl}/posts")
    .queryParam("userId","${selectedUser}")
    .check(jsonPath("$..id").findAll.saveAs("allUserPostsIds"))

  val postNewCommentToRandomPost = http("Post new comment to random post")
    .post(s"${Default.apiUrl}/comments")
    .body(StringBody("""{"postId":"${allUserPostsIds.random()}", 
                     |"name":"Test name", "email":"test@exampl.com",
                     |"body":"Test body"}""".stripMargin))
    .check(jsonPath("$..id").find.saveAs("newCommentId"))

  val getNewComment = http("Get new comment")
    .get(s"${Default.apiUrl}/comments/" + "${newCommentId}")
    .check(status.is(200))
    .check(jsonPath("$.email").exists)
    .check(jsonPath("$.email").is("test@exampl.com"))
}