package com.example.musicano.Activities

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DefaultItemAnimator
import com.example.musicano.Adapters.SongAdapter
import com.example.musicano.Models.Song
import com.example.musicano.R
import java.util.Locale

class BrowsingSongsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SongAdapter
    private lateinit var allSongs: List<Song>
    private var filteredSongs: MutableList<Song> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Lock orientation to portrait mode
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browsing_songs)

        // Handle system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        makeSystemBarsTransparent()

        // Set up RecyclerView and Adapter
        recyclerView = findViewById(R.id.browse_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Set RecyclerView animation (Fade in / Slide in)
        val itemAnimator = DefaultItemAnimator().apply {
            addDuration = 1000
            removeDuration = 1000
            changeDuration = 1000
        }
        recyclerView.itemAnimator = itemAnimator

        // Receive song list from the intent
        val songs: List<Song>? = intent.getParcelableArrayListExtra("SONGS_LIST")
        if (songs != null) {
            allSongs = songs
            filteredSongs.addAll(allSongs)
            adapter = SongAdapter(filteredSongs)
            recyclerView.adapter = adapter
        }

        // Set up search functionality
        val searchEditText = findViewById<EditText>(R.id.search_song)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(editable: Editable?) {
                val query = editable.toString().lowercase().trim()
                filterSongs(query)
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterSongs(query: String) {
        filteredSongs.clear()
        if (query.isEmpty()) {
            filteredSongs.addAll(allSongs)
        } else {
            for (song in allSongs) {
                if (song.title.lowercase(Locale.ROOT).contains(query) ||
                    song.artist.name.lowercase(Locale.ROOT).contains(query)
                ) {
                    filteredSongs.add(song)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        val searchEditText = findViewById<EditText>(R.id.search_song)
        if (filteredSongs.size != allSongs.size || searchEditText.text.isNotEmpty()) {
            // Clear search and reset song list
            searchEditText.text.clear()
            filteredSongs.clear()
            filteredSongs.addAll(allSongs)
            adapter.notifyDataSetChanged()
        } else {
            // If no search query, allow the default back action
            super.onBackPressed()
        }
    }

    private fun makeSystemBarsTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
        }
    }
}
