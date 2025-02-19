/**
 * Copyright (C) 2025 Cisco Systems, Inc.
 */
package com.wgtwo.example.sms

import build.buf.gen.wgtwo.events.v0.*
import com.github.ajalt.clikt.command.main
import com.wgtwo.auth.WgtwoAuth
import com.wgtwo.example.BaseConfig
import com.wgtwo.example.ifTrue
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
 * Receive SMS events
 */
class ReceiveSms : BaseConfig("bazel run //kotlin/sms:receive --") {
    override suspend fun run() {
        println("[$target] Starting...")

        val wgtwoAuth = WgtwoAuth.builder(clientId, clientSecret).build()
        val tokenSource = wgtwoAuth.clientCredentials.newTokenSource("events.sms.subscribe")
        val callCredentials = tokenSource.callCredentials()

        val channel: ManagedChannel = ManagedChannelBuilder.forTarget(target)
            .keepAliveWithoutCalls(true)
            .keepAliveTime(60, TimeUnit.SECONDS)
            .keepAliveTimeout(10, TimeUnit.SECONDS)
            .build()

        // Create stub for the events service
        val stub = EventsServiceGrpcKt.EventsServiceCoroutineStub(channel).withCallCredentials(callCredentials)

        val request = subscribeEventsRequest {
            this.type += EventType.SMS_EVENT
            this.maxInFlight = 10
            this.manualAck = manualAckConfig {
                this.enable = true
            }
        }
        stub.subscribe(request)
            .onStart { println("Subscribing to SMS events...") }
            .retryWhen { cause, attempt ->
                println("[retry attempt $attempt] Got error: $cause")
                // Retry if this is a gRPC error and that error is not UNAUTHENTICATED or PERMISSION_DENIED
                val shouldRetry = cause is StatusException &&
                        cause.status.code != Status.Code.UNAUTHENTICATED &&
                        cause.status.code != Status.Code.PERMISSION_DENIED
                shouldRetry.ifTrue {
                    // Exponential backoff with a maximum of 60 seconds
                    delay(attempt.times(2).seconds.coerceAtMost(60.seconds))
                }
            }
            .collect { response ->
                println("Received event:")
                println(response)

                val ackRequest = ackRequest {
                    inbox = response.event.metadata.ackInbox
                    sequence = response.event.metadata.sequence
                }
                stub.ack(ackRequest)
                println("Acknowledged event")
            }

        // Shutdown the channel
        channel.shutdown().awaitTermination(10, TimeUnit.SECONDS)
    }
}

suspend fun main(args: Array<String>) = ReceiveSms().main(args)
