package com.example.geojournal

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu // PERBAIKAN: Ikon pengganti
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddJournalScreen(
    viewModel: JournalViewModel = viewModel(),
    onBack: () -> Unit,
    journalId: Int,
    capturedImageUri: Uri? = null,
    onNavigateToCamera: () -> Unit
) {
    val context = LocalContext.current

    // State Form
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var savedImagePath by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    // State UI
    var locationStatus by remember { mutableStateOf("Add Location") }
    var isLocationLoading by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }

    // 1. Handle Hasil Foto dari Kamera
    LaunchedEffect(capturedImageUri) {
        if (capturedImageUri != null) {
            selectedImageUri = capturedImageUri
            savedImagePath = capturedImageUri.path ?: ""
        }
    }

    // 2. Load Data jika Edit Mode
    LaunchedEffect(journalId) {
        if (journalId != -1) {
            isEditMode = true
            val journal = viewModel.getJournalById(journalId)
            if (journal != null) {
                title = journal.title
                description = journal.description
                savedImagePath = journal.photoUri
                if (journal.photoUri.isNotEmpty()) {
                    selectedImageUri = Uri.parse(journal.photoUri)
                }
                latitude = journal.latitude
                longitude = journal.longitude
                if (latitude != null) locationStatus = "Location Saved ✓"
            }
        }
    }

    // Launchers
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            isLocationLoading = true
            getCurrentLocation(context, fusedLocationClient) { lat, lon ->
                latitude = lat
                longitude = lon
                locationStatus = "Location Saved ✓"
                isLocationLoading = false
            }
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Photo Picker (Galeri)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            val path = copyUriToInternalStorage(context, uri)
            if (path != null) savedImagePath = path
        }
    }

    // Permission Kamera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onNavigateToCamera()
        } else {
            Toast.makeText(context, "Izin kamera diperlukan", Toast.LENGTH_SHORT).show()
        }
    }

    // UI Structure (Dialog Pilihan)
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Choose Image Source") },
            text = { Text("Take a new photo or select from gallery?") },
            confirmButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        onNavigateToCamera()
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp)) // Placeholder Icon
                        Spacer(Modifier.width(4.dp))
                        Text("Camera")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Menu, null, modifier = Modifier.size(16.dp)) // Placeholder Icon
                        Spacer(Modifier.width(4.dp))
                        Text("Gallery")
                    }
                }
            },
            containerColor = Color(0xFF2C2C2C),
            titleContentColor = Color.White,
            textContentColor = Color.Gray
        )
    }

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Entry" else "New Entry", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    TextButton(onClick = {
                        val finalId = if (isEditMode) journalId else 0
                        viewModel.addJournal(finalId, title, description, savedImagePath, latitude, longitude)
                        onBack()
                    }) {
                        Text("Done", color = Color(0xFF6C5DD3), style = MaterialTheme.typography.titleMedium)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121212))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Box Gambar (Klik untuk muncul Dialog)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E1E1E))
                    .clickable { showImageSourceDialog = true },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null || savedImagePath.isNotEmpty()) {
                    AsyncImage(
                        model = selectedImageUri ?: File(savedImagePath),
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Add, "Add Photo", tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Text("Add Cover Photo", color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tombol Lokasi
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (latitude != null) Color(0xFF2C2C2C) else Color.Transparent)
                    .clickable {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            isLocationLoading = true
                            getCurrentLocation(context, fusedLocationClient) { lat, lon ->
                                latitude = lat
                                longitude = lon
                                locationStatus = "Location Saved ✓"
                                isLocationLoading = false
                            }
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                            )
                        }
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = if (latitude != null) Color(0xFF6C5DD3) else Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (isLocationLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Getting location...", color = Color.Gray, fontSize = 14.sp)
                } else {
                    Text(
                        text = if (latitude != null) locationStatus else "Add/Update Location",
                        color = if (latitude != null) Color.White else Color.Gray,
                        fontWeight = if (latitude != null) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            }

            HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))

            // Input Title
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Title", color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(color = Color.DarkGray)

            // Input Description
            TextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Start writing...", color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
        }
    }
}

// Helper Functions
fun getCurrentLocation(
    context: Context,
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    onLocationReceived: (Double, Double) -> Unit
) {
    try {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    onLocationReceived(location.latitude, location.longitude)
                } else {
                    Toast.makeText(context, "Cannot get location", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to get location", Toast.LENGTH_SHORT).show()
            }
    } catch (e: SecurityException) {
        e.printStackTrace()
    }
}

fun copyUriToInternalStorage(context: Context, uri: Uri): String? {
    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    val fileName = "journal_${System.currentTimeMillis()}.jpg"
    val file = File(context.filesDir, fileName)
    return try {
        val outputStream = FileOutputStream(file)
        inputStream?.use { input -> outputStream.use { output -> input.copyTo(output) } }
        file.absolutePath
    } catch (e: Exception) { e.printStackTrace(); null }
}