package com.example.tdd.api

import com.example.tdd.api.models.AuthenticationResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
  ): Call<AuthenticationResponse>

  fun extendAuthentication(
    @Field("refresh_token") refreshToken: String,
    @Field("grant_type") grantType: String = "refresh_token",
    @Field("client_id") clientId: String = CLIENT_ID
  ): Call<AuthenticationResponse>

  companion object {
    const val API_URL = "https://example.vividmindsoft.com"
    private const val CLIENT_ID = "69bfdce9-2c9f-4a12-aa7b-4fe15e1228dc"

    @Volatile private var instance: AuthenticationApi? = null

    // TODO: Dagger?

    fun getInstance(baseUrl: String = API_URL): AuthenticationApi =
      instance ?: Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AuthenticationApi::class.java).also {
          instance = it
        }
  }

}