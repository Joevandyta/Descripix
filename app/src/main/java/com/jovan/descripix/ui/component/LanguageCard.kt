package com.jovan.descripix.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LanguageCard(
    modifier: Modifier = Modifier,
    onEnglishClicked : () -> Unit,
    onIndonesiaClicked : () -> Unit
){
    Card(
        elevation = CardDefaults.elevatedCardElevation(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier
            .padding(2.dp)
            .fillMaxWidth(0.3f)
    ) {
        Column(
            modifier = Modifier
                .wrapContentWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Box(
                modifier
                    .fillMaxWidth()
                    .clickable {
                        onEnglishClicked()
                    }
            ) {
                Text(
                    text = "English",
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onIndonesiaClicked()
                    }
            ) {
                Text(
                    text = "Indonesia",
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}