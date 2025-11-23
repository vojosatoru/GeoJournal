package com.example.geojournal

import androidx.compose.runtime.Composable
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
                onNavigateToMap = { navController.navigate("map") } // Navigasi ke Peta
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
            AddJournalScreen(
                onBack = { navController.popBackStack() },
                journalId = id
            )
        }

        // Halaman Travel Map (Baru)
        composable("map") {
            TravelMapScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}