package com.pulsepath.app.data

data class AppSession(
    val appName: String,
    val timeRange: String,
    val minutes: Int
)

// Fake data for now. We replace this with real phone data in a later step.
val sampleSessions = listOf(
    AppSession("YouTube", "06:12 AM - 06:45 AM", 33),
    AppSession("WhatsApp", "07:02 AM - 07:28 AM", 26),
    AppSession("Instagram", "10:15 AM - 11:02 AM", 47),
    AppSession("TikTok", "11:20 AM - 11:45 AM", 25),
    AppSession("Facebook", "01:05 PM - 01:31 PM", 26),
    AppSession("Chrome", "03:15 PM - 04:02 PM", 47),
    AppSession("Snapchat", "08:20 PM - 08:45 PM", 25)
)