# Flixster+ Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build an Android app that fetches and displays currently playing movies from The Movie Database API in a scrollable list with posters, titles, and descriptions.

**Architecture:** Simple Android app with direct API calls using AsyncHTTPClient, RecyclerView for display, Glide for image loading, and Gson for JSON parsing. The app will make async network requests to TMDB API, parse JSON to Kotlin data classes, and display in RecyclerView.

**Tech Stack:**
- Kotlin for Android development
- CodePath AsyncHTTPClient for network requests
- Glide 4.16.0 for image loading
- Gson 2.8.6 for JSON parsing
- RecyclerView for list display
- ConstraintLayout for UI

**API Details:**
- Base URL: https://api.themoviedb.org/3/movie/now_playing
- API Key: a07e22bc18f5cb106bfe4cc1f83ad8ed
- Image base URL: https://image.tmdb.org/t/p/w500/

---

## Section 1: Project Setup and Dependencies

### Task 1: Add Required Dependencies

**Files:**
- Modify: `app/build.gradle.kts`

**Step 1: Add AsyncHTTPClient dependency**

Add to `dependencies` block:
```kotlin
implementation("com.codepath.libraries:asynchttpclient:2.2.0")
```

**Step 2: Add Glide dependencies**

Add to `dependencies` block:
```kotlin
implementation("com.github.bumptech.glide:glide:4.16.0")
annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
```

**Step 3: Add Gson dependency**

Add to `dependencies` block:
```kotlin
implementation("com.google.code.gson:gson:2.8.6")
```

**Step 4: Add RecyclerView dependencies**

Add to `dependencies` block:
```kotlin
implementation("androidx.recyclerview:recyclerview:1.3.2")
```

**Step 5: Sync Gradle**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 6: Commit**

```bash
git add app/build.gradle.kts
git commit -m "feat: add required dependencies for network, image loading, and parsing"
```

---

### Task 2: Add Internet Permission

**Files:**
- Modify: `app/src/main/AndroidManifest.xml`

**Step 1: Add INTERNET permission**

Add inside `<manifest>` tag, before `<application>`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

**Step 2: Build to verify**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add app/src/main/AndroidManifest.xml
git commit -m "feat: add internet permission for API requests"
```

---

### Task 3: Create Glide Module

**Files:**
- Create: `app/src/main/java/com/example/cs388project3/MyAppGlideModule.kt`

**Step 1: Create MyAppGlideModule class**

Create file with content:
```kotlin
package com.example.cs388project3

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions

@GlideModule
class MyAppGlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setDefaultRequestOptions(
            RequestOptions().format(DecodeFormat.PREFER_ARGB_8888)
        )
    }
}
```

**Step 2: Build and verify Glide code generation**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL (Glide generates GlideApp code)

**Step 3: Commit**

```bash
git add app/src/main/java/com/example/cs388project3/MyAppGlideModule.kt
git commit -m "feat: create Glide module for image loading configuration"
```

---

## Section 2: Data Models

### Task 4: Create Movie Data Class

**Files:**
- Create: `app/src/main/java/com/example/cs388project3/models/Movie.kt`

**Step 1: Create package directory**

Run: `mkdir -p app/src/main/java/com/example/cs388project3/models`

**Step 2: Create Movie data class**

Create file with content:
```kotlin
package com.example.cs388project3.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Movie(
    @SerializedName("adult")
    val adult: Boolean? = false,

    @SerializedName("backdrop_path")
    val backdropPath: String? = "",

    @SerializedName("genre_ids")
    val genreIds: List<Int>? = emptyList(),

    @SerializedName("id")
    val id: Int? = 0,

    @SerializedName("original_language")
    val originalLanguage: String? = "",

    @SerializedName("original_title")
    val originalTitle: String? = "",

    @SerializedName("overview")
    val overview: String? = "",

    @SerializedName("popularity")
    val popularity: Double? = 0.0,

    @SerializedName("poster_path")
    val posterPath: String? = "",

    @SerializedName("release_date")
    val releaseDate: String? = "",

    @SerializedName("title")
    val title: String? = "",

    @SerializedName("video")
    val video: Boolean? = false,

    @SerializedName("vote_average")
    val voteAverage: Double? = 0.0,

    @SerializedName("vote_count")
    val voteCount: Int? = 0
) {
    val fullPosterPath: String
        get() = "https://image.tmdb.org/t/p/w500/$posterPath"

    val fullBackdropPath: String
        get() = "https://image.tmdb.org/t/p/w500/$backdropPath"
}
```

**Step 5: Build to verify**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 6: Commit**

```bash
git add app/build.gradle.kts
git add app/src/main/java/com/example/cs388project3/models/Movie.kt
git commit -m "feat: create Movie data model with Gson annotations and image URL helpers"
```

---

## Section 3: Network Layer

### Task 5: Create TMDB API Client

**Files:**
- Create: `app/src/main/java/com/example/cs388project3/api/TmdbApi.kt`

**Step 1: Create package directory**

Run: `mkdir -p app/src/main/java/com/example/cs388project3/api`

**Step 2: Create TmdbApi object**

Create file with content:
```kotlin
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
```

**Step 3: Build to verify**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add app/src/main/java/com/example/cs388project3/api/TmdbApi.kt
git commit -m "feat: create TmdbApi client for fetching now playing movies"
```

---

## Section 4: UI Components - Movie Item Layout

### Task 7: Create Movie Item Layout

**Files:**
- Create: `app/src/main/res/layout/item_movie.xml`

**Step 1: Create item_movie.xml layout**

Create file with content:
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    android:padding="12dp">

    <ImageView
        android:id="@+id/ivPoster"
        android:layout_width="100dp"
        android:layout_height="150dp"
        android:contentDescription="@string/movie_poster"
        android:scaleType="centerCrop"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        tools:src="@tools:sample/avatars" />

    <TextView
        android:id="@+id/tvTitle"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="12dp"
        android:layout_marginEnd="8dp"
        android:ellipsize="end"
        android:maxLines="2"
        android:textColor="@android:color/black"
        android:textSize="18sp"
        android:textStyle="bold"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toEndOf="@id/ivPoster"
        app:layout_constraintTop_toTopOf="@id/ivPoster"
        tools:text="Movie Title Here" />

    <TextView
        android:id="@+id/tvOverview"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:layout_marginStart="12dp"
        android:layout_marginTop="8dp"
        android:layout_marginEnd="8dp"
        android:ellipsize="end"
        android:maxLines="4"
        android:textColor="@android:color/darker_gray"
        android:textSize="14sp"
        app:layout_constraintBottom_toBottomOf="@id/ivPoster"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toEndOf="@id/ivPoster"
        app:layout_constraintTop_toBottomOf="@id/tvTitle"
        tools:text="This is a movie overview that describes the plot and storyline of the film." />

</androidx.constraintlayout.widget.ConstraintLayout>
```

**Step 2: Add string resource**

Add to `app/src/main/res/values/strings.xml`:
```xml
<string name="movie_poster">Movie Poster</string>
```

**Step 3: Build to verify**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add app/src/main/res/layout/item_movie.xml
git add app/src/main/res/values/strings.xml
git commit -m "feat: create movie item layout with poster, title, and overview"
```

---

## Section 5: RecyclerView Adapter

### Task 8: Create MovieAdapter

**Files:**
- Create: `app/src/main/java/com/example/cs388project3/adapter/MovieAdapter.kt`

**Step 1: Create package directory**

Run: `mkdir -p app/src/main/java/com/example/cs388project3/adapter`

**Step 2: Create MovieAdapter class**

Create file with content:
```kotlin
package com.example.cs388project3.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cs388project3.R
import com.example.cs388project3.models.Movie

class MovieAdapter(private val movies: List<Movie>) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_movie, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position]
        holder.bind(movie)
    }

    override fun getItemCount(): Int = movies.size

    class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPoster: ImageView = itemView.findViewById(R.id.ivPoster)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvOverview: TextView = itemView.findViewById(R.id.tvOverview)

        fun bind(movie: Movie) {
            tvTitle.text = movie.title
            tvOverview.text = movie.overview

            Glide.with(itemView.context)
                .load(movie.fullPosterPath)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .centerCrop()
                .into(ivPoster)
        }
    }
}
```

**Step 3: Build to verify**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add app/src/main/java/com/example/cs388project3/adapter/MovieAdapter.kt
git commit -m "feat: create MovieAdapter for RecyclerView with Glide image loading"
```

---

## Section 6: Main Activity Integration

### Task 8: Update Main Activity Layout

**Files:**
- Modify: `app/src/main/res/layout/activity_main.xml`

**Step 1: Replace content with RecyclerView**

Replace entire file content with:
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rvMovies"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:clipToPadding="false"
        android:padding="4dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        tools:listitem="@layout/item_movie" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

**Step 2: Build to verify**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add app/src/main/res/layout/activity_main.xml
git commit -m "feat: replace main layout with RecyclerView for movie list"
```

---

### Task 9: Update MainActivity to Fetch and Display Movies

**Files:**
- Modify: `app/src/main/java/com/example/cs388project3/MainActivity.kt`

**Step 1: Replace MainActivity content**

Replace entire file content with:
```kotlin
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
```

**Step 2: Build to verify**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add app/src/main/java/com/example/cs388project3/MainActivity.kt
git commit -m "feat: integrate API call and RecyclerView in MainActivity"
```

---

## Section 7: Stretch Features - UI Styling

### Task 10: Add Custom Colors

**Files:**
- Modify: `app/src/main/res/values/colors.xml`

**Step 1: Add custom colors**

Replace entire file content with:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="black">#FF000000</color>
    <color name="white">#FFFFFFFF</color>
    <color name="primary">#E50914</color>
    <color name="primary_dark">#B20710</color>
    <color name="accent">#FFA500</color>
    <color name="background">#F5F5F5</color>
    <color name="surface">#FFFFFF</color>
    <color name="text_primary">#212121</color>
    <color name="text_secondary">#757575</color>
</resources>
```

**Step 2: Build to verify**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add app/src/main/res/values/colors.xml
git commit -m "feat: add custom color palette for app theming"
```

---

### Task 11: Update App Theme

**Files:**
- Modify: `app/src/main/res/values/themes.xml`
- Modify: `app/src/main/res/values-night/themes.xml`

**Step 1: Update light theme**

Replace `app/src/main/res/values/themes.xml` content:
```xml
<resources xmlns:tools="http://schemas.android.com/tools">
    <style name="Theme.Cs388project3" parent="Theme.MaterialComponents.DayNight.DarkActionBar">
        <item name="colorPrimary">@color/primary</item>
        <item name="colorPrimaryVariant">@color/primary_dark</item>
        <item name="colorOnPrimary">@color/white</item>
        <item name="colorSecondary">@color/accent</item>
        <item name="colorSecondaryVariant">@color/accent</item>
        <item name="colorOnSecondary">@color/black</item>
        <item name="android:statusBarColor">?attr/colorPrimaryVariant</item>
    </style>
</resources>
```

**Step 2: Update dark theme**

Replace `app/src/main/res/values-night/themes.xml` content:
```xml
<resources xmlns:tools="http://schemas.android.com/tools">
    <style name="Theme.Cs388project3" parent="Theme.MaterialComponents.DayNight.DarkActionBar">
        <item name="colorPrimary">@color/primary</item>
        <item name="colorPrimaryVariant">@color/primary_dark</item>
        <item name="colorOnPrimary">@color/white</item>
        <item name="colorSecondary">@color/accent</item>
        <item name="colorSecondaryVariant">@color/accent</item>
        <item name="colorOnSecondary">@color/black</item>
        <item name="android:statusBarColor">?attr/colorPrimaryVariant</item>
    </style>
</resources>
```

**Step 3: Build to verify**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add app/src/main/res/values/themes.xml
git add app/src/main/res/values-night/themes.xml
git commit -m "feat: update app theme with custom colors"
```

---

### Task 12: Style Movie Item

**Files:**
- Modify: `app/src/main/res/layout/item_movie.xml`

**Step 1: Update item_movie.xml with styled elements**

Replace entire file content with:
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardCornerRadius="8dp"
    app:cardElevation="4dp">

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="12dp">

        <ImageView
            android:id="@+id/ivPoster"
            android:layout_width="100dp"
            android:layout_height="150dp"
            android:contentDescription="@string/movie_poster"
            android:scaleType="centerCrop"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent"
            tools:src="@tools:sample/avatars" />

        <TextView
            android:id="@+id/tvTitle"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginStart="12dp"
            android:layout_marginEnd="8dp"
            android:ellipsize="end"
            android:maxLines="2"
            android:textColor="@color/text_primary"
            android:textSize="18sp"
            android:textStyle="bold"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toEndOf="@id/ivPoster"
            app:layout_constraintTop_toTopOf="@id/ivPoster"
            tools:text="Movie Title Here" />

        <TextView
            android:id="@+id/tvOverview"
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:layout_marginStart="12dp"
            android:layout_marginTop="8dp"
            android:layout_marginEnd="8dp"
            android:ellipsize="end"
            android:maxLines="4"
            android:textColor="@color/text_secondary"
            android:textSize="14sp"
            app:layout_constraintBottom_toBottomOf="@id/ivPoster"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toEndOf="@id/ivPoster"
            app:layout_constraintTop_toBottomOf="@id/tvTitle"
            tools:text="This is a movie overview that describes the plot and storyline of the film." />

    </androidx.constraintlayout.widget.ConstraintLayout>

</androidx.cardview.widget.CardView>
```

**Step 2: Build to verify**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add app/src/main/res/layout/item_movie.xml
git commit -m "feat: style movie item with CardView and custom colors"
```

---

## Section 8: Stretch Features - Orientation Responsivity

### Task 13: Create Landscape Layout

**Files:**
- Create: `app/src/main/res/layout-land/item_movie.xml`

**Step 1: Create landscape layout directory**

Run: `mkdir -p app/src/main/res/layout-land`

**Step 2: Create landscape movie item layout**

Create file with content:
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardCornerRadius="8dp"
    app:cardElevation="4dp">

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="12dp">

        <ImageView
            android:id="@+id/ivPoster"
            android:layout_width="80dp"
            android:layout_height="120dp"
            android:contentDescription="@string/movie_poster"
            android:scaleType="centerCrop"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent"
            tools:src="@tools:sample/avatars" />

        <TextView
            android:id="@+id/tvTitle"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginStart="12dp"
            android:layout_marginEnd="8dp"
            android:ellipsize="end"
            android:maxLines="1"
            android:textColor="@color/text_primary"
            android:textSize="16sp"
            android:textStyle="bold"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toEndOf="@id/ivPoster"
            app:layout_constraintTop_toTopOf="@id/ivPoster"
            tools:text="Movie Title Here" />

        <TextView
            android:id="@+id/tvOverview"
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:layout_marginStart="12dp"
            android:layout_marginTop="6dp"
            android:layout_marginEnd="8dp"
            android:ellipsize="end"
            android:maxLines="2"
            android:textColor="@color/text_secondary"
            android:textSize="13sp"
            app:layout_constraintBottom_toBottomOf="@id/ivPoster"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toEndOf="@id/ivPoster"
            app:layout_constraintTop_toBottomOf="@id/tvTitle"
            tools:text="This is a movie overview that describes the plot." />

    </androidx.constraintlayout.widget.ConstraintLayout>

</androidx.cardview.widget.CardView>
```

**Step 3: Build to verify**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add app/src/main/res/layout-land/item_movie.xml
git commit -m "feat: create landscape layout for movie items"
```

---

### Task 14: Configure RecyclerView for Landscape

**Files:**
- Create: `app/src/main/res/layout-land/activity_main.xml`

**Step 1: Create landscape main layout**

Create file with content:
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rvMovies"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:clipToPadding="false"
        android:padding="4dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:spanCount="2"
        tools:listitem="@layout/item_movie" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

**Step 2: Update MainActivity to use GridLayoutManager for landscape**

Modify `app/src/main/java/com/example/cs388project3/MainActivity.kt`:

Change the `onCreate` method where RecyclerView is initialized:
```kotlin
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

    val isLandscape = resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    rvMovies.layoutManager = if (isLandscape) {
        GridLayoutManager(this, 2)
    } else {
        LinearLayoutManager(this)
    }

    fetchNowPlayingMovies()
}
```

**Step 3: Build to verify**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add app/src/main/res/layout-land/activity_main.xml
git add app/src/main/java/com/example/cs388project3/MainActivity.kt
git commit -m "feat: add landscape orientation with grid layout"
```

---

## Section 9: Stretch Features - Glide Placeholders

### Task 15: Create Placeholder Resources

**Files:**
- Create: `app/src/main/res/drawable/ic_movie_placeholder.xml`

**Step 1: Create placeholder drawable**

Create file with content:
```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="100dp"
    android:height="150dp"
    android:viewportWidth="100"
    android:viewportHeight="150">

    <rect
        android:width="100"
        android:height="150"
        android:fillColor="@color/background" />

    <path
        android:fillColor="@color/text_secondary"
        android:pathData="M50,70 L30,50 L30,90 Z" />

    <rect
        android:width="60"
        android:height="8"
        android:x="20"
        android:y="100"
        android:fillColor="@color/text_secondary"
        android:fillAlpha="0.3" />

    <rect
        android:width="40"
        android:height="6"
        android:x="30"
        android:y="115"
        android:fillColor="@color/text_secondary"
        android:fillAlpha="0.2" />

</vector>
```

**Step 2: Create error drawable**

Create `app/src/main/res/drawable/ic_movie_error.xml` with content:
```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="100dp"
    android:height="150dp"
    android:viewportWidth="100"
    android:viewportHeight="150">

    <rect
        android:width="100"
        android:height="150"
        android:fillColor="@color/background" />

    <path
        android:fillColor="@color/primary"
        android:pathData="M50,40 L35,65 L65,65 Z" />

    <circle
        android:cx="50"
        android:cy="85"
        android:radius="5"
        android:fillColor="@color/primary" />

    <rect
        android:width="60"
        android:height="8"
        android:x="20"
        android:y="100"
        android:fillColor="@color/text_secondary"
        android:fillAlpha="0.3" />

    <rect
        android:width="40"
        android:height="6"
        android:x="30"
        android:y="115"
        android:fillColor="@color/text_secondary"
        android:fillAlpha="0.2" />

</vector>
```

**Step 3: Build to verify**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add app/src/main/res/drawable/ic_movie_placeholder.xml
git add app/src/main/res/drawable/ic_movie_error.xml
git commit -m "feat: create custom placeholder and error drawables"
```

---

### Task 16: Update MovieAdapter with Enhanced Glide Features

**Files:**
- Modify: `app/src/main/java/com/example/cs388project3/adapter/MovieAdapter.kt`

**Step 1: Update bind method in MovieViewHolder**

Replace the `bind` method with:
```kotlin
fun bind(movie: Movie) {
    tvTitle.text = movie.title
    tvOverview.text = movie.overview

    Glide.with(itemView.context)
        .load(movie.fullPosterPath)
        .placeholder(R.drawable.ic_movie_placeholder)
        .error(R.drawable.ic_movie_error)
        .centerCrop()
        .override(200, 300)
        .into(ivPoster)
}
```

**Step 2: Build to verify**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add app/src/main/java/com/example/cs388project3/adapter/MovieAdapter.kt
git commit -m "feat: add enhanced Glide features with custom placeholders"
```

---

## Section 10: Verification - Build and Test

### Task 17: Final Build Verification

**Files:**
- None (verification task)

**Step 1: Clean build**

Run: `./gradlew clean`
Expected: BUILD SUCCESSFUL

**Step 2: Full build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 3: Verify APK generation**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL and APK generated in `app/build/outputs/apk/debug/`

**Step 4: Check for compilation errors**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git commit --allow-empty -m "test: final build verification successful"
```

---

### Task 18: Feature Verification Checklist

**Files:**
- None (documentation task)

**Step 1: Create verification documentation**

Create `VERIFICATION.md` with content:
```markdown
# Flixster+ Feature Verification

## Required Features

### ✅ Make a request to TMDB API's now_playing endpoint
- [x] AsyncHTTPClient added to dependencies
- [x] TmdbApi client created with getNowPlayingMovies() method
- [x] API key configured: a07e22bc18f5cb106bfe4cc1f83ad8ed
- [x] MainActivity fetches movies on app launch
- [x] Network call is asynchronous

### ✅ Parse through JSON data and implement RecyclerView to display all movies
- [x] Gson library added for JSON parsing
- [x] Movie data class created with Gson annotations
- [x] MovieResponse wrapper class for API response
- [x] MovieAdapter extends RecyclerView.Adapter
- [x] MovieViewHolder binds data to views
- [x] RecyclerView configured in MainActivity
- [x] Movies list displayed in RecyclerView

### ✅ Use Glide to load and display movie poster images
- [x] Glide library added to dependencies
- [x] MyAppGlideModule created for configuration
- [x] Image base URL configured: https://image.tmdb.org/t/p/w500/
- [x] Glide used in MovieAdapter to load poster images
- [x] Images displayed correctly in ImageView

## Stretch Features

### ✅ Improve and customize the user interface through styling and coloring
- [x] Custom color palette created (primary, accent, background, etc.)
- [x] App theme updated with custom colors
- [x] CardView used for movie items
- [x] Styled text views with proper colors and sizes

### ✅ Implement orientation responsivity
- [x] Landscape layout created for movie items
- [x] Landscape layout created for main activity
- [x] GridLayoutManager (2 columns) for landscape
- [x] LinearLayoutManager (1 column) for portrait
- [x] MainActivity switches layout based on orientation

### ✅ Implement Glide to display placeholder graphics during loading
- [x] Custom placeholder drawable created (ic_movie_placeholder)
- [x] Custom error drawable created (ic_movie_error)
- [x] Glide configured with placeholder()
- [x] Glide configured with error()
- [x] Image override size configured (200x300)

## Technical Implementation

### Project Structure
```
app/src/main/java/com/example/cs388project3/
├── MainActivity.kt
├── MyAppGlideModule.kt
├── adapter/
│   └── MovieAdapter.kt
├── api/
│   └── TmdbApi.kt
└── models/
    ├── Movie.kt
    └── MovieResponse.kt
```

### Layout Files
```
app/src/main/res/
├── layout/
│   ├── activity_main.xml
│   └── item_movie.xml
├── layout-land/
│   ├── activity_main.xml
│   └── item_movie.xml
├── values/
│   ├── colors.xml
│   ├── strings.xml
│   └── themes.xml
├── values-night/
│   └── themes.xml
└── drawable/
    ├── ic_movie_placeholder.xml
    └── ic_movie_error.xml
```

## Dependencies Added

```kotlin
// Network
implementation("com.codepath.libraries:asynchttpclient:2.2.0")

// Image Loading
implementation("com.github.bumptech.glide:glide:4.16.0")
annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

// JSON Parsing
implementation("com.google.code.gson:gson:2.8.6")

// RecyclerView
implementation("androidx.recyclerview:recyclerview:1.3.2")
```

## Build Verification

All builds successful:
```bash
./gradlew clean          ✅
./gradlew build          ✅
./gradlew assembleDebug  ✅
./gradlew compileDebugKotlin ✅
```

## Manual Testing Checklist

### Functionality
- [ ] App launches without crashes
- [ ] Movies load from API on startup
- [ ] Poster images display correctly
- [ ] Movie titles display correctly
- [ ] Movie overviews display correctly
- [ ] List scrolls smoothly
- [ ] Images load with placeholder during network request
- [ ] Error placeholder shows if image fails to load

### Orientation
- [ ] Portrait mode displays single column
- [ ] Landscape mode displays two columns
- [ ] Rotation maintains scroll position
- [ ] UI adapts correctly to orientation changes

### Styling
- [ ] Custom colors applied correctly
- [ ] CardView shadows visible
- [ ] Text styling consistent
- [ ] Dark mode theme works (if enabled)

## Known Limitations

- Pagination not implemented (only first page loaded)
- No search functionality
- No movie details view
- No favorites/watchlist feature
- No refresh button

## Future Enhancements

1. Implement pull-to-refresh
2. Add infinite scroll pagination
3. Add movie detail view
4. Implement favorites/watchlist
5. Add search functionality
6. Add filtering by genre
7. Add sorting options
8. Cache API responses
9. Handle network errors more gracefully
10. Add loading progress indicator
```

**Step 2: Commit verification documentation**

```bash
git add VERIFICATION.md
git commit -m "docs: add comprehensive feature verification checklist"
```

---

## Summary

This implementation plan builds Flixster+ from scratch in 18 bite-sized tasks:

1. Add required dependencies (AsyncHTTPClient, Glide, Gson, RecyclerView)
2. Add internet permission
3. Create Glide module
4. Create Movie data model
5. Create TmdbApi client
6. Create movie item layout
7. Create MovieAdapter
8. Update main activity layout
9. Integrate API and RecyclerView in MainActivity
10. Add custom colors
11. Update app theme
12. Style movie item with CardView
13. Create landscape layout for items
14. Configure RecyclerView for landscape orientation
15. Create placeholder and error drawables
16. Update MovieAdapter with enhanced Glide features
17. Final build verification
18. Feature verification documentation

Each task includes:
- Exact file paths to create/modify
- Complete code to implement
- Build verification steps
- Commit messages

All builds must succeed before committing, and final verification ensures all features work correctly.
