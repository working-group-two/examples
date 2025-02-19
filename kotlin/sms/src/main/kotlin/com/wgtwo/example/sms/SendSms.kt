/**
 * Copyright (C) 2025 Cisco Systems, Inc.
 */
package com.wgtwo.example.sms

import build.buf.gen.wgtwo.sms.v1.SmsServiceGrpcKt.SmsServiceCoroutineStub
import build.buf.gen.wgtwo.sms.v1.sendTextFromSubscriberRequest
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.wgtwo.auth.WgtwoAuth
import com.wgtwo.example.BaseConfig
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import java.util.concurrent.TimeUnit


/**
 * Send an SMS
 */
class SendSms : BaseConfig("bazel run //kotlin/sms:send --") {
    private val from: String by argument()
    private val to: String by argument()
    private val message: String by option().prompt("SMS Content")

    override suspend fun run() {
        println("[$target] Sending SMS from $from to $to with message: ${message}")

        val wgtwoAuth = WgtwoAuth.builder(clientId, clientSecret).build()
        val tokenSource = wgtwoAuth.clientCredentials.newTokenSource("sms.text:send_from_subscriber")
        val callCredentials = tokenSource.callCredentials()

        val channel: ManagedChannel = ManagedChannelBuilder.forTarget(target)
            .keepAliveWithoutCalls(true)
            .keepAliveTime(60, TimeUnit.SECONDS)
            .keepAliveTimeout(10, TimeUnit.SECONDS)
            .build()

        val stub = SmsServiceCoroutineStub(channel).withCallCredentials(callCredentials)

        val request = sendTextFromSubscriberRequest {
            fromSubscriber = from
            toAddress = to
            content = message
        }

        val response = stub.sendTextFromSubscriber(request)
        println(response)

        channel.shutdown().awaitTermination(10, TimeUnit.SECONDS)
    }
}

suspend fun main(args: Array<String>) = SendSms().main(args)
