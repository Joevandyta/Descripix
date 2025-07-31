package com.jovan.descripix.ui.screen.detail

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jovan.descripix.R
import com.jovan.descripix.data.source.local.entity.CaptionEntity
import com.jovan.descripix.ui.common.ModalType
import com.jovan.descripix.ui.common.SocialMediaPackage
import com.jovan.descripix.ui.common.UiState
import com.jovan.descripix.ui.component.DateTimePickerModal
import com.jovan.descripix.ui.component.FloatingToolbar
import com.jovan.descripix.ui.component.MetadataDateItem
import com.jovan.descripix.ui.component.MetadataItem
import com.jovan.descripix.ui.component.ShareList
import com.jovan.descripix.ui.component.TaskFailedModal
import com.jovan.descripix.ui.theme.DescripixTheme
import com.jovan.descripix.utils.shareContent
import com.jovan.descripix.utils.shareToSpecificApp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    captionEntity: CaptionEntity,
    modifier: Modifier = Modifier,
    viewModel: DetailsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val generatedCaptionState by viewModel.generatedCaption.collectAsStateWithLifecycle()
    val savedCaptionState by viewModel.saveCaption.collectAsStateWithLifecycle()
    val editCaptionState by viewModel.editCaption.collectAsStateWithLifecycle()
    val deleteCaptionState by viewModel.deleteCaption.collectAsStateWithLifecycle()

    var isShareExpanded by remember { mutableStateOf(false) }
    var isInitialCaptionSave by remember { mutableStateOf(false) }
    var toggleSaveActive by remember { mutableStateOf(false) }
    var isGenerateButtonActive by remember { mutableStateOf(true) }
    var isToggleSaveEnabled by remember { mutableStateOf(true) }

    var captionText by remember { mutableStateOf(captionEntity.caption ?: "") }
    var author by remember { mutableStateOf(captionEntity.author ?: "") }
    var selectedDate by remember { mutableStateOf(captionEntity.date ?: "") }
    var location by remember { mutableStateOf(captionEntity.location ?: "") }
    var device by remember { mutableStateOf(captionEntity.device ?: "") }
    var model by remember { mutableStateOf(captionEntity.model ?: "") }

    var isAuthorChecked by remember { mutableStateOf(false) }
    var isDateChecked by remember { mutableStateOf(false) }
    var isLocationChecked by remember { mutableStateOf(false) }
    var isDeviceChecked by remember { mutableStateOf(false) }
    var isModelChecked by remember { mutableStateOf(false) }

    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    val captionState by viewModel.captionEntityState.collectAsStateWithLifecycle()
    var visibleModal by remember { mutableStateOf<ModalType?>(null) }

    BackHandler {
        onBack()
    }

    LaunchedEffect(Unit) {
        viewModel.resetAllStates()
        if (captionEntity.id > 0) {
            isInitialCaptionSave = true
            toggleSaveActive = true
        } else {
            isInitialCaptionSave = false
            toggleSaveActive = false
        }
        viewModel.getSession(context)
        viewModel.setCaptionEntity(captionEntity)
    }
    LaunchedEffect(
        captionText,
        author,
        selectedDate,
        location,
        device,
        model,
        captionState
    ) {
        captionState?.let { state ->
            toggleSaveActive = !(captionText != state.caption
                    || author != state.author
                    || selectedDate != state.date
                    || location != state.location
                    || device != state.device
                    || model != state.model)
        } ?: false
    }

    when (editCaptionState) {
        is UiState.Loading -> {
            isToggleSaveEnabled = false
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = modifier
                    .fillMaxSize()
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        is UiState.Success -> {
            val editedCaption = (editCaptionState as UiState.Success).data
            LaunchedEffect(editCaptionState) {
                if (editedCaption.status) {
                    captionState?.let {
                        viewModel.setCaptionEntity(
                            it.copy(
                                caption = captionText,
                                author = author,
                                date = selectedDate,
                                location = location,
                                device = device,
                                model = model,
                            )
                        )
                    }
                    isInitialCaptionSave = true
                    toggleSaveActive = true
                } else {
                    visibleModal = ModalType.EDITFAILED
                }
            }
            AnimatedVisibility(
                visible = visibleModal == ModalType.EDITFAILED,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                BasicAlertDialog(
                    onDismissRequest = { visibleModal = null },
                ) {
                    TaskFailedModal(
                        text = editedCaption.message.toString(),
                        onClick = { visibleModal = null },
                    )
                }
            }
            isToggleSaveEnabled = true
        }

        is UiState.Error -> {
            isToggleSaveEnabled = true
        }
    }
    when (savedCaptionState) {
        is UiState.Loading -> {
            isToggleSaveEnabled = false
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = modifier
                    .fillMaxSize()
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        is UiState.Success -> {
            val savedCaption = (savedCaptionState as UiState.Success).data
            LaunchedEffect(savedCaptionState) {
                if (
                    savedCaption.status &&
                    savedCaption.data != null
                ) {
                    viewModel.setCaptionEntity(
                        captionEntity.copy(
                            id = savedCaption.data.id,
                            caption = savedCaption.data.caption,
                            author = savedCaption.data.author,
                            date = savedCaption.data.date,
                            location = savedCaption.data.location,
                            device = savedCaption.data.device,
                            model = savedCaption.data.model,
                        )
                    )
                    isInitialCaptionSave = true
                    toggleSaveActive = true
                } else {
                    visibleModal = ModalType.SAVEFAILED
                }

            }
            AnimatedVisibility(
                visible = visibleModal == ModalType.SAVEFAILED,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                BasicAlertDialog(
                    onDismissRequest = { visibleModal = null },
                ) {
                    TaskFailedModal(
                        text = savedCaption.message.toString(),
                        onClick = { visibleModal = null },
                    )
                }
            }
            isToggleSaveEnabled = true
        }

        is UiState.Error -> {
            isToggleSaveEnabled = true

        }
    }

    when (deleteCaptionState) {
        is UiState.Loading -> {
            isToggleSaveEnabled = false
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = modifier
                    .fillMaxSize()
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        is UiState.Success -> {
            val deletedCaption = (deleteCaptionState as UiState.Success).data
            LaunchedEffect(deleteCaptionState) {
                if (deletedCaption.status) {
                    viewModel.clearCaptionEntity()
                    isInitialCaptionSave = false
                    toggleSaveActive = false
                } else {
                    visibleModal = ModalType.DELETEFAILED
                }
            }
            isToggleSaveEnabled = true

            AnimatedVisibility(
                visible = visibleModal == ModalType.DELETEFAILED,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                BasicAlertDialog(
                    onDismissRequest = { visibleModal = null },
                ) {
                    TaskFailedModal(
                        text = deletedCaption.message.toString(),
                        onClick = { visibleModal = null },
                    )
                }
            }
        }

        is UiState.Error -> {
            isToggleSaveEnabled = true

        }
    }
    when (generatedCaptionState) {
        is UiState.Loading -> {
            isGenerateButtonActive = false
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = modifier
                    .fillMaxSize()
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        is UiState.Success -> {
            val generatedCaption = (generatedCaptionState as UiState.Success).data
            LaunchedEffect(generatedCaptionState) {
                if (generatedCaption.status) {
                    if (generatedCaption.data != null) {
                        captionText = generatedCaption.data.caption
                    }
                } else {
                    visibleModal = ModalType.GENERATEfAILED
                }
            }
            AnimatedVisibility(
                visible = visibleModal == ModalType.GENERATEfAILED,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                BasicAlertDialog(
                    onDismissRequest = { visibleModal = null },
                ) {
                    TaskFailedModal(
                        text = generatedCaption.message.toString(),
                        onClick = { visibleModal = null },
                    )
                }
            }
            isGenerateButtonActive = true
        }

        is UiState.Error -> {
            isGenerateButtonActive = true
        }
    }

    when (sessionState) {
        is UiState.Loading -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .background(Color.Transparent)
                    .fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        }

        is UiState.Success -> {
            val session = (sessionState as UiState.Success)
            Box(
                modifier = modifier
                    .padding(4.dp)
            ) {

                DetailContent(
                    captionEntity = captionEntity,
                    onBack = onBack,
                    author = author,
                    captionText = captionText,
                    onCaptionTextChange = { captionText = it },
                    onAuthorChange = { author = it },
                    selectedDate = selectedDate,
                    onDateChange = { selectedDate = it },
                    location = location,
                    onLocationChange = { location = it },
                    device = device,
                    onDeviceChange = { device = it },
                    model = model,
                    onModelChange = { model = it },
                    isAuthorChecked = isAuthorChecked,
                    onAuthorCheckedChange = { isAuthorChecked = !isAuthorChecked },
                    isDateChecked = isDateChecked,
                    onDateCheckedChange = { isDateChecked = !isDateChecked },
                    isLocationChecked = isLocationChecked,
                    onLocationCheckedChange = { isLocationChecked = !isLocationChecked },
                    isDeviceChecked = isDeviceChecked,
                    onDeviceCheckedChange = { isDeviceChecked = !isDeviceChecked },
                    isModelChecked = isModelChecked,
                    onModelCheckedChange = { isModelChecked = !isModelChecked },
                )

                if (isShareExpanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                isShareExpanded = false
                            }
                    )
                }
                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .offset(y = (-16).dp)
                ) {
                    val (sharelist, notLoginText, toolbar) = createRefs()
                    AnimatedVisibility(
                        visible = isShareExpanded,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.constrainAs(sharelist) {
                            bottom.linkTo(toolbar.top)
                            end.linkTo(toolbar.end)
                            start.linkTo(toolbar.start)
                        }
                    ) {
                        ShareList(
                            onClickShareWhatsApp = {
                                captionState?.image?.let {
                                    shareToSpecificApp(
                                        context = context,
                                        packageName = SocialMediaPackage.WHATSAPP,
                                        text = captionText,
                                        imageUri = it.toUri()
                                    )
                                }
                            },
                            onClickShareInstagram = {
                                captionState?.image?.let {
                                    shareToSpecificApp(
                                        context = context,
                                        packageName = SocialMediaPackage.INSTAGRAM,
                                        text = captionText,
                                        imageUri = it.toUri()
                                    )
                                }
                            },
                            onClickShareFacebook = {
                                captionState?.image?.let {
                                    shareToSpecificApp(
                                        context = context,
                                        packageName = SocialMediaPackage.FACEBOOK,
                                        text = captionText,
                                        imageUri = it.toUri()
                                    )
                                }
                            },
                            onClickShareThreads = {
                                captionState?.image?.let {
                                    shareToSpecificApp(
                                        context = context,
                                        packageName = SocialMediaPackage.THREADS,
                                        text = captionText,
                                        imageUri = it.toUri()
                                    )
                                }

                            },
                            onCLickShareX = {
                                captionState?.image?.let {
                                    shareToSpecificApp(
                                        context = context,
                                        packageName = SocialMediaPackage.X,
                                        text = captionText,
                                        imageUri = it.toUri()
                                    )
                                }

                            },
                            onGeneralShare = {
                                captionState?.image?.let {
                                    shareContent(
                                        context = context,
                                        text = captionText,
                                        imageUri = it.toUri()
                                    )
                                }
                            }
                        )
                    }
                    if (!(sessionState as UiState.Success).data.isLogin) {
                        Text(
                            text = stringResource(R.string.sign_in_to_save),

                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .constrainAs(notLoginText) {
                                    bottom.linkTo(toolbar.top)
                                    end.linkTo(toolbar.end)
                                    start.linkTo(toolbar.start)
                                }
                        )
                    }
                    FloatingToolbar(
                        modifier = Modifier.constrainAs(toolbar) {
                            bottom.linkTo(parent.bottom)
                            end.linkTo(parent.end)
                            start.linkTo(parent.start)
                        },
                        onShareClicked = { isShareExpanded = !isShareExpanded },
                        isGenerateButtonActive = isGenerateButtonActive,
                        onGenerateClicked = {

                            viewModel.generateCaption(
                                author = if (isAuthorChecked) author else "",
                                date = if (isDateChecked) selectedDate else "",
                                location = if (isLocationChecked) location else "",
                                device = if (isDeviceChecked) device else "",
                                model = if (isModelChecked) model else "",
                                image = captionEntity.image.toUri(),
                                context = context
                            )
                        },
                        isLogin = session.data.isLogin,
                        onLoginClicked = {
                            viewModel.login(context)
                        },
                        toogleSaveActive = toggleSaveActive,
                        isToogleSaveEnabled = isToggleSaveEnabled,
                        onSaveClicked = {
                            if (!toggleSaveActive) {
                                //Save
                                if (!isInitialCaptionSave) {
                                    //Save
                                    viewModel.saveCaption(
                                        caption = captionText,
                                        author = author,
                                        date = selectedDate,
                                        location = location,
                                        device = device,
                                        model = model,
                                        image = captionEntity.image,
                                        token = session.data.token,
                                        context = context
                                    )
                                } else {
                                    //Edit
                                    captionState?.let {
                                        viewModel.editCaption(
                                            id = it.id,
                                            caption = captionText,
                                            token = session.data.token,
                                            author = author,
                                            date = selectedDate,
                                            location = location,
                                            device = device,
                                            model = model,
                                            image = captionEntity.image,
                                            context = context
                                        )
                                    }
                                }
                            } else {
                                //Delete
                                captionState?.let {
                                    viewModel.deleteCaption(
                                        id = it.id,
                                        token = session.data.token,
                                        context = context
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }

        is UiState.Error -> {

        }
    }

}

@Composable
fun DetailContent(
    captionEntity: CaptionEntity,
    onBack: () -> Unit,
    captionText: String,
    onCaptionTextChange: (String) -> Unit,
    // Metadata parameters
    author: String,
    onAuthorChange: (String) -> Unit,
    selectedDate: String,
    onDateChange: (String) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
    device: String,
    onDeviceChange: (String) -> Unit,
    model: String,
    onModelChange: (String) -> Unit,
    // Checkbox states
    isAuthorChecked: Boolean,
    onAuthorCheckedChange: (Boolean) -> Unit,
    isDateChecked: Boolean,
    onDateCheckedChange: (Boolean) -> Unit,
    isLocationChecked: Boolean,
    onLocationCheckedChange: (Boolean) -> Unit,
    isDeviceChecked: Boolean,
    onDeviceCheckedChange: (Boolean) -> Unit,
    isModelChecked: Boolean,
    onModelCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isMetadataExpanded by remember { mutableStateOf(false) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isZoomedOut by remember { mutableStateOf(true) }

    var isCaptionLayoutVisible by remember { mutableStateOf(false) }
    LaunchedEffect(captionText) {
        if (captionText.isNotEmpty()) isCaptionLayoutVisible = true
    }

    var showDatePicker by remember { mutableStateOf(false) }
    if (showDatePicker) {
        DateTimePickerModal(
            onDateTimeSelected = { newDateTime ->
                newDateTime?.let {
                    val formatter = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
                    val formattedDate = formatter.format(Date(it))
                    // Update the selected date state
                    onDateChange(formattedDate)
                }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    ConstraintLayout(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        val (header, caption, textHelperZoom, textHelperMeta, metadata, space) = createRefs()

        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(header) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            val (backButton, image) = createRefs()
            IconButton(
                onClick = onBack,
                modifier = Modifier.constrainAs(backButton) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .constrainAs(image) {
                        top.linkTo(backButton.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp,
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            ) {
                AsyncImage(
                    model = captionEntity.image,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clipToBounds()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (scale > 1f) {
                                        scale = 1f
                                        offset = Offset.Zero
                                        isZoomedOut = true
                                    } else {
                                        scale = 2f
                                        isZoomedOut = false
                                    }
                                },
                                onTap = {
                                    isZoomedOut = !isZoomedOut
                                    if (isZoomedOut) {
                                        scale = 1f
                                        offset = Offset.Zero
                                    }
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val newScale = (scale * zoom).coerceIn(1f, 5f)
                                if (newScale > 1f) {
                                    scale = newScale

                                    // Batasi offset agar tidak keluar dari bounds
                                    val maxOffset = (size.width * (scale - 1)) / 2
                                    offset = Offset(
                                        x = (offset.x + pan.x).coerceIn(-maxOffset, maxOffset),
                                        y = (offset.y + pan.y).coerceIn(-maxOffset, maxOffset)
                                    )
                                    isZoomedOut = false
                                }
                            }
                        }
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )

                )
            }

        }
        Text(
            text = stringResource(R.string.pinch_to_zoom_image),
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            modifier = Modifier
                .constrainAs(textHelperZoom) {
                    top.linkTo(header.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }

        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(caption) {
                    top.linkTo(textHelperZoom.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            AnimatedVisibility(
                visible = isCaptionLayoutVisible,
                enter = slideInVertically(
                    initialOffsetY = { -40 },
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                ),
                exit = slideOutVertically(
                    targetOffsetY = { -40 },
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = FastOutLinearInEasing
                    )
                ) + fadeOut(
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = FastOutLinearInEasing
                    )
                ),
            ) {
                Card(
                    shape = CardDefaults.elevatedShape,
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                ) {
                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = captionText,
                            onValueChange = onCaptionTextChange,
                            minLines = 1,
                            maxLines = 5,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                textAlign = TextAlign.Justify,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        IconButton(
                            onClick = { },
                            modifier = Modifier
                                .padding(0.dp)
                                .align(Alignment.CenterVertically)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_copy),
                                contentDescription = stringResource(R.string.copy)
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = stringResource(R.string.select_the_data_to_be_added_to_the_caption),
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            modifier = Modifier
                .constrainAs(textHelperMeta) {
                    top.linkTo(caption.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        ConstraintLayout(
            modifier = modifier
                .fillMaxWidth()
                .padding(4.dp)
                .constrainAs(metadata) {
                    top.linkTo(textHelperMeta.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            val (title, list) = createRefs()
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .constrainAs(title) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            ) {
                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    val (left, right) = createRefs()
                    Text(
                        text = stringResource(R.string.image_metadata),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 20.sp
                        ),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .constrainAs(left) {
                                start.linkTo(parent.start)
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)
                            }
                    )
                    IconButton(
                        onClick = { isMetadataExpanded = !isMetadataExpanded },
                        modifier = Modifier
                            .padding(0.dp)
                            .constrainAs(right) {
                                end.linkTo(parent.end)
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)
                            }
                    ) {
                        Icon(
                            imageVector = if (isMetadataExpanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                            contentDescription = if (isMetadataExpanded) stringResource(R.string.show_less) else stringResource(R.string.show_more)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isMetadataExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .constrainAs(list) {
                        top.linkTo(title.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            ) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    MetadataItem(
                        label = stringResource(R.string.string_author),
                        value = author,
                        onValueChange = onAuthorChange,
                        checked = isAuthorChecked,
                        onCheckedChange = onAuthorCheckedChange
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    MetadataDateItem(
                        label = stringResource(R.string.string_date),
                        value = selectedDate,
                        onDatePickerClick = { showDatePicker = true },
                        checked = isDateChecked,
                        onCheckedChange = onDateCheckedChange
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    MetadataItem(
                        label = stringResource(R.string.location),
                        value = location,
                        onValueChange = onLocationChange,
                        checked = isLocationChecked,
                        onCheckedChange = onLocationCheckedChange
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    MetadataItem(
                        label = stringResource(R.string.device),
                        value = device,
                        onValueChange = onDeviceChange,
                        checked = isDeviceChecked,
                        onCheckedChange = onDeviceCheckedChange
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    MetadataItem(
                        label = stringResource(R.string.model),
                        value = model,
                        onValueChange = onModelChange,
                        checked = isModelChecked,
                        onCheckedChange = onModelCheckedChange
                    )

                }
            }
        }

        Spacer(
            modifier = Modifier
                .height(150.dp)
                .constrainAs(space) {
                    top.linkTo(metadata.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailContentPreview() {
    DescripixTheme {
        DetailContent(
            captionEntity = CaptionEntity(
                image = "android.resource://com.pck.name/drawable/img",
                author = "Jhon Doe",
                date = "2023-08-01",
                location = "Example Location",
                device = "Example Device",
                model = "Example Model",
                caption = LoremIpsum(50).values.joinToString(" "),
                id = 1
            ),
            onBack = {},
            author = "dsda",
            onAuthorChange = {},
            selectedDate = "2023-08-01",
            onDateChange = {},
            location = "",
            onLocationChange = {},
            device = "",
            onDeviceChange = {},
            model = "",
            onModelChange = {},
            isAuthorChecked = false,
            onAuthorCheckedChange = {},
            isDateChecked = false,
            onDateCheckedChange = {},
            isLocationChecked = false,
            onLocationCheckedChange = {},
            isDeviceChecked = false,
            onDeviceCheckedChange = {},
            isModelChecked = false,
            onModelCheckedChange = {},
            captionText = "",
            onCaptionTextChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDetailScreen() {
    DescripixTheme {
        DetailScreen(
            captionEntity = CaptionEntity(
                image = "android.resource://com.pck.name/drawable/img",
                author = "Jhon Doe",
                date = "2023-08-01",
                location = "Example Location",
                device = "Example Device",
                model = "Example Model",
                caption = LoremIpsum(50).values.joinToString(" "),
                id = 1
            ),
            onBack = {}
        )
    }
}