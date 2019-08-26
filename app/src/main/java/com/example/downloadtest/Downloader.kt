package com.example.downloadtest

import android.os.Handler
import android.os.Looper
import android.util.Base64InputStream
import android.util.Log
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import kotlin.math.log

class Downloader(bean:DownloadBean,listener:DownloadListener) : DownloadProgress {

    var pause = false
    var start = false
    var cancel = false

    override fun onProgress(progress: Int) {}
    val cacheFiles = ArrayList<File>()
    val progresses = arrayOf(0,0,0,0)//用于进度
    val bean = bean
    val listener = listener
    val interceptor = DownloadInterceptor(this)
    var lenth = -1L
    var finishCount = 0
    var totalCount = 0
    val handler = Handler(Looper.getMainLooper())
    val httpClient:OkHttpClient = OkHttpClient.Builder().apply {
        readTimeout(10,TimeUnit.SECONDS)
        writeTimeout(10,TimeUnit.SECONDS)
        connectTimeout(3,TimeUnit.SECONDS)
        addInterceptor(interceptor)
    }.build()
    val retrofit:Retrofit = Retrofit.Builder()
        .client(httpClient)
        .baseUrl(bean.url)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()

    val downTmpFile:File = File(bean.path,"${bean.name}.tmp")
    val observer = object :Observer<Int>{
        override fun onComplete() {

        }

        override fun onSubscribe(d: Disposable) {

        }

        override fun onNext(t: Int) {
            finishCount++
            Log.d("文件$t","下载完成")
            if(finishCount==totalCount&&start){
                start = false
                listener.end()
                downTmpFile.renameTo(File(bean.path,bean.name))
                cleanCache()
            }
        }

        override fun onError(e: Throwable) {
            listener.error(e.toString())
        }
    }


    fun pause(){
        if(pause) return
        resetStatus()
        pause = true
    }

    fun cancel(){
        if(cancel) return
        resetStatus()
        cancel = true
        if(!start)
            cleanCache()
    }


    fun start(subscriber:Observer<Response<ResponseBody>>) {
        if(start) return
        resetStatus()
        start = true
        retrofit.create(Api::class.java).getDownloadSource("bytes=0--1",bean.name)
            .subscribeOn(Schedulers.io())
            .unsubscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
            .doOnNext{
                if(!downTmpFile.parentFile.exists()) downTmpFile.parentFile.mkdirs()
                val downTmpAccessFile = RandomAccessFile(downTmpFile,"rw")
                lenth = it.body()!!.contentLength()
                downTmpAccessFile.setLength(lenth)
                val avg = lenth/4
                totalCount = 3
                for(index in 0 .. 3){
                    if(index<3)
                        download(observer,index*avg,(index+1)*avg-1,index)
                    else
                        download(observer,index*avg,lenth-1,index)
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(subscriber)
    }

    private fun download(subscriber:Observer<Int>,start:Long,end:Long,index:Int){
        retrofit.create(Api::class.java).onDownloadSource("bytes=$start-$end",bean.name)
            .subscribeOn(Schedulers.io())
            .unsubscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .doOnNext {
                download(it.byteStream(),start,end,index,it.contentLength())
            }
            .map { index }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(subscriber)
    }

    private fun download(inputStream: InputStream,start:Long,end: Long,index:Int,lenth:Long){
        val cacheFile = File(bean.path,"Thread_${bean.name}$index")
        cacheFiles.add(cacheFile)
        var newStart = start
        val cacheAccessFile = RandomAccessFile(cacheFile,"rwd")
        if(cacheFile.exists()&&cacheFile.length()>0){
            newStart = cacheAccessFile.readLine().toLong()
        }

        val downloadFile = RandomAccessFile(downTmpFile,"rwd")
        downloadFile.seek(newStart)
        var len = 0
        var total = 0L
        var progress = 0L
        var bytes = ByteArray(1024)
        while ({len = inputStream.read(bytes);len}()>0) {
            if(pause){
                return
            }else if(cancel){
                cleanCache()
                return
            }
            downloadFile.write(bytes,0,len)
            total += len
            progress = newStart + total
            cacheAccessFile.seek(0)
            cacheAccessFile.write(progress.toString().toByteArray(Charsets.UTF_8))
            updateProgress(index,((progress-start)*100/lenth).toInt())
        }
    }

    private fun updateProgress(index:Int,progress: Int){
        progresses[index] = progress
        handler.post {
            when(index){
                0->listener.progress1(progress)
                1->listener.progress2(progress)
                2->listener.progress3(progress)
                3->listener.progress4(progress)
            }
            listener.downloadTotal((progresses[0]+progresses[1]+progresses[2]+progresses[3])/4)
        }
    }

    @Synchronized
    private fun cleanCache(){
        for(file in cacheFiles){
            if(file.exists())
                file.delete()
        }
        if(downTmpFile.exists())
            downTmpFile.delete()
        cacheFiles.clear()
        initProgress()
    }

    private fun resetStatus(){
        start = false
        pause = false
        cancel = false
        totalCount = 0
        finishCount = 0
        for(i in 0 until 3)
            progresses[i] = 0
    }

    private fun initProgress(){
        listener.progress1(0)
        listener.progress2(0)
        listener.progress3(0)
        listener.progress4(0)
        listener.downloadTotal(0)
    }
}