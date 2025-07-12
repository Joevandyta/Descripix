package com.jovan.descripix.ui.screen.profile

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jovan.descripix.R
import com.jovan.descripix.data.source.local.entity.UserEntity
import com.jovan.descripix.data.source.remote.request.UserRequest
import com.jovan.descripix.domain.model.Language
import com.jovan.descripix.ui.common.UiState
import com.jovan.descripix.ui.component.ComingSoonModal
import com.jovan.descripix.ui.component.LogoutModal
import com.jovan.descripix.ui.component.SettingCard
import com.jovan.descripix.ui.component.TaskFailedModal
import com.jovan.descripix.ui.theme.DescripixTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ModalType {
    COMING_SOON, EDIT_PROFILE, TASKFAILED, LOGOUT
}

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {

    val context = LocalContext.current
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    val logoutState by viewModel.logoutState.collectAsStateWithLifecycle()
    val isConnected by viewModel.isConnected.collectAsStateWithLifecycle()
    val updateUserState by viewModel.updateUserState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.initializeLanguage(context)
        viewModel.resetAllStates()
        viewModel.getSession(isConnected, context)
    }
    when (sessionState) {
        is UiState.Loading -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        }

        is UiState.Success -> {
            val session = (sessionState as UiState.Success)
            if (session.data.isLogin) {
                LaunchedEffect(session.data.token) {
                    viewModel.getUserDetail(
                        isConnected,
                        session.data.refreshToken,
                        session.data.token,
                        context
                    )
                    Log.d("ProfileScreen", "Fetching user detail")
                }
                LaunchedEffect(updateUserState) {
                    if (updateUserState is UiState.Success) {
                        viewModel.getUserDetail(
                            isConnected,
                            session.data.refreshToken,
                            session.data.token,
                            context
                        )
                    }
                }
                AutenticatedDisplay(modifier = modifier)

                if (logoutState is UiState.Success) {
                    Log.d("ProfileScreen", "Logout Success")
                }
            } else {
                GuestDisplay()
            }
        }

        is UiState.Error -> {}

    }
}

@Composable
fun GuestDisplay(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val primaryColor = MaterialTheme.colorScheme.primary
    var visibleModal by remember { mutableStateOf<ModalType?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val currentLanguageState by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val languages = Language.getAllLanguages()
    if (visibleModal != null) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    visibleModal = null
                }
        )
    }
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            val (rectangle, circleLeft, rectLeft, rectRight, circleRight, layoutProfile) = createRefs()

            Box(
                modifier = Modifier
                    .constrainAs(rectangle) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .height(128.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
            )
            Canvas(
                modifier = Modifier
                    .constrainAs(circleLeft) {
                        top.linkTo(rectangle.bottom)
                        bottom.linkTo(rectangle.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                drawArc(
                    color = primaryColor,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    size = Size(size.width, size.height),
                    topLeft = Offset(0f, 0f),
                    style = Fill
                )
            }
            Canvas(
                modifier = Modifier
                    .constrainAs(rectRight) {
                        top.linkTo(rectangle.bottom)
                        start.linkTo(layoutProfile.end)
                        end.linkTo(parent.end)
                    }
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                drawRect(
                    color = primaryColor,
                    size = Size(size.width, size.height),
                    topLeft = Offset(0f, 0f),
                    style = Fill
                )
            }
            Canvas(
                modifier = Modifier
                    .constrainAs(circleRight) {
                        top.linkTo(circleLeft.bottom)
                        start.linkTo(rectLeft.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
                    .height(42.dp)
            ) {
                drawArc(
                    color = backgroundColor,
                    startAngle = 0f,
                    sweepAngle = -180f,
                    useCenter = false,
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, size.height),
                    style = Fill
                )
            }
            Canvas(
                modifier = Modifier
                    .constrainAs(rectLeft) {
                        top.linkTo(circleRight.top)
                        start.linkTo(parent.start)
                        end.linkTo(layoutProfile.start)
                    }
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                drawRect(
                    color = backgroundColor,
                    size = Size(size.width, size.height),
                    topLeft = Offset(0f, 0f),
                    style = Fill
                )
            }

            // Gambar profil (bulat)
            Box(
                modifier = Modifier
                    .constrainAs(layoutProfile) {
                        top.linkTo(rectangle.bottom)
                        bottom.linkTo(circleRight.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .padding(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = CircleShape
                    )
            ) {
                Image(
                    painter = painterResource(R.drawable.profile_placeholder),
                    contentDescription = "User Profile",
                    modifier = Modifier
                        .size(150.dp)
                        .padding(8.dp)
                        .clip(CircleShape)
                )
            }
        }

        Text(
            text = stringResource(R.string.you_are_not_authenticated_click_button_below_to_sign_in),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )

        Button(
            onClick = {
                //TODO
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = "Sign in"
            )
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer),
            ) {
                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    val (language, divider2, about, languageModal) = createRefs()

                    Box(
                        modifier = Modifier
                            .constrainAs(language) {
                                top.linkTo(parent.top)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            },
                        contentAlignment = Alignment.TopEnd
                    ) {
                        val currentLanguage = (currentLanguageState as UiState.Success).data
                        SettingCard(
                            painterResource(R.drawable.ic_language),
                            stringResource(R.string.language) + ": ${currentLanguage.name}",
                            onClick = {
                                expanded = !expanded
                            },
                        )

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            languages.forEach { selectedLanguage ->
                                DropdownMenuItem(
                                    text = { Text(selectedLanguage.name) },
                                    onClick = {
                                        expanded = false
                                        viewModel.changeLanguage(context, selectedLanguage.code)
                                    },
                                    trailingIcon = if (currentLanguage.code == selectedLanguage.code) {
                                        { Icon(Icons.Default.Check, contentDescription = null) }
                                    } else null,
                                )
                            }
                        }
                    }
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 0.dp)
                            .constrainAs(divider2) {
                                top.linkTo(language.bottom)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }
                    )
                    SettingCard(
                        painterResource(R.drawable.ic_info),
                        stringResource(R.string.about_app),
                        onClick = {
                            visibleModal = ModalType.COMING_SOON
                        },
                        modifier = Modifier
                            .constrainAs(about) {
                                top.linkTo(divider2.bottom)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }
                    )
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visibleModal == ModalType.COMING_SOON,
            enter = scaleIn(tween(300)) + fadeIn(tween(300)),
            exit = scaleOut(tween(200)) + fadeOut(tween(200))
        ) {
            ComingSoonModal(
                onClick = {
                    visibleModal = null
                },
                modifier = Modifier
                    .width(300.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutenticatedDisplay(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val primaryColor = MaterialTheme.colorScheme.primary
    var visibleModal by remember { mutableStateOf<ModalType?>(null) }

    val userDetailState by viewModel.userDetailState.collectAsStateWithLifecycle()
    val updateUserState by viewModel.updateUserState.collectAsStateWithLifecycle()
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    val isConnected by viewModel.isConnected.collectAsStateWithLifecycle()
    val logoutState by viewModel.logoutState.collectAsStateWithLifecycle()
    val currentLanguageState by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    var isButtonAcivated by remember { mutableStateOf(true) }
    var isLanguageExpanded by remember { mutableStateOf(false) }

    when (logoutState) {
        is UiState.Loading -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                LinearProgressIndicator()
            }
        }

        is UiState.Success -> {
            Log.d("ProfileScreen", "Logout Success")
        }

        is UiState.Error -> {}
    }
    when (userDetailState) {
        is UiState.Loading -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
            Log.d("ProfileScreen", "Loading User Detail")
        }

        is UiState.Success -> {
            val userDetail = (userDetailState as UiState.Success).data
            Column(
                modifier = modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    val (rectangle, circleLeft, rectLeft, rectRight, circleRight, layoutProfile) = createRefs()

                    Box(
                        modifier = Modifier
                            .constrainAs(rectangle) {
                                top.linkTo(parent.top)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }
                            .height(128.dp)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Canvas(
                        modifier = Modifier
                            .constrainAs(circleLeft) {
                                top.linkTo(rectangle.bottom)
                                bottom.linkTo(rectangle.bottom)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        drawArc(
                            color = primaryColor,
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = false,
                            size = Size(size.width, size.height),
                            topLeft = Offset(0f, 0f),
                            style = Fill
                        )
                    }
                    Canvas(
                        modifier = Modifier
                            .constrainAs(rectRight) {
                                top.linkTo(rectangle.bottom)
                                start.linkTo(layoutProfile.end)
                                end.linkTo(parent.end)
                            }
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        drawRect(
                            color = primaryColor,
                            size = Size(size.width, size.height),
                            topLeft = Offset(0f, 0f),
                            style = Fill
                        )
                    }
                    Canvas(
                        modifier = Modifier
                            .constrainAs(circleRight) {
                                top.linkTo(circleLeft.bottom)
                                start.linkTo(rectLeft.start)
                                end.linkTo(parent.end)
                                width = Dimension.fillToConstraints
                            }
                            .height(42.dp)
                    ) {
                        drawArc(
                            color = backgroundColor,
                            startAngle = 0f,
                            sweepAngle = -180f,
                            useCenter = false,
                            topLeft = Offset(0f, 0f),
                            size = Size(size.width, size.height),
                            style = Fill
                        )
                    }
                    Canvas(
                        modifier = Modifier
                            .constrainAs(rectLeft) {
                                top.linkTo(circleRight.top)
                                start.linkTo(parent.start)
                                end.linkTo(layoutProfile.start)
                            }
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        drawRect(
                            color = backgroundColor,
                            size = Size(size.width, size.height),
                            topLeft = Offset(0f, 0f),
                            style = Fill
                        )
                    }

                    // Gambar profil (bulat)
                    Box(
                        modifier = Modifier
                            .constrainAs(layoutProfile) {
                                top.linkTo(rectangle.bottom)
                                bottom.linkTo(circleRight.bottom)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }
                            .padding(4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = CircleShape
                            )
                    ) {
                        AsyncImage(
                            model = userDetail.profileImg,
                            contentDescription = "User Profile",
                            modifier = Modifier
                                .size(150.dp)
                                .padding(8.dp)
                                .clip(CircleShape)
                        )
                    }
                }

                Text(
                    text = userDetail.username,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 26.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = userDetail.email,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        IconButton(
                            onClick = {
                                visibleModal = ModalType.EDIT_PROFILE
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                modifier = Modifier
                                    .padding(4.dp)
                            )
                        }
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "About Me",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = userDetail.aboutMe
                                    ?: stringResource(R.string.no_information_provided),
                                textAlign = TextAlign.Justify,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }

                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                ) {
                    Card(
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .padding(top = 8.dp, bottom = 16.dp)
                        ) {
                            Text(
                                text = "Birth Date",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = userDetail.birthDate
                                    ?: stringResource(R.string.no_information_provided),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Card(
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
                    ) {

                        Column(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .padding(top = 8.dp, bottom = 16.dp)
                        ) {
                            Text(
                                text = "Gender",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = userDetail.gender
                                    ?: stringResource(R.string.no_information_provided), // Data dummy, ganti dengan data sebenarnya
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer),
                ) {
                    ConstraintLayout(
                        modifier
                            .fillMaxWidth()
                    ) {
                        val (language, divider2, about, divider3, logout, languageModal) = createRefs()

                        Box(
                            modifier = Modifier
                                .constrainAs(language) {
                                    top.linkTo(parent.top)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                },
                            contentAlignment = Alignment.TopEnd
                        ) {
                            val languages = Language.getAllLanguages()
                            val currentLanguage = (currentLanguageState as UiState.Success).data
                            SettingCard(
                                painterResource(R.drawable.ic_language),
                                stringResource(R.string.language) + ": ${currentLanguage.name}",
                                onClick = {
                                    isLanguageExpanded = !isLanguageExpanded
                                },
                            )

                            DropdownMenu(
                                expanded = isLanguageExpanded,
                                onDismissRequest = { isLanguageExpanded = false },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                languages.forEach { selectedLanguage ->
                                    DropdownMenuItem(
                                        text = { Text(selectedLanguage.name) },
                                        onClick = {
                                            isLanguageExpanded = false
                                            viewModel.changeLanguage(context, selectedLanguage.code)
                                        },
                                        trailingIcon = if (currentLanguage.code == selectedLanguage.code) {
                                            { Icon(Icons.Default.Check, contentDescription = null) }
                                        } else null,
                                    )
                                }
                            }
                        }
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 0.dp)
                                .constrainAs(divider2) {
                                    top.linkTo(language.bottom)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                }
                        )
                        SettingCard(
                            painterResource(R.drawable.ic_info),
                            stringResource(R.string.about_app),
                            onClick = {
                                visibleModal = ModalType.COMING_SOON
                            },
                            modifier = Modifier
                                .constrainAs(about) {
                                    top.linkTo(divider2.bottom)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                }
                        )

                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 0.dp)
                                .constrainAs(divider3) {
                                    top.linkTo(about.bottom)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                }
                        )

                        SettingCard(
                            painterResource(R.drawable.ic_logout),
                            stringResource(R.string.logout),
                            onClick = {
                                visibleModal = ModalType.LOGOUT
                                Log.d("ProfileScreen", "Logout")
                            },
                            modifier = Modifier
                                .constrainAs(logout) {
                                    top.linkTo(divider3.bottom)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                }
                        )

                    }
                }
            }

            if (visibleModal != null) {

                Log.d("ProfileScreen", "Visible MOdal = $visibleModal")
                AnimatedVisibility(
                    visible = visibleModal != null,
                    enter = scaleIn(tween(300)) + fadeIn(tween(300)),
                    exit = scaleOut(tween(200)) + fadeOut(tween(200))
                ) {
                    BasicAlertDialog(
                        onDismissRequest = { visibleModal = null },
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        properties = DialogProperties(
                            usePlatformDefaultWidth = false,
                            dismissOnBackPress = false,
                            dismissOnClickOutside = false,
                        )
                    ) {
                        when (visibleModal) {
                            ModalType.COMING_SOON -> {
                                ComingSoonModal(
                                    onClick = {
                                        visibleModal = null
                                    },
                                    modifier = Modifier
                                        .width(300.dp)
                                )
                            }
                            ModalType.EDIT_PROFILE -> {
                                EditProfile(
                                    onConfirmClicked = { newUserData ->
                                        Log.d(
                                            "ProfileScreen-onConfirmClicked",
                                            "New User Data: $newUserData"
                                        )
                                        val session = (sessionState as UiState.Success)
                                        Log.d("ProfileScreen-onConfirmClicked", "New User Data: $session")

                                        if (session.data.isLogin) {
                                            viewModel.updateUserDetail(
                                                newUserData,
                                                token = session.data.token,
                                                context = context
                                            )
                                        }
                                    },
                                    isButtonAcivated = isButtonAcivated,
                                    onDismissClicked = {
                                        visibleModal = null
                                    },
                                    initialUser = userDetail,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                )
                            }
                            ModalType.TASKFAILED -> {
                                TaskFailedModal(
                                    text = stringResource(R.string.please_try_again),
                                    onClick = {
                                        visibleModal = null
                                    },
                                    modifier = Modifier
                                        .width(300.dp)
                                )
                            }
                            ModalType.LOGOUT -> {
                                LogoutModal(
                                    onLogoutConfirm = {
                                        viewModel.logout(context)
                                    },
                                    onDismiss = {
                                        visibleModal = null
                                    }
                                )
                            }
                            null -> {}
                        }
                    }
                }
            }

            when (updateUserState) {
                is UiState.Loading -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LinearProgressIndicator()
                    }
                    isButtonAcivated = false
                }

                is UiState.Success -> {
                    Log.d("ProfileScreen - updateUserResponse", "Success")
                    val updateResponse = (updateUserState as UiState.Success).data
                    if (updateResponse.status) {
                        LaunchedEffect(updateUserState) {
                            visibleModal = null
                        }
                    } else {
                        LaunchedEffect(updateResponse) {
                            visibleModal = ModalType.TASKFAILED
                        }
                        Log.d("ProfileScreen", "Visible MOdal = $visibleModal")
                    }
                    isButtonAcivated = true
                }
                is UiState.Error -> {
                    isButtonAcivated = true
                }
            }
        }
        is UiState.Error -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfile(
    modifier: Modifier = Modifier,
    initialUser: UserEntity?,
    onDismissClicked: () -> Unit,
    onConfirmClicked: (UserRequest) -> Unit,
    isButtonAcivated: Boolean,
) {
    var aboutText by remember { mutableStateOf(initialUser?.aboutMe ?: "") }
    var gender by remember { mutableStateOf(initialUser?.gender ?: "") }
    var birthdate by remember { mutableStateOf(initialUser?.birthDate ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showGenderMenu by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val genderOptions = listOf(
        stringResource(R.string.string_display_male),
        stringResource(R.string.string_display_female)
    )


    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer),
    ) {
        ConstraintLayout(
            modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
        ) {
            val (titleText, editAbout, editGender, editBirthdate, confirmButton) = createRefs()

            Text(
                text = "Edit Profile",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.constrainAs(titleText) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            )


            OutlinedTextField(
                value = aboutText,
                onValueChange = { aboutText = it },
                label = { Text(stringResource(R.string.about_me)) },
                placeholder = { Text("Tell us about yourself...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .constrainAs(editAbout) {
                        top.linkTo(titleText.bottom, margin = 8.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                maxLines = 4,
                singleLine = false,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            // Gender section
            ExposedDropdownMenuBox(
                expanded = showGenderMenu,
                onExpandedChange = { showGenderMenu = !showGenderMenu },
                modifier = Modifier.constrainAs(editGender) {
                    top.linkTo(editAbout.bottom, margin = 8.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            ) {
                OutlinedTextField(
                    value = gender,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Gender") },
                    placeholder = { Text("Select gender") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showGenderMenu)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                ExposedDropdownMenu(
                    expanded = showGenderMenu,
                    onDismissRequest = { showGenderMenu = false }
                ) {
                    genderOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                gender = option
                                showGenderMenu = false
                            }
                        )
                    }
                }
            }

            // Birthdate section
            OutlinedTextField(
                value = birthdate,
                onValueChange = { },
                readOnly = true,
                label = { Text("Birthdate") },
                placeholder = { Text("Select birthdate") },
                trailingIcon = {
                    IconButton(onClick = {
                        Log.d("EditProfile", "Icon birthdate clicked")
                        showDatePicker = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select date"
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(editBirthdate) {
                        top.linkTo(editGender.bottom, margin = 8.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .clickable {
                        Log.d("EditProfile", "birthdate clicked")
                        showDatePicker = true
                    },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .constrainAs(confirmButton) {
                        top.linkTo(editBirthdate.bottom, margin = 8.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
            ) {
                Button(
                    onClick = onDismissClicked,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    enabled = isButtonAcivated,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text("Cancel")
                }
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.5f)
                )
                Button(
                    onClick = {
                        val updatedUser = UserRequest(
                            gender = gender,
                            birthDate = birthdate,
                            aboutMe = aboutText
                        )
                        onConfirmClicked(updatedUser)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    enabled = isButtonAcivated,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Confirm")
                }

            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {

                TextButton(
                    onClick = {
                        val selectedDate = datePickerState.selectedDateMillis
                        selectedDate?.let {
                            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                            val formattedDate = formatter.format(Date(it))
                            birthdate = formattedDate
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}



@Preview(showBackground = true)
@Composable
fun PreviewAuthDisplay() {
    DescripixTheme {
        EditProfile(
            onConfirmClicked = {},
            initialUser = UserEntity(
                id = "fds",
                username = "dasda",
                email = "ddasdad",
            ),
            onDismissClicked = {},
            isButtonAcivated = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingCard() {
    DescripixTheme {
        GuestDisplay()
    }
}