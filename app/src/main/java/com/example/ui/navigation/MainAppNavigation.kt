package com.example.ui.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.data.repository.AnatomyRepository
import com.example.ui.ai.AITutorScreen
import com.example.ui.ar3d.ARViewerScreen
import com.example.ui.exam.FinalExamScreen
import com.example.ui.forum.ForumScreen
import com.example.ui.home.HomeScreen
import com.example.ui.progress.ProgressScreen
import com.example.ui.quiz.QuizScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object ArViewer : Screen("ar_explorer?structureId={structureId}&fullBody={fullBody}") {
        fun createRoute(structureId: String? = null, fullBody: Boolean = true): String {
            return if (structureId != null) {
                "ar_explorer?structureId=$structureId&fullBody=$fullBody"
            } else {
                "ar_explorer"
            }
        }
    }
    data object Quiz : Screen("quiz")
    data object Certificates : Screen("certificates")
    data object Forum : Screen("forum")
    data object Progress : Screen("progress")
    data object AiTutor : Screen("ai_tutor?prompt={prompt}") {
        fun createRoute(prompt: String? = null): String {
            return if (!prompt.isNullOrBlank()) {
                val encoded = URLEncoder.encode(prompt, StandardCharsets.UTF_8.toString())
                "ai_tutor?prompt=$encoded"
            } else {
                "ai_tutor"
            }
        }
    }
}

enum class BottomBarDestination(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home", "Cuerpo 2D", Icons.Filled.AccessibilityNew, Icons.Outlined.AccessibilityNew),
    AR_EXPLORER("ar_explorer", "3D / RA", Icons.Filled.ViewInAr, Icons.Outlined.ViewInAr),
    QUIZ("quiz", "Retos", Icons.Filled.Quiz, Icons.Outlined.Quiz),
    CERTIFICATES("certificates", "Diplomas", Icons.Filled.School, Icons.Outlined.School),
    FORUM("forum", "Comunidad", Icons.Filled.Forum, Icons.Outlined.Forum),
    AI_TUTOR("ai_tutor", "Tutor IA", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)
}

@Composable
fun MainAppNavigation() {
    val context = LocalContext.current
    val repository = remember { AnatomyRepository.getInstance(context) }
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestinationRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                BottomBarDestination.entries.forEach { dest ->
                    val isSelected = currentDestinationRoute?.startsWith(dest.route) == true
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                                contentDescription = dest.title
                            )
                        },
                        label = { Text(dest.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Interactive 2D Human Body Navigator Screen (Pantalla Principal)
            composable(Screen.Home.route) {
                HomeScreen(
                    repository = repository,
                    onNavigateToOrgan = { organId ->
                        navController.navigate(Screen.ArViewer.createRoute(structureId = organId, fullBody = false))
                    },
                    onNavigateToQuiz = { _ ->
                        navController.navigate(Screen.Quiz.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToAITutor = { query ->
                        navController.navigate(Screen.AiTutor.createRoute(prompt = query))
                    },
                    onNavigateToCertificates = {
                        navController.navigate(Screen.Certificates.route)
                    }
                )
            }

            // 2. 3D Anatomy & AR Explorer Screen
            composable(
                route = Screen.ArViewer.route,
                arguments = listOf(
                    navArgument("structureId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("fullBody") {
                        type = NavType.BoolType
                        defaultValue = true
                    }
                )
            ) { backStackEntry ->
                val structureId = backStackEntry.arguments?.getString("structureId")
                val fullBody = backStackEntry.arguments?.getBoolean("fullBody") ?: (structureId == null)

                ARViewerScreen(
                    repository = repository,
                    initialStructureId = structureId,
                    initialFullBody = fullBody,
                    onBackToHome = {
                        navController.popBackStack()
                    },
                    onConsultAITutor = { question ->
                        navController.navigate(Screen.AiTutor.createRoute(prompt = question))
                    }
                )
            }

            // 3. Quiz and Gamified Retos Screen
            composable(Screen.Quiz.route) {
                QuizScreen(
                    repository = repository,
                    onNavigateToExam = {
                        navController.navigate(Screen.Certificates.route)
                    }
                )
            }

            // 4. Digital Certificates and Final Exam Screen
            composable(Screen.Certificates.route) {
                FinalExamScreen(repository = repository)
            }

            // 5. Medical Community Discussion Forum Screen
            composable(Screen.Forum.route) {
                ForumScreen(repository = repository)
            }

            // 6. User Progress Screen
            composable(Screen.Progress.route) {
                ProgressScreen(
                    repository = repository,
                    onNavigateToCertificates = {
                        navController.navigate(Screen.Certificates.route)
                    }
                )
            }

            // 7. AI Medical Anatomy Tutor Screen
            composable(
                route = Screen.AiTutor.route,
                arguments = listOf(
                    navArgument("prompt") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val encodedPrompt = backStackEntry.arguments?.getString("prompt")
                val decodedPrompt = encodedPrompt?.let {
                    try {
                        URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
                    } catch (e: Exception) {
                        it
                    }
                }
                AITutorScreen(initialPrompt = decodedPrompt)
            }
        }
    }
}
