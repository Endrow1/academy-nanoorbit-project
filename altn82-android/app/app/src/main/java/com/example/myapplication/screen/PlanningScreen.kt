package com.example.myapplication.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.component.FenetreCard
import com.example.myapplication.models.StationSol
import com.example.myapplication.models.StatutFenetre
import com.example.myapplication.viewmodel.NanoOrbitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningScreen(viewModel: NanoOrbitViewModel) {
    val stations by viewModel.stations.collectAsStateWithLifecycle()
    val fenetresFromCache by viewModel.fenetres.collectAsStateWithLifecycle()

    var selectedStation by remember { mutableStateOf<StationSol?>(null) }
    var expanded by remember { mutableStateOf(false) }

    val fenetres = fenetresFromCache
        .filter { selectedStation == null || it.codeStation == selectedStation?.codeStation }
        .sortedBy { it.datetimeDebut }

    val totalDuree = fenetres.sumOf { it.duree }
    val totalVolume = fenetres.sumOf { it.volumeDonnees ?: 0.0 }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Planning des communications") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = selectedStation?.nomStation ?: "Choisir une station",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Station au sol") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    stations.forEach { station ->
                        DropdownMenuItem(
                            text = { Text(station.nomStation) },
                            onClick = {
                                selectedStation = station
                                expanded = false
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Temps de contact", style = MaterialTheme.typography.labelMedium)
                    Text(
                        "${totalDuree}s",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Volume total", style = MaterialTheme.typography.labelMedium)
                    Text(
                        "${String.format("%.2f", totalVolume)} Go",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        if (fenetres.isEmpty()) {
                            Text(
                                "Aucune fenêtre pour cette station",
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            fenetres.forEachIndexed { index, fenetre ->
                                FenetreCard(fenetre, selectedStation?.nomStation ?: "Inconnu")
                                if (index < fenetres.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        thickness = 0.5.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(statut: StatutFenetre) {
    val color = Color(statut.color.toLong(16))

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Text(
            text = statut.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}