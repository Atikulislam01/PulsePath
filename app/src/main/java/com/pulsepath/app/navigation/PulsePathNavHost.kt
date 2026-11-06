package com.pulsepath.app.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pulsepath.app.components.BottomNavBar
import com.pulsepath.app.data.SettingsPrefs
import com.pulsepath.app.screens.AppCategoriesScreen
import com.pulsepath.app.screens.AppDetailScreen
import com.pulsepath.app.screens.AppSessionTimelineScreen
import com.pulsepath.app.screens.AppSwitchingScreen
import com.pulsepath.app.screens.DataTransparencyScreen
import com.pulsepath.app.screens.HelpScreen
import com.pulsepath.app.screens.HomeScreen
import com.pulsepath.app.screens.InsightsScreen
import com.pulsepath.app.screens.LocalScreenAccent
import com.pulsepath.app.screens.NoDataScreen
import com.pulsepath.app.screens.OnboardingScreen
import com.pulsepath.app.screens.PrivacyScreen
import com.pulsepath.app.screens.ReportsScreen
import com.pulsepath.app.screens.SettingsScreen
import com.pulsepath.app.screens.TypingTestScreen
import com.pulsepath.app.screens.UsageAccessScreen
import com.pulsepath.app.screens.WeeklyTrendsScreen
import com.pulsepath.app.screens.WelcomeScreen
import com.pulsepath.app.ui.theme.BackgroundWhite

/** The main color of each screen. It matches the colors of the bottom bar tabs. */
private fun accentFor(route: String?): Color = when (route) {
    Routes.HOME -> Color(0xFF1E90FF)
    Routes.APPS, Routes.APP_DETAIL -> Color(0xFF7B61FF)
    Routes.INSIGHTS, Routes.CATEGORIES, Routes.SWITCHING, Routes.TYPING -> Color(0xFFFF8A00)
    Routes.REPORTS, Routes.WEEKLY_TRENDS, Routes.PRIVACY, Routes.TRANSPARENCY -> Color(0xFF12A57F)
    Routes.SETTINGS, Routes.HELP -> Color(0xFFE5484D)
    else -> Color(0xFF7B61FF)
}

@Composable
fun PulsePathNavHost() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Returning users skip the setup screens and start on Home
    val startDestination = remember {
        if (SettingsPrefs.isOnboardingDone(context)) Routes.HOME else Routes.WELCOME
    }

    // The bottom bar shows on the 5 tabs and on the detail screens
    val showBottomBar = currentRoute in Routes.bottomTabs ||
            currentRoute in Routes.detailRoutesWithBar

    // Which tab is highlighted. Sub-screens highlight the tab they belong to.
    val highlightedRoute = when (currentRoute) {
        Routes.CATEGORIES, Routes.SWITCHING -> Routes.INSIGHTS
        Routes.WEEKLY_TRENDS, Routes.PRIVACY, Routes.TRANSPARENCY -> Routes.REPORTS
        Routes.HELP -> Routes.SETTINGS
        else -> currentRoute
    }

    // The screen color fades smoothly when you move to another screen
    val accent by animateColorAsState(
        targetValue = accentFor(currentRoute),
        animationSpec = tween(450),
        label = "screenAccent"
    )

    // Goes to Home, clears everything before it, and remembers that setup is finished
    val goToHome = {
        SettingsPrefs.setOnboardingDone(context)
        navController.navigate(Routes.HOME) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    // Switches to one of the 5 bottom tabs
    val goToTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(Routes.HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundWhite,
        // Each screen handles its own status bar spacing
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = highlightedRoute,
                    onItemClick = goToTab
                )
            }
        }
    ) { innerPadding ->
        CompositionLocalProvider(LocalScreenAccent provides accent) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(innerPadding),
                // Screens fade in with a small upward slide instead of snapping
                enterTransition = {
                    fadeIn(tween(320)) + slideInVertically(tween(320)) { it / 16 }
                },
                exitTransition = { fadeOut(tween(180)) },
                popEnterTransition = {
                    fadeIn(tween(320)) + slideInVertically(tween(320)) { it / 16 }
                },
                popExitTransition = { fadeOut(tween(180)) }
            ) {
                composable(Routes.WELCOME) {
                    WelcomeScreen(
                        onGetStarted = { navController.navigate(Routes.ONBOARDING) }
                    )
                }

                composable(Routes.ONBOARDING) {
                    OnboardingScreen(
                        onBack = { navController.popBackStack() },
                        onNext = { navController.navigate(Routes.USAGE_ACCESS) }
                    )
                }

                composable(Routes.USAGE_ACCESS) {
                    UsageAccessScreen(
                        onBack = { navController.popBackStack() },
                        onGranted = { goToHome() },
                        onNotNow = { goToHome() }
                    )
                }

                composable(Routes.NO_DATA) {
                    NoDataScreen(
                        onBack = { navController.popBackStack() },
                        onStartMonitoring = { navController.navigate(Routes.USAGE_ACCESS) },
                        onLearnMore = { navController.navigate(Routes.TRANSPARENCY) }
                    )
                }

                composable(Routes.HOME) {
                    HomeScreen(
                        onGrantPermission = { navController.navigate(Routes.USAGE_ACCESS) }
                    )
                }

                composable(Routes.APPS) {
                    AppSessionTimelineScreen(
                        onBack = { navController.popBackStack() },
                        onSessionClick = { packageName ->
                            navController.navigate(Routes.appDetail(packageName))
                        }
                    )
                }

                // The route argument is still called "appName", but it carries the package name
                composable(
                    route = Routes.APP_DETAIL,
                    arguments = listOf(navArgument("appName") { type = NavType.StringType })
                ) { entry ->
                    val packageName = entry.arguments?.getString("appName") ?: ""
                    AppDetailScreen(
                        packageName = packageName,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Routes.INSIGHTS) {
                    InsightsScreen(
                        onBack = { navController.popBackStack() },
                        onOpenSwitching = { navController.navigate(Routes.SWITCHING) },
                        onOpenCategories = { navController.navigate(Routes.CATEGORIES) },
                        onOpenTyping = { navController.navigate(Routes.TYPING) }
                    )
                }

                composable(Routes.TYPING) {
                    TypingTestScreen(onBack = { navController.popBackStack() })
                }

                composable(Routes.SWITCHING) {
                    AppSwitchingScreen(onBack = { navController.popBackStack() })
                }

                composable(Routes.CATEGORIES) {
                    AppCategoriesScreen(onBack = { navController.popBackStack() })
                }

                composable(Routes.REPORTS) {
                    ReportsScreen(
                        onBack = { navController.popBackStack() },
                        onOpenWeekly = { navController.navigate(Routes.WEEKLY_TRENDS) },
                        onOpenPrivacy = { navController.navigate(Routes.PRIVACY) },
                        onOpenNoData = { navController.navigate(Routes.NO_DATA) }
                    )
                }

                composable(Routes.WEEKLY_TRENDS) {
                    WeeklyTrendsScreen(onBack = { navController.popBackStack() })
                }

                composable(Routes.PRIVACY) {
                    PrivacyScreen(
                        onBack = { navController.popBackStack() },
                        onExport = { goToTab(Routes.REPORTS) },
                        onOpenTransparency = { navController.navigate(Routes.TRANSPARENCY) }
                    )
                }

                composable(Routes.TRANSPARENCY) {
                    DataTransparencyScreen(onBack = { navController.popBackStack() })
                }

                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        onBack = { navController.popBackStack() },
                        onManageCategories = { navController.navigate(Routes.CATEGORIES) },
                        onExportData = { goToTab(Routes.REPORTS) },
                        onOpenHelp = { navController.navigate(Routes.HELP) }
                    )
                }

                composable(Routes.HELP) {
                    HelpScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}