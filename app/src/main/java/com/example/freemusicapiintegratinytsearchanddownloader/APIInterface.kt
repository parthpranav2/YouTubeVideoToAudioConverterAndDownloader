package com.example.freemusicapiintegratinytsearchanddownloader

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface APIInterface {
    @GET("search")
    fun getVideoByName(
        @Query("q") query: String,
        @Query("limit") limit: Int = 10
    ): Call<SearchData>

    @GET("mp3")
    fun getURLofDownloadable(
        @Query("url") query: String
    ):Call<DownloadData>
}