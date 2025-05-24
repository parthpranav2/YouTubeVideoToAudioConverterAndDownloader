package com.example.freemusicapiintegratinytsearchanddownloader

data class Video(
    val duration: String,
    val id: String,
    val isLive: Boolean,
    val name: String,
    val thumbnail: String,
    val url: String,
    val views: Int
)