package com.jovan.descripix.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jovan.descripix.R
import com.jovan.descripix.ui.common.TestTags
import com.jovan.descripix.ui.theme.DescripixTheme

@Composable
fun FloatingToolbar(
    modifier: Modifier = Modifier,
    onShareClicked: () -> Unit,
    isGenerateButtonActive: Boolean,
    onGenerateClicked: () -> Unit,
    toogleSaveActive: Boolean,
    isToogleSaveEnabled: Boolean,
    onSaveClicked: () -> Unit = {},
    isLogin: Boolean,
    onLoginClicked: () -> Unit = {}
) {


    Row(
        modifier = modifier
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            shape = RoundedCornerShape(48.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp,
            )
        ) {
            Row(
                Modifier.padding(8.dp)
            ) {
                if (isLogin) {

                    TextButton(
                        onClick = {
                            onSaveClicked()
                        },
                        enabled = isToogleSaveEnabled,
                        modifier = Modifier.testTag(TestTags.FLOATING_TOOLBAR_SAVE)
                        ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                tint = if(isToogleSaveEnabled) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.4f),
                                painter = painterResource(
                                    if (toogleSaveActive) {
                                        R.drawable.ic_bookmark_added
                                    } else {
                                        R.drawable.ic_bookmark
                                    }
                                ),
                                contentDescription = stringResource(R.string.save_caption),
                                )
                            Text(
                                text = if (toogleSaveActive) stringResource(R.string.saved) else stringResource(
                                    R.string.save
                                ),
                                color = if(isToogleSaveEnabled) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.4f)
                            )
                        }
                    }
                } else {
                    TextButton(
                        onClick = onLoginClicked,
                        enabled = true,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                painter = painterResource(R.drawable.ic_login),
                                contentDescription = stringResource(R.string.sign_in_button),
                                )
                            Text(
                                text = stringResource(R.string.sign_in),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
                TextButton(
                    onClick = onGenerateClicked,
                    enabled = isGenerateButtonActive,
                    ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            tint = if(isGenerateButtonActive) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.4f),
                            painter = painterResource(R.drawable.ic_rocket),
                            contentDescription = stringResource(R.string.generate_caption)
                        )
                        Text(
                            text = stringResource(R.string.generate),
                            color = if(isGenerateButtonActive) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.4f)
                        )
                    }

                }
            }
        }
        Spacer(Modifier.size(16.dp))
        Button(
            onClick = onShareClicked,
            modifier = Modifier
                .size(62.dp),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(25),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
            ),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    painter = painterResource(R.drawable.ic_share),
                    contentDescription = stringResource(R.string.share_button),
                )
                Text(
                    text = stringResource(R.string.share),
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
        }

    }
}

@Composable
@Preview(showBackground = true)
fun PreviewHorizontal() {
    DescripixTheme {
        FloatingToolbar(
        onShareClicked = {},
        onGenerateClicked ={},
        toogleSaveActive = false,
        onSaveClicked = {},
        isLogin = false,
        onLoginClicked = {},
            isToogleSaveEnabled = true,
            isGenerateButtonActive = true
        )
    }
}
