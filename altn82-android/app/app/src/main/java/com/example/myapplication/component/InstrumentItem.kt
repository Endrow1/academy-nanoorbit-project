package com.example.myapplication.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.myapplication.model.Instrument

@Composable
fun InstrumentItem(instrument: Instrument, etatFonctionnement: String) {
    ListItem(
        headlineContent = { Text(instrument.modele) },
        supportingContent = { Text("${instrument.typeInstrument} • $etatFonctionnement") },
        trailingContent = {
            instrument.resolution?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
        },
        leadingContent = {
            Icon(Icons.Default.Settings, contentDescription = null)
        }
    )
}