package com.jovan.descripix.ui.component

import androidx.annotation.RawRes
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.test.espresso.IdlingResource
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.jovan.descripix.R

@Composable
fun LottieAnimationPreload(
    @RawRes animationResId: Int,
    modifier: Modifier = Modifier,
) {
    val isInTest = System.getProperty("IS_TEST") == "true"

    if (isInTest) {
        // Do not render Lottie during test
        Box(modifier = modifier) {
            // Maybe show a static image or leave blank
        }
    } else {
        val preloaderLottieComposition by rememberLottieComposition(
            LottieCompositionSpec.RawRes(animationResId)
        )

        val preloaderProgress by animateLottieCompositionAsState(
            preloaderLottieComposition,
            iterations = LottieConstants.IterateForever,
            isPlaying = true
        )

        LottieAnimation(
            composition = preloaderLottieComposition,
            progress = preloaderProgress,
            modifier = modifier
        )
    }
}