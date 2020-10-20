package com.bignerdranch.android.photogallery.api

import com.bignerdranch.android.photogallery.FlickrResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface FlickrApi {
/*
    @GET(
        "services/rest/?method=flickr.interestingness.getList" +
                "&api_key=3fcf5929d5d254c966a403dcab986817" +
                "&format=json" +
                "&nojsoncallback=1" +
                "&extras=url_s"
    )

 */

    @GET("services/rest?method=flickr.interestingness.getList")
    fun fetchPhotos(): Call<FlickrResponse>

    //takes url as input to get download data
    @GET
    fun fetchUrlBytes(@Url url: String): Call<ResponseBody>

    //getting search query
    @GET("services/rest?method=flickr.photos.search")
    fun searchPhotos(@Query("text") query: String): Call<FlickrResponse>



}