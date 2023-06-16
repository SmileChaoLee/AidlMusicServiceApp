// IMusicAidlInterface.aidl
package com.smile.aidlserviceapp.aidl;

// Declare any non-default types here with import statements

interface IMusicAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    int playMusic();
    int pauseMusic();
    int stopMusic();
    boolean isMusicLoaded();
    boolean isMusicPlaying();
}