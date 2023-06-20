// IMusicService.aidl
package com.smile.aidlmusicserviceapp;

import com.smile.aidlmusicserviceapp.IMusicServiceCallback;

// Declare any non-default types here with import statements

interface IMusicService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    int playMusic();
    int pauseMusic();
    int stopMusic();
    boolean isMusicLoaded();
    boolean isMusicPlaying();
    /**
     * Often you want to allow a service to call back to its clients.
     * This shows how to do so, by registering a callback interface with
     * the service.
     */
    void registerCallback(IMusicServiceCallback cb);

    /**
     * Remove a previously registered callback interface.
     */
    void unregisterCallback(IMusicServiceCallback cb);
}