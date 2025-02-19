package com.wgtwo.example

suspend fun Boolean.ifTrue(function: suspend () -> Unit) = this.also {
    if (this) function()
}
