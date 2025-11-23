package com.example.geojournal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerScreen(
    onLocationSelected: (Double, Double) -> Unit,
    onBack: () -> Unit
) {
    // Default start point (Indonesia Center -ish) atau bisa default ke 0,0
    val startPoint = GeoPoint(-2.5489, 118.0149)

    // Kita butuh referensi ke MapView untuk mengambil koordinat tengah saat tombol ditekan
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pick Location", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121212))
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val center = mapViewRef?.mapCenter
                    if (center != null) {
                        // Kirim koordinat Latitude & Longitude kembali
                        onLocationSelected(center.latitude, center.longitude)
                    }
                },
                containerColor = Color(0xFF6C5DD3),
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Check, null) },
                text = { Text("Select This Location") }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            // 1. Peta
            AndroidView(
                factory = { context ->
                    MapView(context).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(5.0)
                        controller.setCenter(startPoint)
                        mapViewRef = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // 2. Pin di Tengah Layar (Overlay)
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Center Pin",
                tint = Color.Red,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center)
                    .offset(y = (-24).dp) // Geser ke atas sedikit agar ujung pin pas di tengah
            )

            // Bayangan kecil di bawah pin (Opsional, untuk estetika)
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .align(Alignment.Center)
                    .offset(y = 0.dp)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
            )
        }
    }
}