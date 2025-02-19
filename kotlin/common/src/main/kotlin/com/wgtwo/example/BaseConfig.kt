package com.wgtwo.example

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice


abstract class BaseConfig(name: String) : SuspendingCliktCommand(name) {
    protected val clientId: String by option(
        envvar = "CLIENT_ID",
        help = "[env: CLIENT_ID] OAuth 2.0 Client ID"
    ).required()

    protected val clientSecret: String by option(
        envvar = "CLIENT_SECRET",
        help = "[env: CLIENT_SECRET] OAuth 2.0 Client Secret"
    ).required()

    protected val target: String by option(
        "--endpoint",
        "-e",
        envvar = "ENDPOINT",
        help = "[env: ENDPOINT] API Endpoint"
    ).choice(
        "sandbox.api.shamrock.wgtwo.com:443",
        "api.shamrock.wgtwo.com:443",
        "api.oak.wgtwo.com:443",
        "api.sakura.wgtwo.com:443",
    ).default("sandbox.api.shamrock.wgtwo.com:443")
}
