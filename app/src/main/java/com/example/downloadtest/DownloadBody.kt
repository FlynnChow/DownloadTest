package com.example.downloadtest

import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.*

class DownloadBody(body:ResponseBody,listener:DownloadProgress):ResponseBody() {
    val body = body
    val listener = listener
    var bufferedSource:BufferedSource ?= null

    override fun contentLength(): Long {
        return body.contentLength()
    }

    override fun contentType(): MediaType? {
        return body.contentType()
    }

    override fun source(): BufferedSource {
        if(bufferedSource==null) bufferedSource = source(body.source()).buffer()
        return bufferedSource!!
    }

    private fun source(source: Source):Source{
        return object :ForwardingSource(source){
            var total = 0L
            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead =  super.read(sink, byteCount)
                listener.let {
                    total += if(bytesRead<0) 0 else bytesRead
                    val progress = ((total * 100)/contentLength() * 100).toInt()
                    //listener.onProgress(progress)
                    //在分线程下载情况下无法区分是哪个线程的进度
                }
                return bytesRead
            }
        }
    }
}