package com.example.musicano.Adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.musicano.Activities.SongsPlayerActivity
import com.example.musicano.Models.Song
import com.example.musicano.R

class SongAdapter(private val songs: List<Song>) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.browse_song_title)
        private val artist: TextView = itemView.findViewById(R.id.browse_song_artist)
        private val imageView: ImageView = itemView.findViewById(R.id.browse_song_image)

        fun bind(song: Song) {
            title.text = song.title
            artist.text = song.artist.name
            Glide.with(itemView.context)
                .load(song.album.coverBigUrl)
                .transform(CenterCrop(), RoundedCorners(25))
                .into(imageView)

            itemView.setOnClickListener {
                val intent = Intent(this.itemView.context, SongsPlayerActivity::class.java).apply {
                    putExtra("SONG_INDEX", adapterPosition)
                    putParcelableArrayListExtra("SONGS_LIST", ArrayList(songs))
                }
                itemView.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.song_item, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songs[position])
    }

    override fun getItemCount(): Int = songs.size
}
