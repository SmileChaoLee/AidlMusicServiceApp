package com.smile.aidlserviceapp

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.IBinder.DeathRecipient
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.smile.aidlserviceapp.aidl.IMusicAidlInterface
import com.smile.aidlserviceapp.aidlservice.MusicService
import com.smile.aidlserviceapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "IBinderActivity"
    }
    private lateinit var binding : ActivityMainBinding
    private lateinit var receiver: BroadcastReceiver
    private var isServiceBound = false
    private var myBoundService: IMusicAidlInterface? = null
    private lateinit var deathRecipient: DeathRecipient
    private lateinit var myServiceConnection: ServiceConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.messageText.text = ""
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
                            Log.d(TAG, "ServiceStarted received")
                            binding.messageText.text = "BoundService started."
                            binding.bindServiceButton.isEnabled = true
                            binding.unbindServiceButton.isEnabled = false
                            binding.playButton.isEnabled = false
                            binding.pauseButton.isEnabled = false
                        }

                        Constants.ServiceBound -> {
                            Log.d(TAG, "ServiceBound received")
                            binding.messageText.text = "BoundService Bound."
                            binding.bindServiceButton.isEnabled = false
                            binding.unbindServiceButton.isEnabled = true
                            binding.playButton.isEnabled = false
                            binding.pauseButton.isEnabled = false
                            myBoundService?.let {
                                if (it.isMusicLoaded()) {
                                    binding.playButton.isEnabled = !it.isMusicPlaying()
                                    binding.pauseButton.isEnabled = it.isMusicPlaying()
                                }
                            }
                        }

                        Constants.ServiceUnbound -> {
                            Log.d(TAG, "ServiceUnbound received")
                            binding.messageText.text = "BoundService unbound."
                            binding.bindServiceButton.isEnabled = true
                            binding.unbindServiceButton.isEnabled = false
                            binding.playButton.isEnabled = false
                            binding.pauseButton.isEnabled = false
                        }

                        Constants.ServiceStopped -> {
                            Log.d(TAG, "ServiceStopped received")
                            binding.messageText.text = "BoundService stopped."
                            binding.bindServiceButton.isEnabled = false
                            binding.unbindServiceButton.isEnabled = false
                            binding.playButton.isEnabled = false
                            binding.pauseButton.isEnabled = false
                        }

                        Constants.MusicPlaying -> {
                            Log.d(TAG, "MusicPlaying received")
                            binding.messageText.text = "Music playing."
                            if (isServiceBound) {
                                binding.playButton.isEnabled = false
                                binding.pauseButton.isEnabled = true
                            }
                        }

                        Constants.MusicPaused -> {
                            Log.d(TAG, "MusicPaused received")
                            binding.messageText.text = "Music paused."
                            if (isServiceBound) {
                                binding.playButton.isEnabled = true
                                binding.pauseButton.isEnabled = false
                            }
                        }

                        Constants.MusicStopped -> {
                            Log.d(TAG, "MusicStopped received")
                            binding.messageText.text = "Music stopped."
                            if (isServiceBound) {
                                binding.playButton.isEnabled = true
                                binding.pauseButton.isEnabled = false
                            }
                        }

                        Constants.MusicLoaded -> {
                            Log.d(TAG, "MusicLoaded received")
                            binding.messageText.text = "Music Loaded."
                        }

                        else -> {
                            Log.d(TAG, "Unknown message.")
                            binding.messageText.text = "Unknown message."
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
                myBoundService = IMusicAidlInterface.Stub.asInterface(service)
                isServiceBound = true
                binding.bindServiceButton.isEnabled = false
                binding.unbindServiceButton.isEnabled = true
                binding.playButton.isEnabled = false
                binding.pauseButton.isEnabled = false
                myBoundService?.let {
                    if (it.isMusicLoaded()) {
                        binding.playButton.isEnabled = !it.isMusicPlaying()
                        binding.pauseButton.isEnabled = it.isMusicPlaying()
                    }
                }
            }

            override fun onServiceDisconnected(name: ComponentName) {
                // This method is not always called
                Log.d(TAG, "onServiceDisconnected")
                myBoundService = null
                isServiceBound = false
                binding.bindServiceButton.isEnabled = true
                binding.unbindServiceButton.isEnabled = false
            }
        }

        bindMusicService()
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

    override fun onBackPressed() {
        finish()
    }

    private fun unbindMusicService() {
        Log.d(TAG, "unbindMusicService")
        Toast.makeText(this, "Unbinding ...", Toast.LENGTH_SHORT).show()
        if (isServiceBound) {
            unbindService(myServiceConnection)
            myBoundService = null
            isServiceBound = false
            binding.bindServiceButton.isEnabled = true
            binding.unbindServiceButton.isEnabled = false
            binding.playButton.isEnabled = false
            binding.pauseButton.isEnabled = false
        }
    }

    // bind to the service
    private fun bindMusicService() {
        Log.d(TAG, "bindMusicService")
        Toast.makeText(this, "Binding ...", Toast.LENGTH_SHORT).show()
        if (!isServiceBound) {
            val bindServiceIntent = Intent(this, MusicService::class.java)
            // parameters for this Intent
            Log.d(TAG, "bindMusicServiceis.Binding")
            isServiceBound = bindService(bindServiceIntent, myServiceConnection, BIND_AUTO_CREATE)
        }
        Log.d(TAG, "bindMusicServiceis.ServiceBound = $isServiceBound")
    }
}