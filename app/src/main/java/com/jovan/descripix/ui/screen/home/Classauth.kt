package com.jovan.descripix.ui.screen.home

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jovan.descripix.data.source.local.entity.CaptionEntity
import com.jovan.descripix.ui.common.UiState

