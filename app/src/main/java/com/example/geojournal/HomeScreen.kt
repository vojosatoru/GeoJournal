package com.example.geojournal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: JournalViewModel = viewModel(),
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    onNavigateToMap: () -> Unit
) {
    val journals by viewModel.allJournals.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val totalWords by viewModel.totalWords.collectAsState()
    val totalDays by viewModel.totalDays.collectAsState()

    var isSearchActive by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFF121212),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = Color(0xFF6C5DD3),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // --- HEADER & SEARCH BAR ---
            if (isSearchActive) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChanged(it) },
                    onSearch = { isSearchActive = false },
                    active = false,
                    onActiveChange = { isSearchActive = it },
                    placeholder = { Text("Search journals...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = {
                            isSearchActive = false
                            viewModel.onSearchQueryChanged("")
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {}
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("GeoJournal", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Row {
                        IconButton(onClick = onNavigateToMap) {
                            Icon(Icons.Default.Place, "Map View", tint = Color(0xFF6C5DD3))
                        }
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, "Search", tint = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- STATISTIK REAL ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem(journals.size.toString(), "Entries")
                StatItem(totalWords.toString(), "Words")
                StatItem(totalDays.toString(), "Days")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Your Entries", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                if (searchQuery.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("(Filtered)", color = Color.Gray, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // List Journal
            if (journals.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No journals found." else "Start your journey by adding a new entry!",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(journals) { journal ->
                        JournalCard(
                            journal = journal,
                            onDelete = { viewModel.deleteJournal(journal) },
                            onEdit = { onNavigateToEdit(journal.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count, color = Color(0xFF6C5DD3), fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(label, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun JournalCard(
    journal: JournalEntity,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Row Gambar & Peta
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                // Foto (Kiri)
                if (journal.photoUri.isNotEmpty()) {
                    AsyncImage(
                        model = journal.photoUri,
                        contentDescription = "Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color.DarkGray)
                    )
                } else {
                    // PERBAIKAN: Menggunakan Text sebagai fallback, bukan Icon Image yang error
                    Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFF2C2C2C)), contentAlignment = Alignment.Center) {
                        Text("No Photo", color = Color.Gray, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Peta OSM (Kanan)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color(0xFF2C2C2C))
                ) {
                    if (journal.latitude != null && journal.longitude != null) {
                        AndroidView(
                            factory = { context ->
                                MapView(context).apply {
                                    setTileSource(TileSourceFactory.MAPNIK)
                                    setMultiTouchControls(true)
                                    controller.setZoom(15.0)
                                    setOnTouchListener { _, _ -> true } // Disable interaksi di list
                                }
                            },
                            update = { mapView ->
                                val geoPoint = GeoPoint(journal.latitude, journal.longitude)
                                mapView.controller.setCenter(geoPoint)
                                mapView.overlays.clear()
                                val marker = Marker(mapView)
                                marker.position = geoPoint
                                marker.title = journal.locationName
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                mapView.overlays.add(marker)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No Location", color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Judul
            Text(journal.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)

            // Lokasi
            if (journal.locationName != null && journal.locationName != "Unknown Location" && journal.locationName != "No Location") {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF6C5DD3), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(journal.locationName, color = Color(0xFF6C5DD3), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            // Deskripsi
            Text(
                journal.description,
                color = Color.LightGray,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Footer
            val dateFormat = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(dateFormat.format(journal.date), color = Color.Gray, fontSize = 12.sp)

                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        containerColor = Color(0xFF2C2C2C)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit", color = Color.White) },
                            onClick = { expanded = false; onEdit() },
                            leadingIcon = { Icon(Icons.Default.Edit, null, tint = Color.White) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color(0xFFFF5555)) },
                            onClick = { expanded = false; onDelete() },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color(0xFFFF5555)) }
                        )
                        DropdownMenuItem(
                            text = { Text("Export PDF", color = Color.Cyan) },
                            onClick = { expanded = false },
                            leadingIcon = { Icon(Icons.Default.Share, null, tint = Color.Cyan) }
                        )
                    }
                }
            }
        }
    }
}