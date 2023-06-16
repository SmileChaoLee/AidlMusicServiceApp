// IMusicAidlInterface.aidl
package com.smile.aidlserviceapp.aidl;

// Declare any non-default types here with import statements

interface IMusicAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    // void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
    //         double aDouble, String aString);
    int playMusic();
    int pauseMusic();
    int stopMusic();
    boolean isMusicLoaded();
    boolean isMusicPlaying();
}