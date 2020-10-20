package com.bignerdranch.android.photogallery

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "ThumbnailDownloader"
private const val MESSAGE_DOWNLOAD = 0


class ThumbnailDownloader<in T>(private val responseHandler: Handler,
                                private val onThumbnailDownloaded: (T, Bitmap) -> Unit)
    : HandlerThread(TAG) {
    //fragment lifecycle observer
    val fragmentLifecycleObserver: LifecycleObserver =
        object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun setup() {
                Log.i(TAG, "Starting background thread")
                start()
                looper
            }
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun tearDown() {
                Log.i(TAG, "Destroying background thread")
                quit()
            }
        }

    //adding view lifecycle observer
    val viewLifecycleObserver: LifecycleObserver =
        object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun clearQueue() {
                Log.i(TAG, "Clearing all requests from queue")
                requestHandler.removeMessages(MESSAGE_DOWNLOAD)
                requestMap.clear()
            }
        }


    private var hasQuit = false
    //get bitmap info and recreate
    private lateinit var requestHandler: Handler
    private val requestMap = ConcurrentHashMap<T, String>()
    private val flickrFetchr = FlickrFetchr()
    //handling messages
    @Suppress("UNCHECKED_CAST")
    @SuppressLint("HandlerLeak")
    override fun onLooperPrepared() {
        requestHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    val target = msg.obj as T
                    Log.i(TAG, "Got a request for URL:${requestMap[target]}")
                    handleRequest(target)
                }
            }
        }
    }

    override fun quit(): Boolean {
        hasQuit = true
        return super.quit()
    }


    fun queueThumbnail(target: T, url: String) {
        Log.i(TAG, "Got a URL: $url")
        //getting and sending message
        requestMap[target] = url
        requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget()
    }

    private fun handleRequest(target: T) {
        val url = requestMap[target] ?: return
        val bitmap = flickrFetchr.fetchPhoto(url) ?: return

        //download and displaying images
        responseHandler.post(Runnable {
            if (requestMap[target] != url || hasQuit) {
                return@Runnable
            }
            requestMap.remove(target)
            onThumbnailDownloaded(target, bitmap)
        })

    }
}
