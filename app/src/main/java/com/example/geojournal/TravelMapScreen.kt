package com.example.geojournal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp // PERBAIKAN: Import ini yang tadi kurang
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelMapScreen(
    viewModel: JournalViewModel = viewModel(),
    onBack: () -> Unit
) {
    val journals by viewModel.allJournals.collectAsState()

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            TopAppBar(
                title = { Text("Travel Map", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121212))
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            AndroidView(
                factory = { context ->
                    MapView(context).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(5.0)
                    }
                },
                update = { mapView ->
                    mapView.overlays.clear()

                    val journalsWithLocation = journals.filter { it.latitude != null && it.longitude != null }

                    if (journalsWithLocation.isNotEmpty()) {
                        for (journal in journalsWithLocation) {
                            val marker = Marker(mapView)
                            val point = GeoPoint(journal.latitude!!, journal.longitude!!)
                            marker.position = point
                            marker.title = journal.title
                            marker.snippet = journal.locationName
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                            mapView.overlays.add(marker)
                        }

                        val firstJournal = journalsWithLocation.first()
                        mapView.controller.setCenter(GeoPoint(firstJournal.latitude!!, firstJournal.longitude!!))
                    } else {
                        // Default ke Indonesia
                        mapView.controller.setCenter(GeoPoint(-2.5489, 118.0149))
                    }

                    mapView.invalidate()
                },
                modifier = Modifier.fillMaxSize()
            )

            if (journals.none { it.latitude != null }) {
                Surface(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        "No location data found yet.",
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}