package com.example.geojournal

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState // PERBAIKAN: Import ini
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
        // Halaman Utama
        composable("home") {
            HomeScreen(
                onNavigateToAdd = { navController.navigate("add") },
                onNavigateToEdit = { journalId -> navController.navigate("add?id=$journalId") },
                onNavigateToMap = { navController.navigate("map") }
            )
        }

        // Halaman Tambah/Edit
        composable(
            route = "add?id={id}",
            arguments = listOf(navArgument("id") {
                type = NavType.IntType
                defaultValue = -1
            })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: -1

            // PERBAIKAN: Menggunakan getStateFlow agar tidak butuh library LiveData
            val savedStateHandle = backStackEntry.savedStateHandle
            val capturedUriString by savedStateHandle.getStateFlow<String?>("captured_image_uri", null).collectAsState()

            AddJournalScreen(
                onBack = { navController.popBackStack() },
                journalId = id,
                capturedImageUri = capturedUriString?.let { Uri.parse(it) },
                onNavigateToCamera = { navController.navigate("camera") }
            )
        }

        // Halaman Peta Travel
        composable("map") {
            TravelMapScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Halaman Kamera
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
    }
}