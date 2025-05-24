package com.example.freemusicapiintegratinytsearchanddownloader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso

class VideoAdapter(private val videoList: List<Video>,
                   private val onGetSourceClicked: (videoUrl: String) -> Unit
                    ) :
    RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val songImage: ShapeableImageView = itemView.findViewById(R.id.imgThumbnail)
        val songUrl: TextView = itemView.findViewById(R.id.txtsongURl)
        val btnGetSource: View = itemView.findViewById(R.id.btnGetSource)

        val txtDownloadStatus: TextView = itemView.findViewById(R.id.txtDownloadStatus)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.eachitem, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videoList[position]
        holder.songUrl.text = video.url
        Picasso.get().load(video.thumbnail).into(holder.songImage)

        // Reset UI
        holder.txtDownloadStatus.visibility = View.GONE
        holder.progressBar.visibility = View.GONE
        holder.btnGetSource.isEnabled = true

        holder.btnGetSource.setOnClickListener {
            holder.progressBar.visibility = View.VISIBLE
            holder.txtDownloadStatus.visibility = View.VISIBLE
            holder.txtDownloadStatus.text = "Obtaining Source..."
            holder.btnGetSource.isEnabled = false

            GlobalData.thumbnailPath = video.thumbnail

            onGetSourceClicked(video.url)

            // After download is done (you need a callback or LiveData/Flow),
            // update UI like:
            // holder.progressBar.visibility = View.GONE
            // holder.txtDownloadStatus.text = "Download Complete"
            // holder.btnDownload.text = "Downloaded"
        }
    }

    override fun getItemCount(): Int = videoList.size
}
