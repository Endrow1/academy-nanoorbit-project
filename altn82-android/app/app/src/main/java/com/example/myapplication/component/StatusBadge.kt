package com.example.myapplication.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.models.StatutSatellite

@Composable
fun StatusBadge(statut: StatutSatellite, modifier: Modifier = Modifier) {

    val colorInt = statut.color.toLong(16)
    val composeColor = Color(colorInt)

    Surface(
        modifier = modifier,
        color = composeColor.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, composeColor)
    ) {
        Text(
            text = statut.getLabel(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = composeColor
        )
    }
}