package com.smile.aidlmusicserviceapp.aidlservice

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.RemoteCallbackList
import android.os.RemoteException
import android.util.Log
// import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.smile.aidlmusicserviceapp.Constants
import com.smile.aidlmusicserviceapp.IMusicService
import com.smile.aidlmusicserviceapp.IMusicServiceCallback
import com.smile.aidlmusicserviceapp.R

class MusicService : Service() {
    companion object {
        private const val TAG : String = "MusicService"
    }

    /**
     * This is a list of callbacks that have been registered with the
     * service.  Note that this is package scoped (instead of private) so
     * that it can be accessed more efficiently from inner classes.
     */
    val serviceCallbacks: RemoteCallbackList<IMusicServiceCallback> =
        RemoteCallbackList<IMusicServiceCallback>()

    private var mediaPlayer : MediaPlayer? = null

    private val binder = object : IMusicService.Stub() {
        var isLoaded : Boolean = false
        var isPlaying : Boolean = false

        override fun playMusic(): Int {
            var result: Int = Constants.ErrorCode
            mediaPlayer?.let {
                if (!isPlaying) {
                    it.start()
                    result = Constants.MusicPlaying
                    isPlaying = true
                }
            }
            Log.d(TAG, "playMusic.result = $result")
            broadcastResult(result)
            return result
        }

        override fun pauseMusic(): Int {
            var result = Constants.ErrorCode
            mediaPlayer?.let {
                Log.d(TAG, "mediaPlayer not null")
                if (isPlaying) {
                    Log.d(TAG,"mediaPlayer.isPlaying() is true")
                    it.pause()
                    result = Constants.MusicPaused
                    isPlaying = false
                }
            }
            Log.d(TAG, "pauseMusic.result = $result")
            broadcastResult(result)
            return result
        }

        override fun stopMusic(): Int {
            var result = Constants.ErrorCode
            mediaPlayer?.let {
                it.stop()
                result = Constants.MusicStopped
                isPlaying = false
            }
            Log.d(TAG, "stopMusic.result = $result")
            broadcastResult(result)
            return result
        }

        override fun isMusicLoaded(): Boolean = isLoaded

        override fun isMusicPlaying(): Boolean = isPlaying

        override fun registerCallback(cb: IMusicServiceCallback?) {
            Log.d(TAG, "registerCallback")
            cb?.let {
                serviceCallbacks.register(it)
            }
        }

        override fun unregisterCallback(cb: IMusicServiceCallback?) {
            Log.d(TAG, "unregisterCallback")
            cb?.let {
                serviceCallbacks.unregister(it)
            }
        }
    }

    inner class MessageHandler(looper: Looper): Handler(looper) {
        override fun handleMessage(msg: Message) {
            val result = msg.what
            callCallback(result)
            /*
            val n: Int = serviceCallbacks.beginBroadcast()
            Log.d(TAG, "MessageHandler.Broadcast message to all clients.n = $n")
            var i = 0
            while (i < n) {
                Log.d(TAG, "MessageHandler.Broadcast message.i = $i")
                try {
                    serviceCallbacks.getBroadcastItem(i).valueChanged(msg.what)
                } catch (e: RemoteException) {
                    // The RemoteCallbackList will take care of removing
                    // the dead object for us.
                    Log.d(TAG, "MessageHandler.Failed to broadcast")
                }
                i++
            }
            serviceCallbacks.finishBroadcast()
            */
        }
    }

    /**
     * Our Handler used to execute operations on the main thread.  This is used
     * to schedule increments of our value.
     */
    private lateinit var mMessageHandler: Handler

    override fun onCreate() {
        Log.d(TAG, "onCreate()")
        val handlerThread = HandlerThread("CallbackHandlerThread")
        handlerThread.start()
        mMessageHandler = MessageHandler(handlerThread.looper)
        loadMusic()
        super.onCreate()
    }

    override fun onBind(p0: Intent?): IBinder? {
        Log.d(TAG, "onBind")
        broadcastResult(Constants.ServiceBound)
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind")
        binder.pauseMusic()
        broadcastResult(Constants.ServiceUnbound)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        serviceCallbacks.kill()
        mMessageHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun loadMusic(): Int {
        binder.isPlaying = false
        var result = Constants.ErrorCode
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(applicationContext, R.raw.music_a)
        }
        mediaPlayer?.let {
            it.isLooping = true
            result = Constants.MusicLoaded
            binder.isLoaded = true
        }
        Log.d(TAG, "loadMusic.result = $result")
        broadcastResult(result)
        return result
    }

    private fun broadcastResult(result: Int) {
        Log.d(TAG, "broadcastResult.result = $result")
        callCallback(result)
        // or use mMessageHandler
        // val message : Message = Message.obtain(null, result, 0, 0)
        // mMessageHandler.sendMessage(message)
    }

    private fun callCallback(result: Int) {
        val n: Int = serviceCallbacks.beginBroadcast()
        Log.d(TAG, "callCallback.Broadcast to all clients.n = $n")
        var i = 0
        while (i < n) {
            Log.d(TAG, "callCallback.Broadcast i = $i")
            try {
                serviceCallbacks.getBroadcastItem(i).valueChanged(result)
            } catch (e: RemoteException) {
                // The RemoteCallbackList will take care of removing
                // the dead object for us.
                Log.e(TAG, "callCallback.Failed to broadcast", e)
            }
            i++
        }
        serviceCallbacks.finishBroadcast()
    }
}