package test.utils

import org.asynchttpclient.{Request, RequestBuilderBase, SignatureCalculator}

class CustomAuth extends SignatureCalculator {

  override def calculateAndAddSignature(request: Request, requestBuilder: RequestBuilderBase[_]): Unit = {
    val url = request.getUrl
    val method = request.getMethod
    val authHeader = "TEST"+method+url
    requestBuilder.setHeader("Authorization", authHeader)
  }

}