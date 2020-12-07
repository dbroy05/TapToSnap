package com.taptosnap.api

import com.taptosnap.model.SnapItem
import retrofit2.Call
import retrofit2.http.*

interface SnapApiService {
    @GET("/iositems/items")
    fun getAllLaunches() : Call<List<SnapItem>>

    @FormUrlEncoded
    @POST("/iositems/items")
    fun checkImage(@Field("ImageLabel") imgLabel: String ,@Field("Image",encoded = true) imgEncodedString: String) : Call<Boolean>

}
