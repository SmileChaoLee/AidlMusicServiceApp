package com.smile.aidlmusicserviceapp.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.smile.aidlmusicserviceapp.BR

class ServiceStatus (
    messageText: String,
    bindEnabled: Boolean,
    unbindEnabled: Boolean,
    playResult: Int,
    pauseResult: Int,
    serverText: String) : BaseObservable() {
        @get:Bindable
        var messageText = messageText
            set(value) {
                field = value
                notifyPropertyChanged(BR.messageText)
            }
        @get:Bindable
        var bindEnabled = bindEnabled
            set(value) {
                field = value
                notifyPropertyChanged(BR.bindEnabled)
            }
        @get:Bindable
        var unbindEnabled = unbindEnabled
            set(value) {
                field = value
                notifyPropertyChanged(BR.unbindEnabled)
            }
        @get:Bindable
        var playResult = playResult
            set(value) {
                field = value
                notifyPropertyChanged(BR.playResult)
            }
        @get:Bindable
        var pauseResult = pauseResult
            set(value) {
                field = value
                notifyPropertyChanged(BR.pauseResult)
            }
        @get:Bindable
        var serverText = serverText
            set(value) {
                field = value
                notifyPropertyChanged(BR.serverText)
            }
    }