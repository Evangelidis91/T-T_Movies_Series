package com.evangelidis.t_tmoviesseries.model

import com.google.gson.annotations.SerializedName

data class GenresResponse(
    @SerializedName("genres") val genres: ArrayList<Genre>?
)

data class Genre(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?
)
