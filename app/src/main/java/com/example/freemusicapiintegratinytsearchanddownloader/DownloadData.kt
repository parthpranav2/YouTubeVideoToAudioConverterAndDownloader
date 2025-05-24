package com.example.freemusicapiintegratinytsearchanddownloader

data class DownloadData(
    val download: String,
    val size: String,
    val success: Boolean,
    val title: String,
    val type: String
)