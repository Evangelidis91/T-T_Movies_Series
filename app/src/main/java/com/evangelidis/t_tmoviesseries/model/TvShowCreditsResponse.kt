package com.evangelidis.t_tmoviesseries.model

import com.google.gson.annotations.SerializedName

data class TvShowCreditsResponse(
    @SerializedName("cast") val cast: List<TvShowCast>?,
    @SerializedName("crew") val crew: List<TvShowCrew>?,
    @SerializedName("id") val id: Int?
)

data class TvShowCast(
    @SerializedName("character") val character: String?,
    @SerializedName("credit_id") val creditId: String?,
    @SerializedName("gender") val gender: Int?,
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("order") val order: Int?,
    @SerializedName("profile_path") val profilePath: String?
)

data class TvShowCrew(
    @SerializedName("credit_id") val creditId: String?,
    @SerializedName("department") val department: String?,
    @SerializedName("gender") val gender: Int?,
    @SerializedName("id") val id: Int?,
    @SerializedName("job") val job: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("profile_path") val profilePath: String?
)
