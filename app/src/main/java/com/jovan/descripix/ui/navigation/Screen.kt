package com.jovan.descripix.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Upload : Screen("upload")
    data object Profile : Screen("profile")
    data object DetailCaption : Screen("detail")
}