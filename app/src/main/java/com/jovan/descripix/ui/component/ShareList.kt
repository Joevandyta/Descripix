package com.jovan.descripix.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jovan.descripix.R
import com.jovan.descripix.ui.theme.DescripixTheme

@Composable
fun ShareList(
    modifier: Modifier = Modifier,
    onClickShareWhatsApp: () -> Unit,
    onClickShareInstagram: () -> Unit,
    onClickShareFacebook: () -> Unit,
    onClickShareThreads: () -> Unit,
    onCLickShareX: () -> Unit,
) {

    Card(
        modifier = modifier
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(contentColor = MaterialTheme.colorScheme.surface.copy(
            alpha = 0.7f
        ))
    ) {
        Row(
            modifier = Modifier
                .padding(0.dp)
                .wrapContentWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            //share WA
            IconButton(
                onClick = onClickShareWhatsApp,
                enabled = true,
                modifier = Modifier
                    .wrapContentWidth(),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_wa),
                    contentDescription = "share to whatsapp",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .fillMaxSize(0.75f)
                )
            }

            //share IG
            IconButton(
                onClick = onClickShareInstagram,
                enabled = true,
                modifier = Modifier
                    .wrapContentWidth()

            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_instagram),
                    contentDescription = "share to Instagram",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .fillMaxSize(0.75f)
                )
            }
            //share to Thread
            IconButton(
                onClick = onClickShareThreads,
                enabled = true,
                modifier = Modifier
                    .wrapContentWidth()

            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_thread),
                    contentDescription = "share to Threads",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .fillMaxSize(0.75f)
                )
            }
            //share Facebook
            IconButton(
                onClick = onCLickShareX,
                enabled = true,
                modifier = Modifier
                    .wrapContentWidth()
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_x),
                    contentDescription = "share to X",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .fillMaxSize(0.75f)
                )
            }
            //Share To X
            IconButton(
                onClick = onClickShareFacebook,
                enabled = true,
                modifier = Modifier
                    .wrapContentWidth()

            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_facebook),
                    contentDescription = "share to Facebook",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .fillMaxSize(0.75f)
                )
            }
            //Share General
            IconButton(
                onClick = {},
                enabled = true,
                modifier = Modifier
                    .wrapContentWidth()

            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_share),
                    contentDescription = "share button",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .fillMaxSize(0.75f)
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ShareListPreview() {
    DescripixTheme {
        ShareList(
            onClickShareWhatsApp = {},
            onClickShareInstagram = {},
            onClickShareFacebook = {},
            onClickShareThreads = {},
            onCLickShareX = {}
        )
    }
}