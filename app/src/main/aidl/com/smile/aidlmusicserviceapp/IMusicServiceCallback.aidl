// IMusicServiceCallback.aidl
package com.smile.aidlmusicserviceapp;

// Declare any non-default types here with import statements

oneway interface IMusicServiceCallback {
    /**
     * Called when the service has a new value for you.
     */
    void valueChanged(int value);
}