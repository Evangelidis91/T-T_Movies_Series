package com.evangelidis.t_tmoviesseries.model

import com.google.gson.annotations.SerializedName

data class TvShowSeasonResponse(
    @SerializedName("air_date")
    val airDate: String?,
    @SerializedName("episodes")
    val episodes: List<Episode>?,
    @SerializedName("_id")
    val _id: String?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("overview")
    val overview: String?,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("season_number")
    val seasonNumber: Int?
)

data class Episode(
    @SerializedName("air_date")
    val airDate: String?,
    @SerializedName("crew")
    val crew: List<TvShowCrew>?,
    @SerializedName("episode_number")
    val episodeNumber: Int?,
    @SerializedName("guest_stars")
    val guestStars: List<GuestStar>?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("overview")
    val overview: String?,
    @SerializedName("production_code")
    val productionCode: String?,
    @SerializedName("season_number")
    val seasonNumber: Int?,
    @SerializedName("show_id")
    val showId: Int?,
    @SerializedName("still_path")
    val stillPath: String?,
    @SerializedName("vote_average")
    val voteAverage: Double?,
    @SerializedName("vote_count")
    val voteCount: Int?
)

data class GuestStar(
    @SerializedName("character")
    val character: String?,
    @SerializedName("credit_id")
    val creditId: String?,
    @SerializedName("gender")
    val gender: Int?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("order")
    val order: Int?,
    @SerializedName("profile_path")
    val profilePath: String?
)