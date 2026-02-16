package com.example.cs388project3.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

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
) : Parcelable {
    val fullPosterPath: String
        get() = "https://image.tmdb.org/t/p/w500/$posterPath"

    val fullBackdropPath: String
        get() = "https://image.tmdb.org/t/p/w500/$backdropPath"

    constructor(parcel: Parcel) : this(
        parcel.readByte() != 0.toByte(),
        parcel.readString(),
        parcel.createIntArray()?.toList(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readDouble(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (adult == true) 1 else 0)
        parcel.writeString(backdropPath)
        parcel.writeIntArray(genreIds?.toIntArray())
        parcel.writeInt(id ?: 0)
        parcel.writeString(originalLanguage)
        parcel.writeString(originalTitle)
        parcel.writeString(overview)
        parcel.writeDouble(popularity ?: 0.0)
        parcel.writeString(posterPath)
        parcel.writeString(releaseDate)
        parcel.writeString(title)
        parcel.writeByte(if (video == true) 1 else 0)
        parcel.writeDouble(voteAverage ?: 0.0)
        parcel.writeInt(voteCount ?: 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Movie> {
        override fun createFromParcel(parcel: Parcel): Movie = Movie(parcel)
        override fun newArray(size: Int): Array<Movie?> = arrayOfNulls(size)
    }
}
