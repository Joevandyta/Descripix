package com.jovan.descripix.ui.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Home : Screen("home")
    data object Upload : Screen("upload")
    data object Profile : Screen("profile")
    data object DetailCaption : Screen("detail")
    data object EditProfile : Screen("editprofile")
}