package com.example.musicano.Activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.musicano.Api.SongsRepository
import com.example.musicano.R

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var songsRepository: SongsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash) // Créez un layout pour votre écran de démarrage

        songsRepository = SongsRepository(this)
        fetchPopularSongs()
    }

    private fun fetchPopularSongs() {
        songsRepository.fetchPopularSongs { songs ->
            // Une fois les données chargées, lancez l'activité de navigation
            val intent = Intent(this, BrowsingSongsActivity::class.java)
            intent.putParcelableArrayListExtra("SONGS_LIST", ArrayList(songs))
            startActivity(intent)

            // Fermez l'écran de démarrage
            finish()
        }
    }
}
