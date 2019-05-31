package com.example.tdd.session

interface TokenStore {

  var accessToken: String?

  var refreshToken: String?

}