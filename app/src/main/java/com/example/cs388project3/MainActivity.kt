package com.example.cs388project3

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.example.cs388project3.adapter.MovieAdapter
import com.example.cs388project3.api.TmdbApi
import com.example.cs388project3.models.Movie
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Headers

class MainActivity : AppCompatActivity() {

    private val movies = mutableListOf<Movie>()
    private lateinit var rvMovies: RecyclerView
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rvMovies = findViewById(R.id.rvMovies)
        rvMovies.layoutManager = LinearLayoutManager(this)

        fetchNowPlayingMovies()
    }

    private fun fetchNowPlayingMovies() {
        TmdbApi.getNowPlayingMovies(1, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                val resultsJsonArray = json.jsonObject.getJSONArray("results")

                val movieType = object : TypeToken<List<Movie>>() {}.type
                val movieList: List<Movie> = gson.fromJson(resultsJsonArray.toString(), movieType)

                movies.addAll(movieList)
                rvMovies.adapter = MovieAdapter(movies)

                Log.d("MainActivity", "Successfully fetched ${movies.size} movies")
            }

            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                errorResponse: String?,
                throwable: Throwable?
            ) {
                Log.e("MainActivity", "Failed to fetch movies: $errorResponse", throwable)
            }
        })
    }
}
