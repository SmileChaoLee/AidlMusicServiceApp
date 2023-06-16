package com.smile.aidlserviceapp.aidlservice

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.smile.aidlserviceapp.Constants
import com.smile.aidlserviceapp.R
import com.smile.aidlserviceapp.aidl.IMusicAidlInterface

class MusicService : Service() {
    companion object {
        private const val TAG : String = "MusicService"
    }
    private var mediaPlayer : MediaPlayer? = null

    private val binder = object : IMusicAidlInterface.Stub() {
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
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate()")
        super.onCreate()
        loadMusic()
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
        return result
    }

    private fun broadcastResult(result: Int) {
        Log.d(TAG, "broadcastResult")
        val broadcastIntent = Intent(Constants.ServiceName)
        val extras = Bundle()
        extras.putInt(Constants.Result, result)
        broadcastIntent.putExtras(extras)
        val localBroadcastManager = LocalBroadcastManager.getInstance(
            baseContext
        )
        localBroadcastManager.sendBroadcast(broadcastIntent)
    }
}