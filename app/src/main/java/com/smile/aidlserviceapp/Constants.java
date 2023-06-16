package com.smile.aidlserviceapp;

public class Constants {
    public static final String ServiceName = "com.smile.aidlserviceapp.MusicService";
    public static final int ErrorCode = -1;
    public static final int ServiceStopped = 0;
    public static final int ServiceStarted = 1;
    public static final int ServiceBound = 2;
    public static final int ServiceUnbound = 3;
    public static final int MusicPlaying = 4;
    public static final int MusicPaused = 5;
    public static final int MusicStopped = 6;
    public static final int MusicLoaded = 7;
    public static final int StopService = 101;
    public static final int PlayMusic = 102;
    public static final int PauseMusic = 103;
    public static final int StopMusic = 104;
    public static final int AskStatus = 201;
    public static final int MusicStatus = 202;

    public static final String Result = "RESULT";
    public static final String MyBoundServiceChannelName = ServiceName + ".ANDROID";
    public static final String MyBoundServiceChannelID = ServiceName + ".CHANNEL_ID";
    public static final int MyBoundServiceNotificationID = 1;

}
