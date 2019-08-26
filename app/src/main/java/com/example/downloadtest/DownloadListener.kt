package com.example.downloadtest

interface DownloadListener {
    fun start()
    fun pause()
    fun cancel()
    fun error(msg:String)
    fun end()
    fun downloadTotal(progress:Int)
    fun progress1(progress:Int)
    fun progress2(progress:Int)
    fun progress3(progress:Int)
    fun progress4(progress:Int)
}
interface DownloadProgress{
    fun onProgress(progress:Int)
}