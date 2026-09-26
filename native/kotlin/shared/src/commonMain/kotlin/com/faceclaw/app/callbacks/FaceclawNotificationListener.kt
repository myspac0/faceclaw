package com.faceclaw.app

interface FaceclawNotificationListener {
    fun onNotificationPosted(key: String?): Unit
    fun onNotificationImageSaved(path: String?, key: String?): Unit
}