package com.jovan.descripix.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jovan.descripix.ui.theme.DescripixTheme
import com.jovan.descripix.utils.simpleToast

@Composable
fun MetadataItem(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            Modifier
                .padding(start = 8.dp)
                .width(100.dp),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = ":",
            color = MaterialTheme.colorScheme.onSecondaryContainer

        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            maxLines = 3,
            modifier = Modifier
                .padding(start = 8.dp, end = 0.dp, top = 16.dp, bottom = 16.dp)
                .weight(1f),
        )
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun MetadataDateItem(
    label: String,
    value: String,
    onDatePickerClick: () -> Unit,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .clickable { onDatePickerClick() }
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            Modifier
                .padding(start = 8.dp)
                .width(100.dp),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = ":",
            color = MaterialTheme.colorScheme.onSecondaryContainer

        )
        Box(
            modifier = Modifier
                .padding(start = 8.dp, end = 0.dp, top = 16.dp, bottom = 16.dp)
                .weight(1f)
        ) {
            Text(
                text = value,
                modifier = Modifier.fillMaxWidth(), // fill area dalam box
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }

        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MetadataItemPreview() {
    var author by remember { mutableStateOf("John Doe") }
    var isChecked by remember { mutableStateOf(false) }
    DescripixTheme {
        MetadataDateItem(
            label = "Author",
            value = author,
            checked = isChecked,
            onCheckedChange = { isChecked = it },
            onDatePickerClick = {  }

        )
    }
}