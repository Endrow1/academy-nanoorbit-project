package com.example.myapplication.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.component.StatusBadge
import com.example.myapplication.mock.MockData
import com.example.myapplication.viewmodel.NanoOrbitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    satelliteId: Int, viewModel: NanoOrbitViewModel, onBack: () -> Unit
) {
    val satellite = MockData.satellites.find { it.idSatellite == satelliteId }
    val orbite = MockData.orbites.find { it.idOrbite == satellite?.idOrbite }
    val instruments = MockData.instruments.filter { it.idSatellite == satelliteId }
    val missionIds = satellite?.missions?.map { it.missionId } ?: emptyList()
    val missions = MockData.missions.filter { it.idMission in missionIds }

    var showAnomalieDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(satellite?.nomSatellite ?: "Détail") }, navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            })
        }) { padding ->
        if (satellite == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Statut: ", style = MaterialTheme.typography.bodyLarge)
                                StatusBadge(satellite.statut)
                            }
                            DetailRow("Format", satellite.formatCubesat)
                            DetailRow("Lancement", satellite.dateLancement?.toString() ?: "N/A")
                            DetailRow("Masse", "${satellite.masse} kg")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            DetailRow("Type d'orbite", orbite?.typeOrbite ?: "N/A")
                            DetailRow("Altitude", "${orbite?.altitude} km")
                            DetailRow("Inclinaison", "${orbite?.inclinaison}°")
                            DetailRow("Couverture", orbite?.zoneCouverture ?: "N/A")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            Column {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Batterie", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "${satellite.battery}%",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { satellite.battery / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp),
                                    color = if (satellite.battery < 15) Color.Red else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                }
                item { DetailSectionTitle("Instruments embarqués") }
                if (instruments.isEmpty()) {
                    item {
                        Text(
                            "Pas d'instruments", modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                } else {
                    items(instruments) { instrument ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            ListItem(
                                overlineContent = { Text("${instrument.typeInstrument} - ${instrument.modele}") },
                                headlineContent = { Text(instrument.refInstrument) },
                                trailingContent = {
                                    Text(
                                        text = instrument.statutInstrument.name,
                                        color = Color(instrument.statutInstrument.color.toLong(16)),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                        }
                    }
                }
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                }
                item { DetailSectionTitle("Missions associées") }
                if (satellite.missions.isEmpty()) {
                    item {
                        Text(
                            "Aucune mission enregistrée",
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                } else {
                    items(missions) { mission ->
                        val role =
                            satellite.missions.find { it.missionId == mission.idMission }?.role
                                ?: "Inconnu"
                        Card(modifier = Modifier.fillMaxWidth()) {
                            ListItem(
                                headlineContent = { Text(mission.nomMission) },
                                supportingContent = { Text("Rôle : $role") },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = { showAnomalieDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Signaler une anomalie")
                    }
                }
            }
        }
    }

    if (showAnomalieDialog) {
        AnomalieDialog(onDismiss = { showAnomalieDialog = false })
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DetailSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
    )
}

@Composable
fun AnomalieDialog(onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Signaler une anomalie") }, text = {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )
    }, confirmButton = {
        Button(onClick = onDismiss, enabled = text.isNotBlank()) { Text("Envoyer") }
    }, dismissButton = {
        TextButton(onClick = onDismiss) { Text("Annuler") }
    })
}