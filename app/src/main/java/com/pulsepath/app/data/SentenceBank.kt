package com.pulsepath.app.data

private val subjects = listOf(
    "The old fisherman", "A curious child", "My neighbor", "The tired traveler",
    "Our teacher", "The young artist", "A friendly baker", "The quiet librarian",
    "Her grandfather", "The village doctor", "The farmer", "Two best friends",
    "The new student", "The gardener", "The postman", "My sister",
    "The tall pilot", "The shy musician", "An old sailor", "The little girl"
)

private val actions = listOf(
    "carried a basket of fresh apples", "read a long story aloud",
    "painted a picture of the hills", "shared warm bread with everyone",
    "walked slowly along the path", "wrote a thoughtful letter",
    "fixed a broken wooden chair", "listened to the gentle rain",
    "planted a row of bright flowers", "sang a quiet song",
    "watched the clouds drift by", "counted the stars one by one",
    "made a pot of strong tea", "learned a new word",
    "told a funny tale", "built a small model boat",
    "folded a stack of clean clothes", "took a deep and calming breath",
    "found a shiny coin", "drew a map of the town",
    "cooked a simple evening meal", "waved to a passing train",
    "solved a tricky puzzle", "picked ripe berries",
    "tidied the whole room"
)

private val endings = listOf(
    "near the old stone bridge", "before the sun came up",
    "on a quiet Sunday morning", "beside the calm blue lake",
    "after a long day of work", "under the shade of an oak tree",
    "while the town was still asleep", "in the warm light of the evening",
    "at the edge of the forest", "during a short winter afternoon",
    "on the porch of the little house", "just before the rain began",
    "across from the busy market", "with a smile on the face",
    "at the end of the summer", "inside the cozy kitchen",
    "beneath a sky full of stars", "on the hill above the village",
    "as the church bell rang", "in the middle of spring"
)

/** 20 x 25 x 20 = 10,000 different sentences. */
val generatedSentences: List<String> = buildList {
    for (subject in subjects) {
        for (action in actions) {
            for (ending in endings) {
                add("$subject $action $ending.")
            }
        }
    }
}