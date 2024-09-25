package com.smile.aidlmusicserviceapp

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.IBinder.DeathRecipient
import android.os.RemoteException
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.smile.aidlmusicserviceapp.databinding.ActivityMainBinding
import com.smile.aidlmusicserviceapp.model.RecentStatus

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }
    private lateinit var binding : ActivityMainBinding
    private lateinit var receiver: BroadcastReceiver

    private val serviceCallback = object : IMusicServiceCallback.Stub() {
        override fun valueChanged(value: Int) {
            Log.d(TAG, "serviceCallback.valueChanged.value = $value")
            RecentStatus.status.serverText = when (value) {
                Constants.ServiceStarted -> {
                    Log.d(TAG, "serviceCallback.ServiceStarted received")
                    "ServiceStarted received"
                }

                Constants.ServiceBound -> {
                    Log.d(TAG, "serviceCallback.ServiceBound received")
                    "ServiceBound received"
                }

                Constants.ServiceUnbound -> {
                    Log.d(TAG, "serviceCallback.ServiceUnbound received")
                    "ServiceUnbound received"
                }

                Constants.ServiceStopped -> {
                    Log.d(TAG, "serviceCallback.ServiceStopped received")
                    "ServiceStopped received"
                }

                Constants.MusicPlaying -> {
                    Log.d(TAG, "serviceCallback.MusicPlaying received")
                    if (isServiceBound) {
                        RecentStatus.status.messageText = "Music playing."
                        RecentStatus.status.playResult = Constants.ErrorCode
                        RecentStatus.status.pauseResult = Constants.PauseMusic
                    }
                    "MusicPlaying received"
                }

                Constants.MusicPaused -> {
                    Log.d(TAG, "serviceCallback.MusicPaused received")
                    if (isServiceBound) {
                        RecentStatus.status.messageText = "Music paused."
                        RecentStatus.status.playResult = Constants.PlayMusic
                        RecentStatus.status.pauseResult = Constants.ErrorCode
                    }
                    "MusicPaused received"
                }

                Constants.MusicStopped -> {
                    Log.d(TAG, "serviceCallback.MusicStopped received")
                    "MusicStopped received"
                }

                Constants.MusicLoaded -> {
                    Log.d(TAG, "serviceCallback.MusicLoaded received")
                    "MusicLoaded received"
                }

                else -> {
                    Log.d(TAG, "serviceCallback.Unknown message.")
                    "Unknown message."
                }
            }
        }
    }

    private var isServiceBound = false
    private var myBoundService: IMusicService? = null
    private lateinit var deathRecipient: DeathRecipient
    private lateinit var myServiceConnection: ServiceConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        // OR
        // binding = ActivityMainBinding.inflate(layoutInflater)
        // setContentView(binding.root)

        binding.apply {
            lifecycleOwner = this@MainActivity
            recentStatus = RecentStatus.status
            constants = Constants
        }

        binding.bindServiceButton.setOnClickListener {
            bindMusicService()
        }

        binding.unbindServiceButton.setOnClickListener {
            unbindMusicService()
        }

        binding.playButton.setOnClickListener {
            if (isServiceBound) {
                myBoundService?.playMusic()
            }
        }

        binding.pauseButton.setOnClickListener {
            if (isServiceBound) {
                myBoundService?.pauseMusic()
            }
        }

        binding.exitBoundService.setOnClickListener {
            finish()
        }

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent == null) {
                    return
                }
                val action = intent.action
                if (action == Constants.ServiceName) {
                    val extras = intent.extras
                    val result = extras!!.getInt(Constants.Result)
                    Log.d(TAG, "onReceive.result = $result")
                    when (result) {
                        Constants.ServiceStarted -> {
                            Log.d(TAG, "onReceive.ServiceStarted received")
                            RecentStatus.status.messageText = "BoundService started."
                            RecentStatus.status.bindEnabled = true
                            RecentStatus.status.unbindEnabled = false
                            RecentStatus.status.playResult = Constants.ErrorCode
                            RecentStatus.status.pauseResult = Constants.ErrorCode
                            RecentStatus.status.serverText = ""
                        }

                        Constants.ServiceBound -> {
                            Log.d(TAG, "onReceive.ServiceBound received")
                            RecentStatus.status.messageText = "Service Bound."
                        }

                        Constants.ServiceUnbound -> {
                            Log.d(TAG, "onReceive.ServiceUnbound received")
                            RecentStatus.status.messageText = "Service Unbound."
                        }

                        Constants.ServiceStopped -> {
                            Log.d(TAG, "onReceive.ServiceStopped received")
                            RecentStatus.status.messageText = "BoundService stopped."
                            RecentStatus.status.bindEnabled = false
                            RecentStatus.status.unbindEnabled = false
                            RecentStatus.status.playResult = Constants.ErrorCode
                            RecentStatus.status.pauseResult = Constants.ErrorCode
                        }

                        Constants.MusicPlaying -> {
                            Log.d(TAG, "onReceive.MusicPlaying received")
                        }

                        Constants.MusicPaused -> {
                            Log.d(TAG, "onReceive.MusicPaused received")
                        }

                        Constants.MusicStopped -> {
                            Log.d(TAG, "onReceive.MusicStopped received")
                            RecentStatus.status.messageText = "Music stopped."
                            if (isServiceBound) {
                                RecentStatus.status.playResult = Constants.PlayMusic
                                RecentStatus.status.pauseResult = Constants.ErrorCode
                            }
                        }

                        Constants.MusicLoaded -> {
                            Log.d(TAG, "onReceive.MusicLoaded received")
                            RecentStatus.status.messageText = "Music Loaded."
                            if (isServiceBound) {
                                RecentStatus.status.playResult = Constants.PlayMusic
                                RecentStatus.status.pauseResult = Constants.ErrorCode
                            }
                        }

                        else -> {
                            Log.d(TAG, "onReceive.Unknown message.")
                            RecentStatus.status.messageText = "Unknown message."
                        }
                    }
                }
            }
        }

        val filter = IntentFilter()
        filter.addAction(Constants.ServiceName)
        val localBroadcastManager = LocalBroadcastManager.getInstance(this)
        localBroadcastManager.registerReceiver(receiver, filter)

        deathRecipient = DeathRecipient { Log.d(TAG, "binderDied") }

        myServiceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                Log.d(TAG, "onServiceConnected")
                // Gets an instance of the AIDL interface named IMusicAidlInterface,
                // which we can use to call on the service
                myBoundService = IMusicService.Stub.asInterface(service)
                isServiceBound = true
                var playResult : Int = Constants.ErrorCode
                var pauseResult : Int = Constants.ErrorCode
                myBoundService?.let {
                    Log.d(TAG, "onServiceConnected.isMusicLoaded = ${it.isMusicLoaded}")
                    if (it.isMusicLoaded) {
                        if (it.isMusicPlaying) {
                            pauseResult = Constants.PauseMusic
                        } else {
                            playResult = Constants.PlayMusic
                        }
                    }
                    try {
                        it.registerCallback(serviceCallback)
                    } catch (e: RemoteException) {
                        // In this case the service has crashed before we could even
                        // do anything with it; we can count on soon being
                        // disconnected (and then reconnected if it can be restarted)
                        // so there is no need to do anything here.
                        Log.d(TAG, "onServiceConnected.registerCallback(serviceCallback) failed")
                    }
                }
                RecentStatus.status.messageText = "Service Bound."
                RecentStatus.status.bindEnabled = false
                RecentStatus.status.unbindEnabled = true
                RecentStatus.status.playResult = playResult
                RecentStatus.status.pauseResult = pauseResult
                RecentStatus.status.serverText = "Bound to Server."
            }

            override fun onServiceDisconnected(name: ComponentName) {
                // This method is not always called
                Log.d(TAG, "onServiceDisconnected")
                try {
                    myBoundService?.unregisterCallback(serviceCallback)
                } catch (e: RemoteException) {
                    Log.d(TAG, "onServiceDisconnected.registerCallback(serviceCallback) failed")
                }
                myBoundService = null
                isServiceBound = false
                RecentStatus.status.messageText = "Service Unbound."
                RecentStatus.status.bindEnabled = true
                RecentStatus.status.unbindEnabled = false
                RecentStatus.status.pauseResult = Constants.ErrorCode
                RecentStatus.status.playResult = Constants.ErrorCode
                RecentStatus.status.serverText = "Unbound to Server"
            }
        }

        bindMusicService()
    }

    override fun onStart() {
        Log.d(TAG, "onStart")
        super.onStart()
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        unbindMusicService()
        // unregisterReceiver(receiver);    // use global broadcast receiver
        // local un-registration
        val localBroadcastManager = LocalBroadcastManager.getInstance(this)
        localBroadcastManager.unregisterReceiver(receiver)
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finish()"))
    override fun onBackPressed() {
        finish()
    }

    private fun unbindMusicService() {
        Log.d(TAG, "unbindMusicService")
        Toast.makeText(this, "Unbinding ...", Toast.LENGTH_SHORT).show()
        if (isServiceBound) {
            try {
                myBoundService?.unregisterCallback(serviceCallback)
            } catch (e: RemoteException) {
                Log.d(TAG, "unbindMusicService.registerCallback(serviceCallback) failed")
            }
            unbindService(myServiceConnection)
            myBoundService = null
            isServiceBound = false
            RecentStatus.status.messageText = "Service Unbound."
            RecentStatus.status.bindEnabled = true
            RecentStatus.status.unbindEnabled = false
            RecentStatus.status.pauseResult = Constants.ErrorCode
            RecentStatus.status.playResult = Constants.ErrorCode
            RecentStatus.status.serverText = "Unbound to Server"
        }
    }

    // bind to the service
    private fun bindMusicService() {
        Log.d(TAG, "bindMusicService")
        Toast.makeText(this, "Binding ...", Toast.LENGTH_SHORT).show()
        /*
        if (!isServiceBound) {
            val bindServiceIntent = Intent(this, MusicService::class.java)
            // parameters for this Intent
            Log.d(TAG, "bindMusicService.Binding to MusicService")
            isServiceBound = bindService(bindServiceIntent, myServiceConnection, BIND_AUTO_CREATE)
        }
        */
        // OR
        if (!isServiceBound) {
            val bindServiceIntent = Intent("AidlMusicService")
            val packName = IMusicService::class.java.packageName
            Log.d(TAG, "bindMusicService.packName = $packName")
            packName?.let {
                bindServiceIntent.setPackage(it)
                Log.d(TAG, "bindMusicService.Binding to MusicService")
                isServiceBound = bindService(bindServiceIntent, myServiceConnection, BIND_AUTO_CREATE)
            }
        }
        Log.d(TAG, "bindMusicServiceis.ServiceBound = $isServiceBound")
    }
}