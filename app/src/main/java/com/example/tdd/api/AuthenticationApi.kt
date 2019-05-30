package com.example.tdd.api

import com.example.tdd.api.models.AuthenticationResponse
import io.reactivex.Single
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthenticationApi {

  @POST("/idp/api/v1/token")
  @FormUrlEncoded
  fun authenticate(
    @Field("username") username: String,
    @Field("password") password: String,
    @Field("grant_type") grantType: String = "password",
    @Field("client_id") clientId: String = CLIENT_ID
  ): Single<AuthenticationResponse>

  @POST("/idp/api/v1/token")
  @FormUrlEncoded
  fun extendAuthentication(
    @Field("refresh_token") refreshToken: String,
    @Field("grant_type") grantType: String = "refresh_token",
    @Field("client_id") clientId: String = CLIENT_ID
  ): Single<AuthenticationResponse>

  companion object {
    const val API_URL = "https://example.vividmindsoft.com"
    private const val CLIENT_ID = "69bfdce9-2c9f-4a12-aa7b-4fe15e1228dc"
  }

}