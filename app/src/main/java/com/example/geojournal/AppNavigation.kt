package com.example.geojournal

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun JournalAppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        // --- HOME ---
        composable("home") {
            HomeScreen(
                onNavigateToAdd = { navController.navigate("add") },
                onNavigateToEdit = { journalId -> navController.navigate("add?id=$journalId") },
                onNavigateToMap = { navController.navigate("map") }
            )
        }

        // --- ADD / EDIT JOURNAL ---
        composable(
            route = "add?id={id}",
            arguments = listOf(navArgument("id") {
                type = NavType.IntType
                defaultValue = -1
            })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: -1

            val savedStateHandle = backStackEntry.savedStateHandle

            // 1. Menerima Data Foto (URI)
            val capturedUriString by savedStateHandle.getStateFlow<String?>("captured_image_uri", null).collectAsState()

            // 2. Menerima Data Lokasi (Lat & Lon) - FITUR BARU
            val pickedLat by savedStateHandle.getStateFlow<Double?>("picked_lat", null).collectAsState()
            val pickedLon by savedStateHandle.getStateFlow<Double?>("picked_lon", null).collectAsState()

            AddJournalScreen(
                onBack = { navController.popBackStack() },
                journalId = id,
                capturedImageUri = capturedUriString?.let { Uri.parse(it) },
                pickedLocation = if (pickedLat != null && pickedLon != null) Pair(pickedLat!!, pickedLon!!) else null, // Kirim ke Screen
                onNavigateToCamera = { navController.navigate("camera") },
                onNavigateToMapPicker = { navController.navigate("pick_location") } // Navigasi ke Map Picker
            )
        }

        // --- GLOBAL MAP ---
        composable("map") {
            TravelMapScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // --- CAMERA ---
        composable("camera") {
            CameraScreen(
                onImageCaptured = { uri ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("captured_image_uri", uri.toString())
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        // --- LOCATION PICKER (BARU) ---
        composable("pick_location") {
            LocationPickerScreen(
                onLocationSelected = { lat, lon ->
                    // Simpan hasil ke SavedStateHandle milik AddJournalScreen
                    navController.previousBackStackEntry?.savedStateHandle?.set("picked_lat", lat)
                    navController.previousBackStackEntry?.savedStateHandle?.set("picked_lon", lon)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}