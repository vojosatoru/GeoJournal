package com.example.geojournal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: JournalViewModel = viewModel(),
    onNavigateToAdd: () -> Unit
) {
    val journals by viewModel.allJournals.collectAsState()

    Scaffold(
        containerColor = Color(0xFF121212), // Background Gelap
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = Color(0xFF6C5DD3), // Warna Ungu aksen
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
            // Header: Title & Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Journal",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Row {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Stats Row (Dummy Data sesuai screenshot)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem(count = journals.size.toString(), label = "Entries")
                StatItem(count = "0", label = "Words Written") // Bisa dihitung nanti
                StatItem(count = "0", label = "Days Journaled")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Today", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)

            Spacer(modifier = Modifier.height(12.dp))

            // List Journal
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(journals) { journal ->
                    JournalCard(journal)
                }
            }
        }
    }
}

@Composable
fun StatItem(count: String, label: String) {
    Column {
        Text(text = count, color = Color(0xFF6C5DD3), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = label, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun JournalCard(journal: JournalEntity) {
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
                AsyncImage(
                    model = journal.photoUri, // URI gambar dari database
                    contentDescription = "Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color.DarkGray)
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Peta / Lokasi (Kanan - Placeholder)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color(0xFF2C2C2C)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Map View", color = Color.Gray, fontSize = 12.sp)
                    // Nanti ganti ini dengan Google Maps Lite Mode
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Judul & Deskripsi
            Text(text = journal.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                text = journal.description,
                color = Color.LightGray,
                fontSize = 14.sp,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Tanggal
            val dateFormat = SimpleDateFormat("EEEE, dd MMM", Locale.getDefault())
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dateFormat.format(journal.date),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            }
        }
    }
}