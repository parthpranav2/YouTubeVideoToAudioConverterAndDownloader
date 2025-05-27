package com.example.freemusicapiintegratinytsearchanddownloader

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.freemusicapiintegratinytsearchanddownloader.databinding.ActivityYtsearchactivityBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class YTSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityYtsearchactivityBinding
    private lateinit var adapter: VideoAdapter
    private val videoList = mutableListOf<Video>()

    private lateinit var apiKey : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYtsearchactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val dbRef = FirebaseDatabase.getInstance().getReference("freemusicapiintegratinytsearchanddownloader").child("apikey")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                apiKey = snapshot.getValue(String::class.java) ?: ""
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@YTSearchActivity,
                    "Unable to fetch api key error: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })


        binding.rvSongs.layoutManager = LinearLayoutManager(this)
        adapter = VideoAdapter(videoList) { videoUrl ->
            downloadMp3FromUrl(videoUrl)
        }
        binding.rvSongs.adapter = adapter

        binding.btnSearch.setOnClickListener {
            fetchVideos(binding.txtName.text.toString())
        }
    }

    private fun fetchVideos(query: String) {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("x-rapidapi-key", apiKey)
                    .addHeader("x-rapidapi-host", "yt-search-and-download-mp3.p.rapidapi.com")
                    .build()
                chain.proceed(request)
            }
            .build()

        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://yt-search-and-download-mp3.p.rapidapi.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(APIInterface::class.java) // <-- This must match the interface

        val retrofitData = retrofitBuilder.getVideoByName(binding.txtName.text.toString())

        retrofitData.enqueue(object : Callback<SearchData> {
            override fun onResponse(call: Call<SearchData>, response: Response<SearchData>) {
                if (response.isSuccessful) {
                    val videos = response.body()?.videos
                    Log.d("YTSearch", "Videos found: $videos")

                    if (!videos.isNullOrEmpty()) {
                        videoList.clear()
                        videoList.addAll(videos)
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@YTSearchActivity, "No results found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@YTSearchActivity, "API Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SearchData>, t: Throwable) {
                Toast.makeText(this@YTSearchActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("YTSearch", "Failure: ${t.message}")
            }
        })

    }

    private fun downloadMp3FromUrl(videoUrl: String) {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("x-rapidapi-key", apiKey)
                    .addHeader("x-rapidapi-host", "yt-search-and-download-mp3.p.rapidapi.com")
                    .build()
                chain.proceed(request)
            }
            .build()
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://yt-search-and-download-mp3.p.rapidapi.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(APIInterface::class.java) // <-- This must match the interface

        val call = retrofitBuilder.getURLofDownloadable(videoUrl)
        call.enqueue(object : Callback<DownloadData> {
            override fun onResponse(call: Call<DownloadData>, response: Response<DownloadData>) {
                if (response.isSuccessful) {
                    val downloadUrl = response.body()?.download
                    val title = response.body()?.title
                    val size = response.body()?.size

                    if (!downloadUrl.isNullOrEmpty()) {
                        // 🔽 You can now use this URL to download the MP3
                        GlobalData.downloadURL=downloadUrl
                        GlobalData.size=size
                        GlobalData.title=title
                        Log.d("MP3Download", "Download URL: $downloadUrl")

                        val intent = Intent(this@YTSearchActivity, MediaPlayerActivity::class.java)
                        startActivity(intent)
                        // Optionally store it in a variable or start a download
                    }
                } else {
                    Log.e("MP3Download", "API response unsuccessful")
                }
            }

            override fun onFailure(call: Call<DownloadData>, t: Throwable) {
                Log.e("MP3Download", "API call failed", t)
            }
        })
    }

}

