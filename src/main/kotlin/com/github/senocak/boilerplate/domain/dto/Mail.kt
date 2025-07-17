package com.github.senocak.boilerplate.domain.dto

data class Mail(
    var from: String? = null,
    var to: String? = null,
    var cc: String? = null,
    var bcc: String? = null,
    var subject: String? = null,
    var content: String? = null,
    var contentType: String = "text/html"
)
