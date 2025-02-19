/**
 * Copyright (C) 2025 Cisco Systems, Inc.
 */
package com.wgtwo.example.consents

import build.buf.gen.wgtwo.common.v0.paginationRequest
import build.buf.gen.wgtwo.consents.v0.ConsentServiceGrpcKt
import build.buf.gen.wgtwo.consents.v0.listConsentsForProductRequest
import com.github.ajalt.clikt.command.main
import com.wgtwo.auth.WgtwoAuth
import com.wgtwo.example.BaseConfig
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds

class ListConsentsForProduct : BaseConfig("bazel run //kotlin/consents:list --") {

    override suspend fun run(): Unit {
        println("[$target] Starting...")

        val wgtwoAuth = WgtwoAuth.builder(clientId, clientSecret).build()
        val tokenSource = wgtwoAuth.clientCredentials.newTokenSource("")
        val callCredentials = tokenSource.callCredentials()

        val channel: ManagedChannel = ManagedChannelBuilder.forTarget(target)
            .keepAliveWithoutCalls(true)
            .keepAliveTime(60, TimeUnit.SECONDS)
            .keepAliveTimeout(10, TimeUnit.SECONDS)
            .build()

        val stub = ConsentServiceGrpcKt.ConsentServiceCoroutineStub(channel)
            .withCallCredentials(callCredentials)

        var nextPageToken = ""
        var pages = 0
        var count = 0
        do {
            val request = listConsentsForProductRequest {
                pagination = paginationRequest {
                    pageSize = 100
                    if (nextPageToken.isNotEmpty()) {
                        pageToken = nextPageToken
                    }
                }
            }
            val response = stub.listConsentsForProduct(request)

            response.consentsList.forEach { consent ->
                println(
                    """
                    |---
                    |Consent:
                    |$consent
                    """.trimMargin()
                )
            }
            nextPageToken = response.pagination.nextPageToken
            pages++
            count += response.consentsCount
            delay(100.milliseconds)
        } while (nextPageToken.isNotEmpty())

        channel.shutdown().awaitTermination(10, TimeUnit.SECONDS)
        println("[$target] Done: Fetch $pages pages with $count consents")
    }
}

suspend fun main(args: Array<String>) = ListConsentsForProduct().main(args)
