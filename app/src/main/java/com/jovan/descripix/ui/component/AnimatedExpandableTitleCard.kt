package com.jovan.descripix.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.airbnb.lottie.model.content.CircleShape

@Composable
fun AnimatedExpandableTitleCard(
    title: String,
    icon: ImageVector,
    expanded: Boolean,
    otherExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card (
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            if (!expanded && !otherExpanded) {
                MaterialTheme.colorScheme.surface
            }else if (expanded){
                    MaterialTheme.colorScheme.primary
            }else {
                MaterialTheme.colorScheme.tertiaryContainer
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (expanded) 6.dp else 2.dp
        ),
        modifier = modifier
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                )
            )
            .padding(4.dp)

    ) {
        ConstraintLayout(
            modifier = Modifier
                .then(
                    if (!expanded && !otherExpanded) {
                        Modifier.fillMaxWidth()
                    } else if (expanded) {
                        Modifier.fillMaxWidth()
                    } else {
                        Modifier.wrapContentWidth()
                    }
                )
                .wrapContentHeight()
                .clickable { onToggle() }
        ) {
            val (left, mid, right) = createRefs()

            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (!expanded && !otherExpanded) {
                    MaterialTheme.colorScheme.onSurface
                }else if (expanded){
                    MaterialTheme.colorScheme.onPrimary
                }else {
                    MaterialTheme.colorScheme.onTertiaryContainer
                },
                modifier = Modifier
                    .then(
                        if (!expanded && !otherExpanded) {
                            Modifier.padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 16.dp)
                        } else if (expanded) {
                            Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
                        } else {
                            Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)

                        }
                    )
                    .constrainAs(left) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
            )

            if (!otherExpanded) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp,
                        color = if (!expanded) {
                            MaterialTheme.colorScheme.onSurface
                        }else {
                            MaterialTheme.colorScheme.onPrimary
                        }
                    ),
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .constrainAs(mid) {
                            start.linkTo(left.end)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        }
                )

                Icon(
                    imageVector = if (expanded)
                        Icons.Filled.KeyboardArrowDown
                    else
                        Icons.Filled.KeyboardArrowUp,
                    contentDescription = null,
                    tint = if (!expanded) {
                        MaterialTheme.colorScheme.onSurface
                    }else {
                        MaterialTheme.colorScheme.onPrimary
                    },
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .constrainAs(right) {
                            end.linkTo(parent.end)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        }
                )

            }
        }
    }
}