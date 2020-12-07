package com.taptosnap.api

import com.taptosnap.model.SnapItem
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * API client to call the main endpoint using Retrofit framework
 */
class SnapApiClient {
    val BASE_URL = "https://hoi4nusv56.execute-api.us-east-1.amazonaws.com/"
    val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun getAllLaunches() : Call<List<SnapItem>> {
        return retrofit.create(SnapApiService::class.java).getAllLaunches()
    }

    fun checkImage(imgLabel: String, imgEncoded: String, callback: (Boolean) -> Unit) : Call<Boolean> {
        return retrofit.create(SnapApiService::class.java).checkImage(imgLabel,imgEncoded)
    }
}