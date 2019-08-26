package com.example.downloadtest

import okhttp3.Interceptor
import okhttp3.Response

class DownloadInterceptor(listener:DownloadProgress):Interceptor{
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        return response.newBuilder().body(DownloadBody(response.body!!,listener)).build()
    }

    val listener = listener
}