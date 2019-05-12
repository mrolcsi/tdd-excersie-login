package com.example.tdd.api.models

import com.google.gson.annotations.SerializedName

data class AuthenticationResponse(
  @SerializedName("access_token") val accessToken: String,
  @SerializedName("token_type") val tokenType: String,
  @SerializedName("expired_in") val expiresIn: Int,
  @SerializedName("refresh_token") val refreshToken: String
)