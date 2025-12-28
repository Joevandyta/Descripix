package com.jovan.descripix.ui.screen.detail

import android.util.Log
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.GTranslate
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.SignLanguage
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jovan.descripix.R
import com.jovan.descripix.data.source.local.entity.CaptionEntity
import com.jovan.descripix.ui.common.Language
import com.jovan.descripix.ui.common.ModalType
import com.jovan.descripix.ui.common.SocialMediaPackage
import com.jovan.descripix.ui.common.TestTags
import com.jovan.descripix.ui.common.UiState
import com.jovan.descripix.ui.common.WritingStyle
import com.jovan.descripix.ui.component.AnimatedExpandableTitleCard
import com.jovan.descripix.ui.component.DateTimePickerModal
import com.jovan.descripix.ui.component.FloatingToolbar
import com.jovan.descripix.ui.component.MetadataDateItem
import com.jovan.descripix.ui.component.MetadataItem
import com.jovan.descripix.ui.component.ShareList
import com.jovan.descripix.ui.component.TaskFailedModal
import com.jovan.descripix.ui.theme.DescripixTheme
import com.jovan.descripix.utils.ImageConverter
import com.jovan.descripix.utils.ImageConverter.fileToContentUri
import com.jovan.descripix.utils.reduceFileSize
import com.jovan.descripix.utils.resizeIfTooLarge
import com.jovan.descripix.utils.shareContent
import com.jovan.descripix.utils.shareToSpecificApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    var sharedImage by remember { mutableStateOf(captionEntity.image) }
    var captionText by remember { mutableStateOf(captionEntity.caption ?: "") }
    var author by remember { mutableStateOf(captionEntity.author ?: "") }
    var selectedDate by remember { mutableStateOf(captionEntity.date ?: "") }
    var location by remember { mutableStateOf(captionEntity.location ?: "") }
    var device by remember { mutableStateOf(captionEntity.device ?: "") }
    var model by remember { mutableStateOf(captionEntity.model ?: "") }
    var selectedStyle by remember { mutableStateOf(WritingStyle.CASUAL) }
    var selectedLanguage by remember { mutableStateOf(Language.English.code) }

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

        sharedImage = if (captionEntity.image.startsWith("http")) {
            withContext(Dispatchers.IO) {
                ImageConverter.downloadImageToFile(
                    context,
                    captionEntity.image
                )?.let { file ->
                        fileToContentUri(context, file)
                    }

            }.toString()
        } else {
            captionEntity.image
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
                modifier = Modifier
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
                modifier = Modifier
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
                modifier = Modifier
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
                    var error = generatedCaption.message.toString()
                    if(error.contains("Server Busy")) error =
                        stringResource(R.string.server_sibuk_silakan_coba_lagi)
                    TaskFailedModal(
                        text = error,
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
                modifier = Modifier
                    .background(Color.Transparent)
                    .fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        }

        is UiState.Success -> {
            val session = (sessionState as UiState.Success)
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .testTag(TestTags.DETAILS_SCREEN)
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
                    selectedStyle = selectedStyle,
                    onStyleChange = { selectedStyle = it },
                    selectedLanguage = selectedLanguage,
                    onLanguageChange = {selectedLanguage = it},
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
                                sharedImage.let {
                                    Log.d("TAG", "caption image: $it")
                                    Log.d("TAG", "caption uri: ${it.toUri()}")
                                    shareToSpecificApp(
                                        context = context,
                                        packageName = SocialMediaPackage.WHATSAPP,
                                        text = captionText,
                                        imageUri = it.toUri()

                                    )
                                }
                            },
                            onClickShareInstagram = {
                                sharedImage.let {
                                    shareToSpecificApp(
                                        context = context,
                                        packageName = SocialMediaPackage.INSTAGRAM,
                                        text = captionText,
                                        imageUri = it.toUri()
                                    )
                                }
                            },
                            onClickShareFacebook = {
                                sharedImage.let {
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
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .constrainAs(notLoginText) {
                                    bottom.linkTo(toolbar.top)
                                    end.linkTo(toolbar.end)
                                    start.linkTo(toolbar.start)
                                }
                        )
                    }else {
                        Text(
                            text = if (!isInitialCaptionSave){
                                stringResource(R.string.click_save_to_save_caption)
                            } else{
                                stringResource(R.string.click_saved_to_delete_caption)
                            },

                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 4.dp)
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
                                token = session.data.token,
                                style = selectedStyle,
                                languageCode = selectedLanguage,
                                context = context
                            )
                        },
                        toogleSaveActive = toggleSaveActive,
                        isToogleSaveEnabled = isToggleSaveEnabled,
                        onSaveClicked = {
                            if (!session.data.isLogin) {
                                viewModel.login(context)
                            } else {
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
    selectedStyle: String,
    onStyleChange: (String) -> Unit,
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit,
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
    val clipboardManager = LocalClipboardManager.current
    var isCaptionLayoutVisible by remember { mutableStateOf(false) }
    var isWritingStyleExpanded by remember { mutableStateOf(false) }
    var isLanguageLayoutExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(captionText) {
        if (captionText.isNotEmpty()) {
            isCaptionLayoutVisible = true
        }
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
        val (image, caption, writingStyle, textHelperMeta, metadata, space) = createRefs()

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .constrainAs(image) {
                    top.linkTo(parent.top)
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
                contentDescription = stringResource(R.string.image),
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(caption) {
                    top.linkTo(image.bottom)
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
                Column {
                    Card(
                        shape = CardDefaults.elevatedShape,
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .testTag(TestTags.CAPTION_TEXT_LAYOUT),
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
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(captionText))
                                },
                                modifier = Modifier
                                    .padding(0.dp)
                                    .align(Alignment.CenterVertically)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_copy),
                                    contentDescription = stringResource(R.string.copy),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Text(
                        text = stringResource(R.string.tap_the_caption_text_to_make_changes),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }

            }
        }

        ConstraintLayout(
            modifier = modifier
                .fillMaxWidth()
                .padding(4.dp)
                .constrainAs(writingStyle) {
                    top.linkTo(caption.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            val (title, writingsButtons, languagesButtons) = createRefs()

            ConstraintLayout(
                modifier = modifier
                    .fillMaxWidth()
                    .constrainAs(title) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            ) {
                val (writingTitle, languagesTitle) = createRefs()
                AnimatedExpandableTitleCard(
                    title = stringResource(R.string.result_style),
                    icon = Icons.Default.SignLanguage,
                    expanded = isWritingStyleExpanded,
                    otherExpanded = isLanguageLayoutExpanded,
                    onToggle = {
                        isLanguageLayoutExpanded = false
                        isWritingStyleExpanded = !isWritingStyleExpanded
                    },
                    modifier = Modifier
                        .constrainAs(writingTitle) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            end.linkTo(languagesTitle.start)
                            width = if (!isWritingStyleExpanded && !isLanguageLayoutExpanded) {
                                Dimension.fillToConstraints
                            } else if (isWritingStyleExpanded) {
                                Dimension.fillToConstraints
                            } else Dimension.wrapContent
                        }
                )

                AnimatedExpandableTitleCard(
                    title = stringResource(R.string.language),
                    icon = Icons.Filled.GTranslate,
                    expanded = isLanguageLayoutExpanded,
                    otherExpanded = isWritingStyleExpanded,
                    onToggle = {
                        isWritingStyleExpanded = false
                        isLanguageLayoutExpanded = !isLanguageLayoutExpanded
                    },
                    modifier = Modifier
                        .constrainAs(languagesTitle) {
                            top.linkTo(parent.top)
                            start.linkTo(writingTitle.end)
                            end.linkTo(parent.end)
                            width = if (!isWritingStyleExpanded && !isLanguageLayoutExpanded) {
                                Dimension.fillToConstraints
                            } else if (isLanguageLayoutExpanded) {
                                Dimension.fillToConstraints
                            } else Dimension.wrapContent
                        }
                )
            }


            AnimatedVisibility(
                visible = isWritingStyleExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .constrainAs(writingsButtons) {
                        top.linkTo(title.bottom)
                        start.linkTo(parent.start)
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val styles = listOf(
                        WritingStyle.FORMAL,
                        WritingStyle.CASUAL,
                        WritingStyle.POETICAL
                    )
                    styles.forEach { style ->
                        val isSelected = selectedStyle == style
                        FilterChip(
                            selected = isSelected,
                            onClick = { onStyleChange(style) },
                            label = {
                                Text(
                                    text = style.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                                )
                            },
                            modifier = Modifier
                                .padding(horizontal = 4.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                containerColor = MaterialTheme.colorScheme.surface,
                                selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = if (!isSelected) BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline
                            ) else null

                        )
                    }

                }
            }

            AnimatedVisibility(
                visible = isLanguageLayoutExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .constrainAs(languagesButtons) {
                        top.linkTo(title.bottom)
                        start.linkTo(parent.start)
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    val language = Language.getAllLanguages()

                    language.forEach { language ->
                        val isSelected = selectedLanguage == language.code
                        FilterChip(
                            selected = isSelected,
                            onClick = { onLanguageChange(language.code) },
                            label = {
                                Text(
                                    text = language.name.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                                )
                            },
                            modifier = Modifier
                                .padding(horizontal = 4.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                containerColor = MaterialTheme.colorScheme.surface,
                                selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = if (!isSelected) BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline
                            ) else null

                        )
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
                    top.linkTo(writingStyle.bottom)
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
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
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
                        .padding(vertical = 4.dp)
                        .clickable { isMetadataExpanded = !isMetadataExpanded }
                ) {
                    val (left, right) = createRefs()
                    Text(
                        text = stringResource(R.string.image_metadata),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onPrimary
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
                            contentDescription = if (isMetadataExpanded) stringResource(R.string.show_less) else stringResource(
                                R.string.show_more
                            ),
                            tint = MaterialTheme.colorScheme.onPrimary
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
                Column {
                    Text(
                        text = stringResource(R.string.tap_to_add_or_modify_image_data),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        modifier = modifier
                            .padding(vertical = 4.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.tertiaryContainer)
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
            onCaptionTextChange = {},
            selectedStyle = WritingStyle.CASUAL,
            onStyleChange = {},
            selectedLanguage = Language.English.code,
            onLanguageChange = {}
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