package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.SettingsManager
import com.example.ui.screens.*
import com.example.ui.theme.DramelioTheme
import com.example.ui.theme.toColor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settingsManager = SettingsManager(applicationContext)

        setContent {
            val config by settingsManager.backendConfig.collectAsStateWithLifecycle()
            val userProfile by settingsManager.userProfile.collectAsStateWithLifecycle()
            val movies by settingsManager.movies.collectAsStateWithLifecycle()
            val transactions by settingsManager.transactions.collectAsStateWithLifecycle()

            val primaryColor = config.appThemePrimaryHex.toColor(Color(0xFFE50914))

            // Periodic background sync loop (every 12 seconds) to keep content & subscription status up-to-date securely
            LaunchedEffect(config.isRemoteConfigEnabled, config.remoteConfigUrl, userProfile.email) {
                if (config.isRemoteConfigEnabled && config.remoteConfigUrl.isNotBlank()) {
                    while (true) {
                        try {
                            settingsManager.syncSubscriptionsAndContent()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        kotlinx.coroutines.delay(12000L)
                    }
                }
            }

            DramelioTheme(config = config) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // Display bottom bar only on primary structural roots (home, checkout, profile)
                        if (currentRoute == "home" || currentRoute == "checkout" || currentRoute == "profile") {
                            NavigationBar(
                                containerColor = Color(0xFF161616),
                                contentColor = Color.White,
                                modifier = Modifier.testTag("app_bottom_nav")
                            ) {
                                NavigationBarItem(
                                    selected = currentRoute == "home",
                                    onClick = { 
                                        if (currentRoute != "home") {
                                            navController.navigate("home") {
                                                popUpTo("home") { inclusive = true }
                                            }
                                        }
                                    },
                                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Beranda") },
                                    label = { Text("Beranda", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        selectedTextColor = Color.White,
                                        indicatorColor = primaryColor,
                                        unselectedIconColor = Color.Gray,
                                        unselectedTextColor = Color.Gray
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentRoute == "checkout",
                                    onClick = {
                                        if (currentRoute != "checkout") {
                                            navController.navigate("checkout") {
                                                popUpTo("home")
                                            }
                                        }
                                    },
                                    icon = { Icon(imageVector = Icons.Default.WorkspacePremium, contentDescription = "Langganan") },
                                    label = { Text("VIP Akses", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        selectedTextColor = Color.White,
                                        indicatorColor = primaryColor,
                                        unselectedIconColor = Color.Gray,
                                        unselectedTextColor = Color.Gray
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentRoute == "profile",
                                    onClick = {
                                        if (currentRoute != "profile") {
                                            navController.navigate("profile") {
                                                popUpTo("home")
                                            }
                                        }
                                    },
                                    icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Profil") },
                                    label = { Text("Profil", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        selectedTextColor = Color.White,
                                        indicatorColor = primaryColor,
                                        unselectedIconColor = Color.Gray,
                                        unselectedTextColor = Color.Gray
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavHost(
                            navController = navController,
                            startDestination = if (userProfile.isRegistered) "home" else "register"
                        ) {
                            composable("register") {
                                RegisterScreen(
                                    currentEmail = userProfile.email,
                                    config = config,
                                    onRegisterSuccess = { name, email ->
                                        settingsManager.saveUserProfile(
                                            userProfile.copy(
                                                name = name,
                                                email = email,
                                                isRegistered = true
                                            )
                                        )
                                        navController.navigate("home") {
                                            popUpTo("register") { inclusive = true }
                                        }
                                    },
                                    primaryColor = primaryColor
                                )
                            }

                            composable("home") {
                                HomeScreen(
                                    movies = movies,
                                    userProfile = userProfile,
                                    config = config,
                                    onSelectMovie = { movie ->
                                        navController.navigate("detail/${movie.id}")
                                    },
                                    onNavigateToProfile = { navController.navigate("profile") },
                                    onNavigateToCheckout = { navController.navigate("checkout") },
                                    primaryColor = primaryColor
                                )
                            }

                            composable(
                                route = "detail/{movieId}",
                                arguments = listOf(navArgument("movieId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val movieId = backStackEntry.arguments?.getString("movieId")
                                val targetMovie = movies.find { it.id == movieId } ?: movies.first()

                                DetailScreen(
                                    movie = targetMovie,
                                    userProfile = userProfile,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToCheckout = { navController.navigate("checkout") },
                                    primaryColor = primaryColor
                                )
                            }

                            composable("checkout") {
                                CheckoutScreen(
                                    config = config,
                                    onNavigateBack = { navController.popBackStack() },
                                    onSubmitTransaction = { tx ->
                                        settingsManager.addTransaction(tx)
                                    },
                                    onPaymentSuccess = {
                                        // Update the latest pending transaction to success
                                        val latestPending = transactions.firstOrNull { it.status == "PENDING" }
                                        if (latestPending != null) {
                                            settingsManager.updateTransactionStatus(latestPending.id, "PAID")
                                        } else {
                                            // Fallback upgrade in case direct simulation is triggered directly
                                            settingsManager.updateTransactionStatus("", "PAID")
                                        }
                                        navController.navigate("profile") {
                                            popUpTo("home")
                                        }
                                    },
                                    primaryColor = primaryColor
                                )
                            }

                            composable("profile") {
                                ProfileScreen(
                                    userProfile = userProfile,
                                    transactions = transactions,
                                    config = config,
                                    onSaveProfile = { name, email ->
                                        settingsManager.saveUserProfile(
                                            userProfile.copy(name = name, email = email)
                                        )
                                    },
                                    onNavigateBack = { navController.navigate("home") },
                                    onNavigateToCheckout = { navController.navigate("checkout") },
                                    onResetData = {
                                        settingsManager.resetToDefaults()
                                        navController.navigate("register") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    },
                                    primaryColor = primaryColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
