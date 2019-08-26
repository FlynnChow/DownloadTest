package com.example.downloadtest

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.io.File
import java.util.jar.Manifest

/**
 * Create By 2019-9
 * RxJava+Retrofit+Okhttp3
 * 多线程断点文件下载
 */
class MainActivity : AppCompatActivity(),DownloadListener {
    private val url:String by lazy {"http://ftp-apk.pconline.com.cn/4537cf11477a9e02ed0d10a2735f7631/pub/download/201010/"}
    private val path:String by lazy { Environment.getExternalStorageDirectory().absolutePath + File.separator + "DownloadTest/" }
    private val name:String by lazy { "com.tencent.mm_7.0.5_1440.apk" }

    override fun start() {
        Toast.makeText(this,"开始下载",Toast.LENGTH_LONG).show()
    }

    override fun pause() {
        Toast.makeText(this,"暂停下载",Toast.LENGTH_LONG).show()
    }

    override fun cancel() {
        Toast.makeText(this,"取消下载",Toast.LENGTH_LONG).show()
    }

    override fun end() {
        Toast.makeText(this,"下载完成",Toast.LENGTH_LONG).show()
    }

    override fun error(msg: String) {
        Toast.makeText(this,msg,Toast.LENGTH_LONG).show()
    }

    override fun downloadTotal(progress: Int) {
        progress_download.progress = if(progress>=0||progress<=100)progress else 0
        text_download.text = progress.toString()
    }

    override fun progress1(progress: Int) {
        progress_thread1.progress = if(progress>=0||progress<=100)progress else 0
    }

    override fun progress2(progress: Int) {
        progress_thread2.progress = if(progress>=0||progress<=100)progress else 0
    }

    override fun progress3(progress: Int) {
        progress_thread3.progress = if(progress>=0||progress<=100)progress else 0
    }

    override fun progress4(progress: Int) {
        progress_thread4.progress = if(progress>=0||progress<=100)progress else 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val downloader = Downloader(DownloadBean(url,path,name,"com.tencent.mm_7.0.5_1440.apk"),this)
        start.setOnClickListener {
            Toast.makeText(this,"下载地址：$path",Toast.LENGTH_LONG).show()
            downloader.start(object : Observer<Response<ResponseBody>> {
            override fun onComplete() {

            }

            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(t: Response<ResponseBody>) {
                Log.d("成功","开始下载 code: ${t.code()}")
            }

            override fun onError(e: Throwable) {
                Log.d("错误！！", "${(( e as HttpException).response()!!.errorBody())}")
            }
        }) }
        pause.setOnClickListener { downloader.pause() }
        cancel.setOnClickListener { downloader.cancel() }

        if(PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),0)
        }
        if(PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)
        }
    }
}
