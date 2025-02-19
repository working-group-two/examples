/**
 * Copyright (C) 2025 Cisco Systems, Inc.
 */
package com.wgtwo.example.sms

import build.buf.gen.wgtwo.events.v0.*
import com.wgtwo.auth.WgtwoAuth
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Status
import io.grpc.StatusException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.retryWhen
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

/**
 * Receive SMS events
 */
suspend fun main() {
    // Setup OAuth2.0 client credentials
    val clientId = requireNotNull(System.getenv("CLIENT_ID")) { "You must set env CLIENT_ID" }
    val clientSecret = requireNotNull(System.getenv("CLIENT_SECRET")) { "You must set env CLIENT_SECRET" }

    val wgtwoAuth = WgtwoAuth.builder(clientId, clientSecret).build()
    val tokenSource = wgtwoAuth.clientCredentials.newTokenSource("events.sms.subscribe")
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

    // Create stub for the events service
    val stub = EventsServiceGrpcKt.EventsServiceCoroutineStub(channel).withCallCredentials(callCredentials)

    val request = subscribeEventsRequest {
        this.type += EventType.SMS_EVENT
        this.maxInFlight = 10
        this.manualAck = manualAckConfig {
            this.enable = true
        }
    }
    println("Subscribing to SMS events...")
    stub.subscribe(request)
        .retryWhen { cause, attempt ->
            if (cause is StatusException && cause.status.code != Status.Code.UNAUTHENTICATED) {
                println("Stream error: ${cause.status}. Retrying attempt $attempt...")
                // Exponential backoff with a maximum of 10 seconds
                val wait = (1 * attempt).seconds.coerceAtMost(10.seconds)
                delay(wait)
                true
            } else {
                println("Stream error: $cause - Not retrying")
                false
            }
        }.collect { response ->
            println("Received event: $response")

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
