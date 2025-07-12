package com.jovan.descripix.ui.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jovan.descripix.ui.theme.DescripixTheme


@Composable
fun StringTemplate(
    modifier: Modifier = Modifier,
){
    val template = """
    Halo semuanya, nama saya jopannn

    %s

    #kemanusian #Mantappu
""".trimIndent()

    val userCaption = "Ini caption dari input."
    val finalText = String.format(template, userCaption)

    Text(finalText)
}

@Preview(showBackground = true)
@Composable
fun StringTemplatePreview(){
    DescripixTheme {
        StringTemplate()
    }
}