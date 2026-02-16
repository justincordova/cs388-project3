package com.example.cs388project3.api

import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import okhttp3.Headers

object TmdbApi {
    private const val API_KEY = "a07e22bc18f5cb106bfe4cc1f83ad8ed"
    private const val BASE_URL = "https://api.themoviedb.org/3"
    private const val NOW_PLAYING_ENDPOINT = "/movie/now_playing"
    private val client = AsyncHttpClient()

    fun getNowPlayingMovies(page: Int, handler: JsonHttpResponseHandler) {
        val url = "$BASE_URL$NOW_PLAYING_ENDPOINT?api_key=$API_KEY&page=$page"
        client.get(url, handler)
    }
}