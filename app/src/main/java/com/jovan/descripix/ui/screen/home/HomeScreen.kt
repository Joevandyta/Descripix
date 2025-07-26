package com.jovan.descripix.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jovan.descripix.R
import com.jovan.descripix.data.source.local.entity.CaptionEntity
import com.jovan.descripix.ui.common.UiState
import com.jovan.descripix.ui.component.CaptionItem
import com.jovan.descripix.ui.component.LottieAnimationPreload
import com.jovan.descripix.ui.component.ShimmerListItem
import com.jovan.descripix.ui.theme.DescripixTheme
import com.jovan.descripix.ui.common.TestTags

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    navigateToDetail: (CaptionEntity) -> Unit,
) {
    val context = LocalContext.current
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    val isConnected by viewModel.isConnected.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.resetAllStates()
        viewModel.getSession(isConnected, context)
    }

    when (sessionState) {
        is UiState.Loading -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(Color.Transparent)
                    .fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        }

        is UiState.Success -> {
            val session = (sessionState as UiState.Success).data
            if (session.isLogin) {
                LaunchedEffect(key1 = true) {
                    viewModel.getAllCaptions(isConnected, session.token, context)
                }

                AutenticatedScreen(
                    modifier = modifier.testTag(TestTags.AUTHENTICATED_SCREEN),
                    navigateToDetail = navigateToDetail
                )
            } else {
                GuestScreen(
                    modifier = modifier
                        .testTag(TestTags.GUEST_SCREEN),
                    onLoginClicked = {
                        viewModel.login(context)
                    }
                )
            }
        }

        is UiState.Error -> {}
    }
}

@Composable
fun GuestScreen(
    modifier: Modifier = Modifier,
    onLoginClicked: () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LocalContext.current

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
            initialOffsetY = { fullHeight -> -fullHeight },
            animationSpec = tween(500)
        ),
        exit = fadeOut()
    ) {
        ConstraintLayout(modifier = modifier) {
            val (header, buttonLayout, textHeader, image) = createRefs()
            val arcColor = MaterialTheme.colorScheme.primaryContainer
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(header) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            ) {
                val (box, arc) = createRefs()
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .constrainAs(box) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        },
                    onDraw = {
                        drawRect(
                            color = arcColor,
                            size = Size(size.width, 200.dp.toPx()), // ukuran rect, sesuaikan
                            topLeft = Offset(0f, size.height - 200.dp.toPx())
                        )
                    })
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .constrainAs(arc) {
                            top.linkTo(box.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        },
                    onDraw = {
                        drawArc(
                            color = arcColor,
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = true,
                            topLeft = Offset(0f, -size.height),
                            size = Size(size.width, size.height * 2),
                        )
                    })
            }
            Text(
                text = stringResource(R.string.Home_fragment_guest_view),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 24.sp,
                    fontFamily = FontFamily(Font(R.font.righteous))
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .constrainAs(textHeader) {
                        bottom.linkTo(buttonLayout.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            )
            Button(
                onClick = onLoginClicked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .wrapContentWidth()
                    .constrainAs(buttonLayout) {
                        top.linkTo(header.bottom)
                        bottom.linkTo(header.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .padding(start = 8.dp, end = 8.dp)
                    .testTag(TestTags.SIGN_IN_BUTTON),
                elevation = ButtonDefaults.buttonElevation(2.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = stringResource(R.string.sign_in_button)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.sign_in),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .constrainAs(image) {
                        top.linkTo(buttonLayout.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            ) {
                LottieAnimationPreload(
                    animationResId = R.raw.login_animation,
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GuestScreenPreview() {

    DescripixTheme {
        GuestScreen(
            onLoginClicked = {}
        )
    }
}

@Composable
fun AutenticatedScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    navigateToDetail: (CaptionEntity) -> Unit
) {
    val context = LocalContext.current
    val captionListState by viewModel.captionListState.collectAsStateWithLifecycle()
    val captionDetailState by viewModel.captionDetailState.collectAsStateWithLifecycle()
    val captionItems = (captionListState as? UiState.Success)?.data.orEmpty()
    val isLoading = captionListState is UiState.Loading

    LaunchedEffect(captionDetailState) {
        when (val state = captionDetailState) {
            is UiState.Success -> {
                val captionData = state.data.data
                if (captionData != null) {
                    val captionEntity = CaptionEntity(
                        id = captionData.id,
                        caption = captionData.caption,
                        author = captionData.author ?: "",
                        date = captionData.date ?: "",
                        location = captionData.location ?: "",
                        device = captionData.device ?: "",
                        model = captionData.model ?: "",
                        image = captionData.image
                    )

                    viewModel.resetCaptionDetail()
                    navigateToDetail(captionEntity)
                } else {
                    viewModel.resetCaptionDetail()
                }
            }

            is UiState.Error -> {
                viewModel.resetCaptionDetail()
            }

            else -> {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        if (!isLoading && captionItems.isEmpty()) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        LottieAnimationPreload(
                            animationResId = R.raw.lottie_empty_list,
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.Center)
                        )
                    }
                    Spacer(modifier = Modifier.size(8.dp))

                    Text(
                        text = stringResource(R.string.you_don_t_have_any_captions_yet),
                        fontSize = 24.sp
                    )
                }
            }
        }

        LazyColumn(
            modifier = modifier.fillMaxSize(),
        ) {
            if (isLoading) {
                items(10) {
                    ShimmerListItem()
                }
            } else {
                items(
                    items = captionItems,
                    key = { it.id }) { item ->
                    val imageUrl = remember(item.image) { item.image }
                    CaptionItem(
                        imageUrl = imageUrl,
                        caption = item.caption.toString(),
                        navigateToDetail = {
                            val captionId = item.id
                            viewModel.getCaptionDetail(captionId, context)
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {

    DescripixTheme {
        AutenticatedScreen(
//            viewModel = hiltViewModel(),
            navigateToDetail = {}
        )
    }
}