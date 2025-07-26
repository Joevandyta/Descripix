package com.jovan.descripix

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jovan.descripix.ui.navigation.NavigationItem
import com.jovan.descripix.ui.navigation.Screen
import com.jovan.descripix.ui.screen.detail.DetailScreen
import com.jovan.descripix.ui.screen.detail.DetailsViewModel
import com.jovan.descripix.ui.screen.home.HomeScreen
import com.jovan.descripix.ui.screen.profile.ProfileScreen
import com.jovan.descripix.ui.theme.DescripixTheme

@Composable
fun DescripixApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: DetailsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val captionEntityState by viewModel.captionEntityState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (currentRoute == Screen.Home.route || currentRoute == Screen.Profile.route) BottomBar(
                navController
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,

            modifier = Modifier.padding(innerPadding)
        ) {

            composable(Screen.Home.route) {
                HomeScreen(
                    navigateToDetail = { captionEntity ->
                        viewModel.setCaptionEntity(captionEntity)
                        navController.navigate(Screen.DetailCaption.route)
                    }
                )
            }
            composable(Screen.Upload.route) {

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument(),
                    onResult = { uri ->
                        uri?.let {
                            context.contentResolver.takePersistableUriPermission(
                                it,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                            val mimeType = context.contentResolver.getType(it)
                            if (mimeType != null && mimeType.startsWith("image/")) {
                                viewModel.extractImageMetadata(context, it)
                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.file_is_not_supported),
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.popBackStack()
                            }
                        } ?: run {
                            Toast.makeText(
                                context,
                                context.getString(R.string.there_is_no_image_selected),
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.popBackStack() // Balik ke layar sebelumnya

                        }
                    }
                )
                LaunchedEffect(Unit) {
                    launcher.launch(arrayOf("image/*"))
                }
                LaunchedEffect(captionEntityState) {
                    if (captionEntityState != null) {
                        navController.navigate(Screen.DetailCaption.route) {
                            popUpTo(Screen.Upload.route) {
                                inclusive = true
                            }
                        }
                    }
                }
            }
            composable(Screen.Profile.route) {
                ProfileScreen()
            }
            composable(Screen.DetailCaption.route) {

                captionEntityState?.let { data ->
                    DetailScreen(
                        captionEntity = data,
                        onBack = {
                            navController.popBackStack()
                            viewModel.clearCaptionEntity()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    ) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route



    NavigationBar(modifier = modifier) {
        val navigationItems = listOf(
            NavigationItem(
                title = stringResource(R.string.menu_home),
                icon = Icons.Default.Home,
                screen = Screen.Home
            ),
            NavigationItem(
                title = stringResource(R.string.menu_upload),
                icon = Icons.Default.AddCircle,
                screen = Screen.Upload
            ),
            NavigationItem(
                title = stringResource(R.string.menu_profile),
                icon = Icons.Default.AccountCircle,
                screen = Screen.Profile
            ),
        )

        navigationItems.map { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) },
                selected = currentRoute == item.screen.route,
                onClick = {

                    navController.navigate(item.screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        restoreState = true
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DescripixPreview() {
    DescripixTheme {
        DescripixApp()
    }
}