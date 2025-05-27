package com.example.freemusicapiintegratinytsearchanddownloader

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.os.Handler


class MediaPlayerActivity : AppCompatActivity() {
    private var mediaPlayer : MediaPlayer? = null
    private var isPlaying = false
    private val client = OkHttpClient()
    private lateinit var progressBar: ProgressBar
    private lateinit var playPauseButton : Button

    private lateinit var seekBar: SeekBar
    private val handler = Handler(Looper.getMainLooper())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_player)

        findViewById<TextView>(R.id.songDownloadURL).text = GlobalData.downloadURL.toString()
        findViewById<TextView>(R.id.songTitle).text = GlobalData.title.toString()
        findViewById<TextView>(R.id.songSize).text = GlobalData.size.toString()

        seekBar = findViewById(R.id.seekBar)
        seekBar.isEnabled = false // Disabled until track is loaded


        Picasso.get().load(GlobalData.thumbnailPath).into(findViewById<ImageView>(R.id.coverImageView))

        progressBar = findViewById(R.id.downloadProgressBar)
        playPauseButton = findViewById(R.id.playPauseButton)
        playPauseButton.isEnabled=false



        findViewById<Button>(R.id.btnDownload).setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                downloadMP3(GlobalData.downloadURL ?: "", GlobalData.title ?: "music")
            }
        }

        /*playPauseButton.setOnClickListener {
            val file = File(GlobalData.localFilePath)
            if (!file.exists()) {
                Toast.makeText(this, "File not downloaded yet!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isPlaying) {
                if (GlobalData.localFilePath != null && File(GlobalData.localFilePath).exists()) {
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(GlobalData.localFilePath)
                        prepare()
                        start()
                    }
                } else {
                    Toast.makeText(this, "Audio file not found", Toast.LENGTH_LONG).show()
                }

                playPauseButton.text = "Pause"
                isPlaying = true
            } else {
                mediaPlayer?.pause()
                playPauseButton.text = "Play"
                isPlaying = false
            }
        }*/

    }

    private suspend fun downloadMP3(url: String, title: String) {

        val request = Request.Builder().url(url).build()

        withContext(Dispatchers.Main) {
            progressBar.progress = 0
            progressBar.visibility = View.VISIBLE
        }

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val inputStream = response.body?.byteStream() ?: return
            val totalBytes = response.body?.contentLength() ?: -1

            val file = File(getExternalFilesDir(null), "$title.mp3")

            withContext(Dispatchers.Main) {
                findViewById<TextView>(R.id.songSaveDirectory).text = file.toString()
            }


            val outputStream = FileOutputStream(file)

            val buffer = ByteArray(8192)
            var bytesRead: Int
            var downloadedBytes = 0L

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                downloadedBytes += bytesRead

                if (totalBytes > 0) {
                    val progressPercent = (downloadedBytes * 100 / totalBytes).toInt()
                    withContext(Dispatchers.Main) {
                        progressBar.progress = progressPercent
                    }
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            GlobalData.localFilePath = file.absolutePath

            withContext(Dispatchers.Main) {
                Toast.makeText(this@MediaPlayerActivity, "Downloaded to ${file.absolutePath}", Toast.LENGTH_LONG).show()
                progressBar.visibility = View.GONE

                // Enable play/pause button after download
                val playPauseButton = findViewById<Button>(R.id.playPauseButton)
                playPauseButton.isEnabled = true

                playPauseButton.setOnClickListener {
                    val file = File(GlobalData.localFilePath)
                    if (!file.exists()) {
                        Toast.makeText(this@MediaPlayerActivity, "File not found!", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    if (!isPlaying) {
                        mediaPlayer = MediaPlayer().apply {
                            setDataSource(file.absolutePath)
                            prepare()
                            start()

                            // Enable SeekBar after media is prepared
                            seekBar.max = duration
                            seekBar.isEnabled = true

                            // Update SeekBar periodically
                            handler.post(object : Runnable {
                                override fun run() {
                                    if (mediaPlayer != null && isPlaying) {
                                        seekBar.progress = mediaPlayer!!.currentPosition
                                        handler.postDelayed(this, 500)
                                    }
                                }
                            })
                        }
                        playPauseButton.text = "Pause"
                        isPlaying = true
                    } else {
                        mediaPlayer?.pause()
                        playPauseButton.text = "Play"
                        isPlaying = false
                    }
                }

                seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        // User dragging the seekbar
                        if (fromUser && mediaPlayer != null) {
                            mediaPlayer?.seekTo(progress)
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MediaPlayerActivity, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
                Log.d("Download failed", "downloadMP3: ${e.message}")
                progressBar.visibility = View.GONE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
