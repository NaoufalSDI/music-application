package com.example.musicano.Api

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.musicano.Models.ChartResponse
import com.example.musicano.Models.Song
import com.google.gson.Gson

class SongsRepository(context: Context) {
    private val requestQueue: RequestQueue = Volley.newRequestQueue(context)
    private val gson = Gson()

    // Callback to return the list of popular songs once the request is completed
    fun fetchPopularSongs(callback: (List<Song>) -> Unit) {
        // Modify the URL to include limit parameter
        val url = "https://api.deezer.com/chart?limit=100" // Deezer's API endpoint for charts

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                // Deserialize the response using Gson
                val chartResponse = gson.fromJson(response, ChartResponse::class.java)
                val popularSongs = chartResponse.tracks?.data ?: emptyList()
                callback(popularSongs) // Return the list of popular songs
            },
            { error ->
                Log.e("Fetching Songs", "Response Error: ${error.message}")
                callback(emptyList()) // Return an empty list on error
            }
        )

        requestQueue.add(stringRequest) // Add the request to the request queue
    }
}
