package com.pulsepath.app.navigation

object Routes {
    const val WELCOME = "welcome"
    const val ONBOARDING = "onboarding"
    const val USAGE_ACCESS = "usage_access"
    const val NO_DATA = "no_data"

    // Bottom navigation tabs
    const val HOME = "home"
    const val APPS = "apps"
    const val INSIGHTS = "insights"
    const val REPORTS = "reports"
    const val SETTINGS = "settings"

    // Detail screens
    const val APP_DETAIL = "app_detail/{appName}"
    fun appDetail(appName: String) = "app_detail/$appName"
    const val CATEGORIES = "categories"
    const val SWITCHING = "app_switching"
    const val WEEKLY_TRENDS = "weekly_trends"
    const val PRIVACY = "privacy"
    const val TRANSPARENCY = "transparency"
    const val HELP = "help"

    val bottomTabs = listOf(HOME, APPS, INSIGHTS, REPORTS, SETTINGS)

    // Screens that are not tabs but still show the bottom bar
    val detailRoutesWithBar = listOf(
        APP_DETAIL, CATEGORIES, SWITCHING, WEEKLY_TRENDS, PRIVACY, TRANSPARENCY, HELP
    )
}