package test.utils

import io.gatling.http.client.{Request, SignatureCalculator}

class CustomAuth extends SignatureCalculator {

  override def sign(request: Request): Unit = {
    val url = request.getUri
    val method = request.getMethod
    val authHeader = "TEST" + method + url
    request.getHeaders.add("Authorization", authHeader)
  }

}