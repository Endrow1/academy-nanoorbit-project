package com.example.myapplication.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.models.EtatStation
import com.example.myapplication.models.StationSol
import com.example.myapplication.viewmodel.NanoOrbitViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File

@Composable
fun MapScreen(viewModel: NanoOrbitViewModel) {
    val context = LocalContext.current
    val stations by viewModel.stations.collectAsStateWithLifecycle()

    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            currentLocation = getLastKnownLocation(context)

            currentLocation?.let { location ->
                mapViewRef?.controller?.setZoom(6.0)
                mapViewRef?.controller?.setCenter(
                    GeoPoint(location.latitude, location.longitude)
                )

                refreshMapMarkers(
                    context = context,
                    mapView = mapViewRef,
                    stations = stations,
                    currentLocation = currentLocation
                )
            }
        }
    }

    LaunchedEffect(stations, currentLocation) {
        refreshMapMarkers(
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
                    setBuiltInZoomControls(false)

                    isHorizontalMapRepetitionEnabled = false
                    isVerticalMapRepetitionEnabled = false

                    controller.setZoom(2.5)
                    controller.setCenter(GeoPoint(20.0, 0.0))

                    mapViewRef = this

                    refreshMapMarkers(
                        context = ctx,
                        mapView = this,
                        stations = stations,
                        currentLocation = currentLocation
                    )
                }
            },
            update = { mapView ->
                mapViewRef = mapView

                refreshMapMarkers(
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

                            refreshMapMarkers(
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
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Me localiser"
            )
        }
    }
}

private fun refreshMapMarkers(
    context: Context,
    mapView: MapView?,
    stations: List<StationSol>,
    currentLocation: Location?
) {
    if (mapView == null) return

    mapView.overlays.removeAll { it is Marker }

    currentLocation?.let { location ->
        val userMarker = Marker(mapView).apply {
            position = GeoPoint(location.latitude, location.longitude)
            title = "Ma position"
            snippet = "Localisation actuelle"

            icon = createUserIcon(context)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)

            setPanToView(true)
        }

        mapView.overlays.add(userMarker)
    }

    stations.forEach { station ->
        val distanceText = currentLocation?.let { location ->
            val results = FloatArray(1)

            Location.distanceBetween(
                location.latitude,
                location.longitude,
                station.latitude,
                station.longitude,
                results
            )

            val km = results[0] / 1000f
            "Distance : %.1f km".format(km)
        } ?: "Distance : non disponible"

        val stationMarker = Marker(mapView).apply {
            position = GeoPoint(station.latitude, station.longitude)

            title = station.nomStation
            snippet =
                "État : ${station.etatStation}\n" +
                        "Bande : ${station.bandeFrequence}\n" +
                        "Débit max : ${station.debitMax ?: "N/A"} Mbps\n" +
                        distanceText

            icon = createStationPointIcon(
                context = context,
                etat = station.etatStation
            )

            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            setPanToView(true)
        }

        mapView.overlays.add(stationMarker)
    }

    mapView.invalidate()
}

private fun createStationPointIcon(
    context: Context,
    etat: EtatStation
): Drawable {
    val size = 54
    val radius = size / 2f
    val strokeWidth = 6f

    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = getStationFillColor(etat)
        style = Paint.Style.FILL
    }

    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = getStationStrokeColor(etat)
        style = Paint.Style.STROKE
        this.strokeWidth = strokeWidth
    }

    canvas.drawCircle(radius, radius, radius - strokeWidth, fillPaint)
    canvas.drawCircle(radius, radius, radius - strokeWidth, strokePaint)

    return BitmapDrawable(context.resources, bitmap)
}

private fun createUserIcon(context: Context): Drawable {
    val size = 64
    val center = size / 2f

    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(33, 150, 243)
        style = Paint.Style.FILL
    }

    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    val userPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }

    canvas.drawCircle(center, center, center - 4f, backgroundPaint)
    canvas.drawCircle(center, center, center - 4f, borderPaint)

    canvas.drawCircle(center, 24f, 8f, userPaint)
    canvas.drawCircle(center, 44f, 15f, userPaint)

    return BitmapDrawable(context.resources, bitmap)
}

private fun getStationFillColor(etat: EtatStation): Int {
    return when (etat) {
        EtatStation.OPERATIONNELLE ->
            android.graphics.Color.rgb(76, 175, 80)

        EtatStation.EN_MAINTENANCE ->
            android.graphics.Color.rgb(255, 165, 0)

        EtatStation.HORS_SERVICE ->
            android.graphics.Color.rgb(158, 158, 158)
    }
}

private fun getStationStrokeColor(etat: EtatStation): Int {
    return when (etat) {
        EtatStation.OPERATIONNELLE ->
            android.graphics.Color.rgb(46, 125, 50)

        EtatStation.EN_MAINTENANCE ->
            android.graphics.Color.rgb(230, 126, 0)

        EtatStation.HORS_SERVICE ->
            android.graphics.Color.DKGRAY
    }
}

private fun hasLocationPermission(context: Context): Boolean {
    val fine =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    val coarse =
        ContextCompat.checkSelfPermission(
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

    return locations.minByOrNull { it.accuracy }
}

@SuppressLint("MissingPermission")
private fun centerOnCurrentLocation(
    context: Context,
    onLocationFound: (Location) -> Unit
) {
    val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

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