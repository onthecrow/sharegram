package com.onthecrow.sharegram.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onthecrow.sharegram.R

@Composable
fun ShareButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            var textHeight by remember { mutableStateOf(0.dp) }
            val density = LocalDensity.current
            AnimatedVisibility(isLoading) {
                CircularProgressIndicator(
                    Modifier
                        .padding(end = 8.dp)
                        .size(textHeight)
                )
            }
            Text(
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    textHeight = with(density) { coordinates.size.height.toDp() }
                },
                text = stringResource(R.string.share)
            )
        }
    }
}

@Preview
@Composable
private fun ShareButtonPreview() {
    ShareButton(isLoading = false, {})
}

@Preview
@Composable
private fun ShareButtonPreview2() {
    ShareButton(isLoading = true, {})
}