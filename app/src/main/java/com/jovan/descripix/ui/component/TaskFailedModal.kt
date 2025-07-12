package com.jovan.descripix.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.jovan.descripix.R
import com.jovan.descripix.ui.theme.DescripixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFailedModal(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.task_failed),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 24.sp,
                    fontFamily = FontFamily(Font(R.font.righteous))
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 20.sp,
                    fontFamily = FontFamily(Font(R.font.righteous))
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                LottieAnimationPreload(
                    animationResId = R.raw.task_failed,
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }

            Button(
                onClick = onClick,
                modifier = Modifier
                    .width(128.dp)
                    .wrapContentHeight()
                    .padding(8.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.elevatedButtonElevation()
            ) {
                Text(
                    text = stringResource(R.string.oke)
                )
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun TaskFailedModalPreview() {
    DescripixTheme {
        TaskFailedModal(
            text = "Task Failed",
            onClick = {}
        )
    }
}
