package com.github.senocak.boilerplate.domain.dto

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonPropertyOrder(value = ["statusCode", "error", "variables"])
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
@JsonTypeName(value = "exception")
class ExceptionDto : BaseDto() {
    var statusCode = 200
    var error: OmaErrorMessageTypeDto? = null
    var variables: Array<String?> = arrayOf(String())

    @JsonPropertyOrder(value = ["id", "text"])
    class OmaErrorMessageTypeDto(val id: String? = null, val text: String? = null)
}
