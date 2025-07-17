package com.github.senocak.boilerplate.domain.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.github.senocak.boilerplate.util.validation.PasswordMatches
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size

@JsonPropertyOrder("user", "token")
data class UserWrapperResponse(
    @JsonProperty("user")
    @Schema(required = true)
    var userResponse: UserResponse,

    @Schema(example = "eyJraWQiOiJ...", description = "Jwt Token", required = true, name = "token", type = "String")
    var token: String? = null
): BaseDto()

@JsonPropertyOrder("name", "username", "email", "roles")
class UserResponse(
    @JsonProperty("name")
    @Schema(example = "Lorem Ipsum", description = "Name of the user", required = true, name = "name", type = "String")
    var name: String,

    @Schema(example = "lorem@ipsum.com", description = "Email of the user", required = true, name = "email", type = "String")
    var email: String,

    @Schema(example = "asenocak", description = "Username of the user", required = true, name = "username", type = "String")
    var username: String,

    @ArraySchema(schema = Schema(example = "ROLE_USER", description = "Roles of the user", required = true, name = "roles"))
    var roles: List<RoleResponse>
): BaseDto()

@PasswordMatches
data class UpdateUserDto(
    @Schema(example = "Anil", description = "Name", required = true, name = "name", type = "String")
    @field:Size(min = 4, max = 40)
    var name: String? = null,

    @Schema(example = "Anil123", description = "Password", required = true, name = "password", type = "String")
    @field:Size(min = 6, max = 20)
    var password: String? = null,

    @Schema(example = "Anil123", description = "Password confirmation", required = true, name = "password", type = "String")
    @field:Size(min = 6, max = 20)
    var password_confirmation: String? = null
): BaseDto()


