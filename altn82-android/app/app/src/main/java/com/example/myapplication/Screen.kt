package com.example.myapplication


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.component.SatelliteCard

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier, viewModel: NanoOrbitViewModel = viewModel()
) {
    val filteredSatellites by viewModel.filteredSatellites.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        TextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Rechercher par nom ou type d'orbite") },
            singleLine = true
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            /*
                Q1 : Utilisation de LazyColumn pour les performances
            */
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredSatellites) { satellite ->
                    SatelliteCard(
                        satellite = satellite, onClick = { })
                }

                if (filteredSatellites.isEmpty()) {
                    item {
                        Text("Aucun satellite trouvé.", modifier = Modifier.padding(16.dp))
                    }
                }

            }
        }
    }
}