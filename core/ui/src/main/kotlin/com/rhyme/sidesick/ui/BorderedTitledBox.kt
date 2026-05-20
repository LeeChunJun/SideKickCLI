package com.rhyme.sidesick.ui

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.layout.offset
import com.jakewharton.mosaic.layout.padding
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.text.SpanStyle
import com.jakewharton.mosaic.text.buildAnnotatedString
import com.jakewharton.mosaic.text.withStyle
import com.jakewharton.mosaic.ui.Alignment
import com.jakewharton.mosaic.ui.Box
import com.jakewharton.mosaic.ui.BoxScope
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.Text

@Composable
fun BorderedTitledBox(
    modifier: Modifier = Modifier,
    title: String,
    titleColor: Color,
    borderColor: Color,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier.border(color = borderColor).padding(horizontal = 1)) {
        Text(
            buildAnnotatedString {
                append("┐ ")
                withStyle(SpanStyle(titleColor)) {
                    append(title)
                }
                append(" ┌")
            },
            modifier = Modifier.align(Alignment.TopStart).offset(x = -1, y = -1),
            color = borderColor,
        )
        content()
    }
}
