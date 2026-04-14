package com.example.myapplication.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.models.EtatStation
import com.example.myapplication.models.StationSol
import com.example.myapplication.viewmodel.NanoOrbitViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.compose.material3.MaterialTheme
import org.osmdroid.config.Configuration
import java.io.File
import android.annotation.SuppressLint
import android.util.Log
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

@Composable
fun MapScreen(viewModel: NanoOrbitViewModel) {
    val context = LocalContext.current
    val stations by viewModel.stations.collectAsStateWithLifecycle()

    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            currentLocation = getLastKnownLocation(context)
            currentLocation?.let { location ->
                mapViewRef?.controller?.setZoom(6.0)
                mapViewRef?.controller?.setCenter(
                    GeoPoint(location.latitude, location.longitude)
                )
                refreshMarkers(
                    context = context,
                    mapView = mapViewRef,
                    stations = stations,
                    currentLocation = currentLocation
                )
            }
        }
    }

    LaunchedEffect(stations, currentLocation) {
        refreshMarkers(
            context = context,
            mapView = mapViewRef,
            stations = stations,
            currentLocation = currentLocation
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val osmConfig = Configuration.getInstance()
                osmConfig.userAgentValue = ctx.packageName

                val basePath = File(ctx.cacheDir, "osmdroid")
                val tileCache = File(basePath, "tiles")
                osmConfig.osmdroidBasePath = basePath
                osmConfig.osmdroidTileCache = tileCache

                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(2.5)
                    controller.setCenter(GeoPoint(20.0, 0.0))
                    mapViewRef = this

                    refreshMarkers(
                        context = context,
                        mapView = this,
                        stations = stations,
                        currentLocation = currentLocation
                    )
                }
            },
            update = { mapView ->
                mapViewRef = mapView
                refreshMarkers(
                    context = context,
                    mapView = mapView,
                    stations = stations,
                    currentLocation = currentLocation
                )
            }
        )

        FloatingActionButton(
            onClick = {
                if (hasLocationPermission(context)) {
                    centerOnCurrentLocation(
                        context = context,
                        onLocationFound = { location ->
                            currentLocation = location
                            mapViewRef?.controller?.setZoom(6.0)
                            mapViewRef?.controller?.setCenter(
                                GeoPoint(location.latitude, location.longitude)
                            )
                            refreshMarkers(
                                context = context,
                                mapView = mapViewRef,
                                stations = stations,
                                currentLocation = currentLocation
                            )
                        }
                    )
                } else {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Me localiser")
        }
    }
}

private fun refreshMarkers(
    context: Context,
    mapView: MapView?,
    stations: List<StationSol>,
    currentLocation: Location?
) {
    if (mapView == null) return

    mapView.overlays.removeAll { it is Marker }

    stations.forEach { station ->
        val marker = Marker(mapView)
        marker.position = GeoPoint(station.latitude, station.longitude)
        marker.title = station.nomStation
        marker.icon = getMarkerIcon(context, station.etatStation)

        val distanceText = currentLocation?.let {
            val results = FloatArray(1)
            Location.distanceBetween(
                it.latitude,
                it.longitude,
                station.latitude,
                station.longitude,
                results
            )
            val km = results[0] / 1000f
            "Distance : %.1f km".format(km)
        } ?: "Distance : non disponible"

        marker.snippet =
            "Bande : ${station.bandeFrequence}\n" +
                    "Débit max : ${station.debitMax ?: "N/A"} Mbps\n" +
                    distanceText

        marker.setPanToView(true)
        mapView.overlays.add(marker)
    }

    mapView.invalidate()
}

private fun getMarkerIcon(context: Context, etat: EtatStation): Drawable? {
    val base = ContextCompat.getDrawable(
        context,
        org.osmdroid.library.R.drawable.marker_default
    )?.mutate()

    val color = when (etat) {
        EtatStation.OPERATIONNELLE -> android.graphics.Color.GREEN
        EtatStation.EN_MAINTENANCE -> android.graphics.Color.rgb(255, 165, 0)
        EtatStation.HORS_SERVICE -> android.graphics.Color.GRAY
    }

    base?.setTint(color)
    return base
}

private fun hasLocationPermission(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val coarse = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    return fine || coarse
}

private fun getLastKnownLocation(context: Context): Location? {
    val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    val providers = locationManager.getProviders(true)
    val locations = providers.mapNotNull { provider ->
        try {
            locationManager.getLastKnownLocation(provider)
        } catch (_: SecurityException) {
            null
        }
    }

    return locations.maxByOrNull { it.accuracy }
}

@SuppressLint("MissingPermission")
private fun centerOnCurrentLocation(
    context: Context,
    onLocationFound: (Location) -> Unit
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    fusedLocationClient
        .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        .addOnSuccessListener { location ->
            Log.d("MAP", "getCurrentLocation = $location")
            if (location != null) {
                onLocationFound(location)
            }
        }
        .addOnFailureListener { e ->
            Log.e("MAP", "Erreur localisation", e)
        }
}