/**
 * Copyright (C) 2025 Cisco Systems, Inc.
 */
package com.wgtwo.example.smsbot

import build.buf.gen.wgtwo.sms.v1.SmsServiceGrpcKt.SmsServiceCoroutineStub
import build.buf.gen.wgtwo.sms.v1.sendTextFromSubscriberRequest
import com.wgtwo.auth.WgtwoAuth
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import java.util.concurrent.TimeUnit

suspend fun main() {
    sendSms()
}

suspend fun sendSms() {
    val channel: ManagedChannel = ManagedChannelBuilder.forTarget("api.shamrock.wgtwo.com:443")
        .keepAliveWithoutCalls(true)
        .keepAliveTime(60, TimeUnit.SECONDS)
        .keepAliveTimeout(10, TimeUnit.SECONDS)
        .build()

    val clientId = requireNotNull(System.getenv("CLIENT_ID")) { "You must set env CLIENT_ID" }
    val clientSecret = requireNotNull(System.getenv("CLIENT_SECRET")) { "You must set env CLIENT_SECRET" }

    val wgtwoAuth = WgtwoAuth.builder(clientId, clientSecret).build()
    val tokenSource = wgtwoAuth.clientCredentials.newTokenSource("sms.text:send_from_subscriber")
    val callCredentials = tokenSource.callCredentials()

    val stub = SmsServiceCoroutineStub(channel).withCallCredentials(callCredentials)

    val request = sendTextFromSubscriberRequest {
        this.fromSubscriber = "+4790658023"
        this.toAddress = "+4796632976"
        this.content = "Hello, world!"
    }

    val response = stub.sendTextFromSubscriber(request)
    println(
        """
        |========================
        |You sent a message
        |$response
        |
        """.trimMargin()
    )
}
