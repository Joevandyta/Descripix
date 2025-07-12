package com.jovan.descripix.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jovan.descripix.R
import com.jovan.descripix.ui.theme.DescripixTheme
import java.io.File

@Composable
fun CaptionItem(
    imageUrl: String,
    caption: String,
    modifier: Modifier = Modifier,
    navigateToDetail: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "ArrowRotation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(8.dp),
        onClick = navigateToDetail
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
        ) {
            AsyncImage(
                model = File(imageUrl),
                contentDescription = "Item Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
            Row (
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = caption,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding( start = 8.dp, end = 0.dp, top = 8.dp, bottom = 8.dp)
                        .weight(1f)
                        .animateContentSize(
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = FastOutSlowInEasing
                            )
                        )
                )

                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Show less" else "Show more",
                        modifier = Modifier.rotate(rotationAngle)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CaptionItemPreview() {
    DescripixTheme {
        CaptionItem(
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e1/Pemandangan_Gunung_Bromo.jpg/2560px-Pemandangan_Gunung_Bromo.jpg",
            caption = "Lorem Ipsum is simply dummy text of the printingsddaadacghrgwfsdfadadachrwasfdsfvsADFvsvfasgfsfdfasdfafasfgasdgfgfsfsfsffsfsafgg",
            navigateToDetail = {}
        )
    }
}