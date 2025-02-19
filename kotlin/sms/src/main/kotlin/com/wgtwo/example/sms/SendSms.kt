/**
 * Copyright (C) 2025 Cisco Systems, Inc.
 */
package com.wgtwo.example.sms

import build.buf.gen.wgtwo.sms.v1.SmsServiceGrpcKt.SmsServiceCoroutineStub
import build.buf.gen.wgtwo.sms.v1.sendTextFromSubscriberRequest
import com.wgtwo.auth.WgtwoAuth
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import java.util.concurrent.TimeUnit

/**
 * Send an SMS
 */
suspend fun main() {
    // Setup OAuth2.0 client credentials
    val clientId = requireNotNull(System.getenv("CLIENT_ID")) { "You must set env CLIENT_ID" }
    val clientSecret = requireNotNull(System.getenv("CLIENT_SECRET")) { "You must set env CLIENT_SECRET" }

    val wgtwoAuth = WgtwoAuth.builder(clientId, clientSecret).build()
    val tokenSource = wgtwoAuth.clientCredentials.newTokenSource("sms.text:send_from_subscriber")
    val callCredentials = tokenSource.callCredentials()

    // Create a gRPC channel to the API Gateway
    val target = "sandbox.api.shamrock.wgtwo.com:443" // For sandbox
    //val target = "api.shamrock.wgtwo.com:443" // For EU
    //val target = "api.oak.wgtwo.com:443" // For US

    val channel: ManagedChannel = ManagedChannelBuilder.forTarget(target)
        .keepAliveWithoutCalls(true)
        .keepAliveTime(60, TimeUnit.SECONDS)
        .keepAliveTimeout(10, TimeUnit.SECONDS)
        .build()

    // Create stub for the SMS service
    val stub = SmsServiceCoroutineStub(channel).withCallCredentials(callCredentials)

    // Send an SMS
    val request = sendTextFromSubscriberRequest {
        fromSubscriber = "+4799001122"
        toAddress = "+4712345678"
        content = "Hello, world!"
    }

    val response = stub.sendTextFromSubscriber(request)
    println(response)

    // Shutdown the channel
    channel.shutdown().awaitTermination(10, TimeUnit.SECONDS)
}
