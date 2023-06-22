package com.smile.aidlmusicserviceapp.model

import com.smile.aidlmusicserviceapp.Constants

object RecentStatus {
    val status : ServiceStatus = ServiceStatus(""
        , bindEnabled = false, unbindEnabled = false
        , playResult = Constants.ErrorCode
        , pauseResult = Constants.ErrorCode
        , serverText = "")
}