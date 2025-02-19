/**
 * Copyright (C) 2025 Cisco Systems, Inc.
 */
package com.wgtwo.example.consents

import build.buf.gen.wgtwo.consents.v1.ConsentEventServiceGrpcKt
import build.buf.gen.wgtwo.consents.v1.ackConsentChangeEventRequest
import build.buf.gen.wgtwo.consents.v1.streamConsentChangeEventsRequest
import build.buf.gen.wgtwo.events.v1.regularStream
import build.buf.gen.wgtwo.events.v1.streamConfiguration
import com.github.ajalt.clikt.command.main
import com.google.protobuf.empty
import com.wgtwo.auth.WgtwoAuth
import com.wgtwo.example.BaseConfig
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Status
import io.grpc.StatusException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retryWhen
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

/**
 * Stream Consent Events
 */
class StreamConsentEvents : BaseConfig("bazel run //kotlin/consents:stream --") {
    override suspend fun run() {
        println("[$target] Starting...")

        val wgtwoAuth = WgtwoAuth.builder(clientId, clientSecret).build()
        val tokenSource = wgtwoAuth.clientCredentials.newTokenSource("")
        val callCredentials = tokenSource.callCredentials()

        val channel: ManagedChannel = ManagedChannelBuilder.forTarget(target)
            .keepAliveWithoutCalls(true)
            .keepAliveTime(60, TimeUnit.SECONDS)
            .keepAliveTimeout(10, TimeUnit.SECONDS)
            .build()

        val stub = ConsentEventServiceGrpcKt.ConsentEventServiceCoroutineStub(channel)
            .withCallCredentials(callCredentials)


        val request = streamConsentChangeEventsRequest {
            streamConfiguration = streamConfiguration {
                maxInFlight = 10
                startAtNew = empty {}
                regular = regularStream {}
            }
        }
        stub.streamConsentChangeEvents(request)
            .onStart { println("Subscribing to Consent events...") }
            .retryWhen { cause, attempt ->
                println("[retry attempt $attempt] Got error: $cause")
                // Retry if this is a gRPC error and that error is not UNAUTHENTICATED or PERMISSION_DENIED
                val shouldRetry = cause is StatusException &&
                        cause.status.code != Status.Code.UNAUTHENTICATED &&
                        cause.status.code != Status.Code.PERMISSION_DENIED
                if (shouldRetry) {
                    // Exponential backoff with a maximum of 60 seconds
                    delay(attempt.times(2).seconds.coerceAtMost(60.seconds))
                }
                shouldRetry
            }
            .collect { response ->
                println("Received event:")
                println(response)

                val ackRequest = ackConsentChangeEventRequest {
                    ackInfo = response.metadata.ackInfo
                }
                stub.ackConsentChangeEvent(ackRequest)
                println("Acknowledged event")
            }
    }
}

suspend fun main(args: Array<String>) = StreamConsentEvents().main(args)
