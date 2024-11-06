package com.example.musicano.Activities


import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.musicano.Models.Song
import com.example.musicano.R

class SongsPlayerActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var playButton: ImageButton
    private lateinit var playAgainButton: ImageButton
    private lateinit var previousButton: ImageButton
    private lateinit var nextButton: ImageButton
    private lateinit var shuffleButton: ImageButton
    private lateinit var currentDurationTextView: TextView
    private lateinit var durationTextView: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var backArrow: ImageView // Back arrow
    private lateinit var mainLayout: View

    private var isPlaying = false
    private var currentSongIndex = 0
    private var songsList: List<Song> = listOf()
    private var isShuffle = false
    private var isActivityActive = false // Variable to track the activity state
    private val handler = Handler() // Handler for updating UI


    override fun onCreate(savedInstanceState: Bundle?) {
        // Lock orientation to portrait
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_songs_player)

        // Handle system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        makeSystemBarsTransparent()

        // Retrieve song data from Intent
        currentSongIndex = intent.getIntExtra("SONG_INDEX", 0)
        songsList = intent.getParcelableArrayListExtra("SONGS_LIST") ?: listOf()

        // Initialize media player if a song URL is provided
        if (songsList.isNotEmpty()) {
            val song = songsList[currentSongIndex]
            initializeMediaPlayer(song.previewUrl)

            // Update song UI with the initial song details
            updateSongUI(song.title, song.artist.name, song.album.coverBigUrl)
        }

        // Initialize UI components
        initializeUIComponents()

        // Set up listeners
        setupListeners()

        isActivityActive = true // Indicate that the activity is active
    }

    private fun makeSystemBarsTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
        }
    }

    private fun initializeUIComponents() {
        mainLayout = findViewById(R.id.main)
        backArrow = findViewById(R.id.back_arrow)
        playButton = findViewById(R.id.play_button)
        playAgainButton = findViewById(R.id.replay_button)
        previousButton = findViewById(R.id.previous_button)
        nextButton = findViewById(R.id.next_button)
        shuffleButton = findViewById(R.id.shuffle_button)
        currentDurationTextView = findViewById(R.id.player_current_duration)
        durationTextView = findViewById(R.id.player_song_duration)
        seekBar = findViewById(R.id.song_seekbar)
    }

    private fun setupListeners() {
        backArrow.setOnClickListener {
            stopSeekBarUpdate() // Stop the SeekBar update
            releaseMediaPlayer() // Release MediaPlayer resources
            finish() // Finish the current activity to return to the previous one
        }
        playButton.setOnClickListener { togglePlayPause() }
        playAgainButton.setOnClickListener { restartSong() }
        previousButton.setOnClickListener { playPreviousSong() }
        shuffleButton.setOnClickListener { toggleShuffle() }
        nextButton.setOnClickListener { playNextSong() }

        // Setup SeekBar listener
        setupSeekBar()
    }

    private fun restartSong() {
        mediaPlayer.seekTo(0)
        mediaPlayer.start()
        isPlaying = true
        playButton.setImageResource(R.drawable.pause)
        updateSeekBar() // Start updating the seek bar when restarting the song
    }

    private fun toggleShuffle() {
        isShuffle = !isShuffle
        shuffleButton.setImageResource(if (isShuffle) R.drawable.shuffle else R.drawable.shuffle_off)
    }

    private fun setupSeekBar() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                stopSeekBarUpdate() // Stop updating while dragging
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                updateSeekBar() // Resume updating after dragging
            }
        })
    }

    private fun initializeMediaPlayer(songUrl: String) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(songUrl)
                prepareAsync()
                setOnPreparedListener {
                    start()
                    this@SongsPlayerActivity.isPlaying = true
                    durationTextView.text = formatDuration(duration)
                    seekBar.max = duration
                    updateSeekBar() // Update the seek bar when the song is prepared

                    // Add OnCompletionListener to play the next song when current finishes
                    setOnCompletionListener {
                        playNextSong()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playSong(song: Song) {
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release() // Release resources before playing a new song
        }
        currentSongIndex = songsList.indexOf(song) // Update current song index
        initializeMediaPlayer(song.previewUrl)
        updateSongUI(song.title, song.artist.name, song.album.coverBigUrl)

        // Update play button
        playButton.setImageResource(R.drawable.pause) // Update to show pause
        isPlaying = true // Set playing state
    }

    private fun updateSongUI(title: String?, artist: String?, imageUrl: String?) {
        findViewById<TextView>(R.id.player_song_artist).text = artist
        findViewById<TextView>(R.id.player_song_title).text = title

        // Load the image and set the background color based on the dominant color
        Glide.with(this)
            .asBitmap() // Load the image as a bitmap
            .load(imageUrl)
            .apply(RequestOptions().fitCenter())
            .transform(CenterCrop(), RoundedCorners(30))
            .into(findViewById<ImageView>(R.id.player_image))

        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    Palette.from(resource).generate { palette ->
                        // Get the dominant color
                        palette?.dominantSwatch?.let {
                            val dominantColor = it.rgb
                            val brightness = calculateBrightness(dominantColor)

                            // Apply text and icon color based on brightness
                            if (brightness > 128) {
                                findViewById<TextView>(R.id.player_song_artist).setTextColor(Color.BLACK)
                                findViewById<TextView>(R.id.player_song_title).setTextColor(Color.BLACK)
                                findViewById<TextView>(R.id.playing_now).setTextColor(Color.BLACK)
                                backArrow.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN)
                            } else {
                                findViewById<TextView>(R.id.player_song_artist).setTextColor(Color.WHITE)
                                findViewById<TextView>(R.id.player_song_title).setTextColor(Color.parseColor("#f1f1f1"))
                                findViewById<TextView>(R.id.playing_now).setTextColor(Color.WHITE)
                                backArrow.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                            }

                            val gradientDrawable = GradientDrawable(
                                GradientDrawable.Orientation.TOP_BOTTOM,
                                intArrayOf(dominantColor, Color.BLACK)
                            )

                            mainLayout.background = gradientDrawable
                        }
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    mainLayout.setBackgroundColor(resources.getColor(android.R.color.white))
                }
            })
    }

    // Helper function to calculate brightness of a color
    private fun calculateBrightness(color: Int): Int {
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)

        // Using the luminance formula to calculate brightness
        return (red * 299 + green * 587 + blue * 114) / 1000
    }

    private fun playNextSong() {
        if (isShuffle) {
            currentSongIndex = songsList.indices.random()
        } else {
            currentSongIndex = (currentSongIndex + 1) % songsList.size
        }
        playSong(songsList[currentSongIndex])
    }

    private fun playPreviousSong() {
        if (songsList.isNotEmpty()) { // Ensure the song list is not empty
            if (isShuffle) {
                currentSongIndex = songsList.indices.random()
            } else {
                currentSongIndex = if (currentSongIndex == 0) {
                    songsList.size - 1 // If index is 0, go to the last song
                } else {
                    currentSongIndex - 1 // Otherwise go to the previous song
                }
            }
            playSong(songsList[currentSongIndex])
        } else {
            Toast.makeText(this, "No song to play", Toast.LENGTH_SHORT).show()
        }
    }

    private fun togglePlayPause() {
        if (isPlaying) {
            mediaPlayer.pause()
            playButton.setImageResource(R.drawable.play) // Update button to play icon
        } else {
            mediaPlayer.start()
            playButton.setImageResource(R.drawable.pause) // Update button to pause icon
            updateSeekBar() // Start updating the seek bar when resuming
        }
        isPlaying = !isPlaying // Toggle the playing state
    }

    private fun updateSeekBar() {
        if (::mediaPlayer.isInitialized && isActivityActive) {
            // Update SeekBar
            seekBar.progress = mediaPlayer.currentPosition

            // Update current duration TextView
            currentDurationTextView.text = formatDuration(mediaPlayer.currentPosition)

            // Post delayed update
            handler.postDelayed({ updateSeekBar() }, 1000) // Update every second
        }
    }

    private fun stopSeekBarUpdate() {
        handler.removeCallbacksAndMessages(null) // Stop updating the seek bar
    }

    private fun formatDuration(milliseconds: Int): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds) // Format duration as mm:ss
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSeekBarUpdate() // Stop updating the SeekBar
        releaseMediaPlayer() // Release resources
        isActivityActive = false // Mark activity as inactive
    }

    private fun releaseMediaPlayer() {
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }
}
