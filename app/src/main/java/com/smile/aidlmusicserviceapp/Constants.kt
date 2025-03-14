package com.smile.aidlmusicserviceapp

object Constants {
    const val ServiceName = "com.smile.aidlserviceapp.MusicService"
    const val ErrorCode = -1
    const val ServiceStopped = 0
    const val ServiceStarted = 1
    const val ServiceBound = 2
    const val ServiceUnbound = 3
    const val MusicPlaying = 4
    const val MusicPaused = 5
    const val MusicStopped = 6
    const val MusicLoaded = 7
    const val StopService = 101
    const val PlayMusic = 102
    const val PauseMusic = 103
    const val StopMusic = 104
    const val AskStatus = 201
    const val MusicStatus = 202
    const val Result = "RESULT"
    const val MyBoundServiceChannelName = "$ServiceName.ANDROID"
    const val MyBoundServiceChannelID = "$ServiceName.CHANNEL_ID"
    const val MyBoundServiceNotificationID = 1
}