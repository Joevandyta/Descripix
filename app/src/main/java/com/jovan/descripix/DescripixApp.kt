package com.jovan.descripix

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jovan.descripix.ui.common.TestTags
import com.jovan.descripix.ui.navigation.NavigationItem
import com.jovan.descripix.ui.navigation.Screen
import com.jovan.descripix.ui.screen.detail.DetailScreen
import com.jovan.descripix.ui.screen.detail.DetailsViewModel
import com.jovan.descripix.ui.screen.home.HomeScreen
import com.jovan.descripix.ui.screen.profile.ProfileScreen
import com.jovan.descripix.ui.theme.DescripixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DescripixApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: DetailsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val captionEntityState by viewModel.captionEntityState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (currentRoute != Screen.Upload.route) {
                TopBar(
                    currentRoute = currentRoute,
                    navController = navController,
                    scrollBehavior = scrollBehavior,)
            }
        },
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
                    },
                    modifier = Modifier.padding(innerPadding)
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
                    viewModel.openImagePicker.collect {
                        launcher.launch(arrayOf("image/*"))
                    }
                }
                LaunchedEffect(Unit) {
                    viewModel.requestPickImage(context)
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
                ProfileScreen(modifier = Modifier.padding(innerPadding))
            }
            composable(Screen.DetailCaption.route) {

                captionEntityState?.let { data ->
                    DetailScreen(
                        captionEntity = data,
                        onBack = {
                            navController.popBackStack()
                            viewModel.clearCaptionEntity()
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    currentRoute: String?,
    navController: NavHostController,
    scrollBehavior: TopAppBarScrollBehavior
) {
    val topBarColor = when (currentRoute) {
        Screen.Home.route -> MaterialTheme.colorScheme.primaryContainer
        Screen.Profile.route -> MaterialTheme.colorScheme.primary

        else -> MaterialTheme.colorScheme.surface
    }
    val topBarTextColor = when (currentRoute) {
        Screen.Home.route -> MaterialTheme.colorScheme.onPrimaryContainer
        Screen.Profile.route -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }
    CenterAlignedTopAppBar(
        modifier = Modifier.padding(0.dp),
        title = {
            Text(
                text = when (currentRoute) {
                    Screen.Home.route -> stringResource(R.string.menu_home)
                    Screen.Profile.route -> stringResource(R.string.menu_profile)
                    Screen.DetailCaption.route -> stringResource(R.string.menu_detail)
                    else -> ""
                }

            )
        },
        navigationIcon = {
            if (currentRoute == Screen.DetailCaption.route) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = topBarColor,
            titleContentColor = topBarTextColor,
            navigationIconContentColor = topBarTextColor,
            scrolledContainerColor = MaterialTheme.colorScheme.background
        )
    )
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
                        contentDescription = item.title,
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
                },
                modifier = Modifier.testTag(item.screen.route + TestTags.BOTTOM_BAR_ICON)
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