package com.example.geojournal

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun JournalAppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        // Halaman Utama
        composable("home") {
            HomeScreen(
                onNavigateToAdd = { navController.navigate("add") }
            )
        }

        // Halaman Tambah
        composable("add") {
            AddJournalScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}