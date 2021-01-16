package com.evangelidis.t_tmoviesseries.utils

object Constants {

    const val SPLASHSCREEN_TIME = 2500L
    const val INPUT_DATE_FORMAT = "yyyy-MM-dd"
    const val OUTPUT_DATE_FORMAT = "dd-MMM-yyyy"
    const val FIREBASE_DATABASE_DATE_FORMAT = "dd/MMM/yy HH:mm"
    const val FIREBASE_NEW_USER_DATE_FORMAT = "dd/M/yyyy hh:mm:ss"
    const val FIREBASE_USER_DATABASE_PATH = "users"
    const val FIREBASE_MESSAGES_DATABASE_PATH = "message"
    const val FIREBASE_MESSAGES_DATABASE_PATH_CHILD = "user-posts"

    const val ROOM_DATABASE_NAME = "watchlist.db"
    const val ROOM_WATCHLIST_TABLE_NAME = "watchlistData"
    const val DATABASE_THREAD = "dbWorkerThread"
    const val CATEGORY_MOVIE = "Movie"
    const val CATEGORY_TV = "TV"
    const val CATEGORY_DIRECTOR = "Director"
    const val CATEGORY_PERSON = "Person"

    const val BASE_URL = "https://api.themoviedb.org/3/"
    const val LANGUAGE = "en-US"
    const val API_KEY = "73bd64c984323a7ca5be2fadb11b81a6"

    const val IS_NOTIFICATION_ON = "is_notification_on"
    const val IS_SYNC_WATCHLIST_ON = "is_sync_watchlist_on"

    const val PLAYING_NOW_MOVIES = "playing_now_movies"
    const val TOP_RATED_MOVIES = "top_rated_movies"
    const val UPCOMING_MOVIES = "upcoming_movies"
    const val POPULAR_MOVIES = "popular_movies"

    const val AIRING_TODAY_TV = "airing_today_tv"
    const val TOP_RATED_TV = "top_rated_tv"
    const val ON_THE_AIR_TV = "on_the_air_tv"
    const val POPULAR_TV = "popular_tv"

    const val IMAGE_POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500"
    const val IMAGE_SMALL_BASE_URL = "https://image.tmdb.org/t/p/w154"
    const val YOUTUBE_VIDEO_URL = "https://www.youtube.com/watch?v=%s"
    const val YOUTUBE_THUMBNAIL_URL = "https://img.youtube.com/vi/%s/0.jpg"

    const val IS_LOGGED_IN = "isLogged"
    const val IS_LOGIN_SKIPPED = "isLoginSkipped"

    const val MOVIE_ID = "movie_id"
    const val TV_SHOW_ID = "tv_show_id"
    const val PERSON_ID = "person_id"

    const val TOTAL_SEASONS = "total_seasons"
    const val TV_SHOW_NAME = "tv_show_name"
}
