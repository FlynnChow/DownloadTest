package com.example.downloadtest

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface Api {
    @Streaming
    @GET
    fun getDownloadSource(@Header("Range") header:String,@Url rul:String):Observable<Response<ResponseBody>>

    @Streaming
    @GET
    fun onDownloadSource(@Header("Range") header:String,@Url rul:String):Observable<ResponseBody>
}