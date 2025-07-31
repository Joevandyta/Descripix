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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jovan.descripix.R
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
                                contentDescription = "Save Caption",

                                )
                            Text(
                                text = if (toogleSaveActive) "Saved" else "Save",
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
                                contentDescription = "Login Button",

                                )
                            Text(
                                text = "Login",
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
                            contentDescription = "Save Caption"
                        )
                        Text(
                            text = "Generate",
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
                    contentDescription = "Save Caption",
                )
                Text(
                    text = "Share",
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
