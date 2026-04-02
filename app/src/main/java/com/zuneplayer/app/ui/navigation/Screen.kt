package com.zuneplayer.app.ui.navigation

sealed class Screen(val route: String, val title: String) {
    data object Home : Screen("home", "Now Playing")
    data object Songs : Screen("songs", "Songs")
    data object Mix : Screen("mix", "Mix")
    data object Albums : Screen("albums", "Albums")
    data object Artists : Screen("artists", "Artists")
    data object Search : Screen("search", "Search")
    data object Settings : Screen("settings", "Settings")
    data object ArtistDetail : Screen("artist/{artistName}", "Artist") {
        fun createRoute(artistName: String) = "artist/$artistName"
    }
    data object AlbumDetail : Screen("album/{albumName}", "Album") {
        fun createRoute(albumName: String) = "album/$albumName"
    }
}
