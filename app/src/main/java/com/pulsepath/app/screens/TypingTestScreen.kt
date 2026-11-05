package com.pulsepath.app.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsepath.app.components.BackArrow
import com.pulsepath.app.components.PrimaryButton
import com.pulsepath.app.components.SystemTypingSection
import com.pulsepath.app.data.TypingResult
import com.pulsepath.app.data.TypingStore
import com.pulsepath.app.ui.theme.BackgroundWhite
import com.pulsepath.app.ui.theme.CardBorder
import com.pulsepath.app.ui.theme.ChartBlueTop
import com.pulsepath.app.ui.theme.ChartCyanBottom
import com.pulsepath.app.ui.theme.NavyHeading
import com.pulsepath.app.ui.theme.PrimaryPurple
import com.pulsepath.app.ui.theme.SuccessGreen
import com.pulsepath.app.ui.theme.TextDark
import com.pulsepath.app.ui.theme.TextMuted
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val sentences = listOf(
    "The quick brown fox jumps over the lazy dog near the quiet river.",
    "Small daily habits build a calmer and more focused mind over time.",
    "Put your phone down for a moment and notice how the day feels.",
    "Good typing comes from steady rhythm, not from rushing every key.",
    "A short walk outside can clear your head better than another scroll."
)

private val ErrorRed = Color(0xFFE5484D)

@Composable
fun TypingTestScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val cardShape = RoundedCornerShape(28.dp)

    // Changing "attempt" starts a fresh test with a new sentence
    var attempt by remember { mutableIntStateOf(0) }
    val target = remember(attempt) { sentences.random() }
    var typed by remember(attempt) { mutableStateOf("") }
    var startTime by remember(attempt) { mutableLongStateOf(0L) }
    var result by remember(attempt) { mutableStateOf<TypingResult?>(null) }

    var historyVersion by remember { mutableIntStateOf(0) }
    val history = remember(historyVersion) { TypingStore.load(context) }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(attempt) {
        try {
            focusRequester.requestFocus()
        } catch (error: Exception) {
            // The keyboard just won't open by itself; the user can tap the box
        }
    }

    // The sentence, coloured letter by letter
    val styledSentence = buildAnnotatedString {
        target.forEachIndexed { i, ch ->
            val done = i < typed.length
            val wrong = done && typed[i] != ch
            val color = when {
                !done -> TextMuted
                wrong -> ErrorRed
                else -> SuccessGreen
            }
            withStyle(
                SpanStyle(
                    color = color,
                    fontWeight = if (done) FontWeight.Bold else FontWeight.Normal,
                    background = if (wrong) ErrorRed.copy(alpha = 0.15f) else Color.Unspecified
                )
            ) {
                append(ch)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackArrow(onClick = onBack)
            Text(
                text = "Typing speed",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NavyHeading
            )
        }

        Text(
            text = "Measure how fast and accurately you type.",
            fontSize = 17.sp,
            color = TextDark,
            modifier = Modifier.padding(start = 48.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // The sentence to copy
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .border(2.dp, CardBorder, cardShape)
                .padding(24.dp)
        ) {
            Text(
                text = "TYPE THIS SENTENCE",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = styledSentence,
                fontSize = 20.sp,
                lineHeight = 30.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = typed,
            onValueChange = { newText ->
                if (result == null && newText.length <= target.length) {
                    // The timer starts with the first letter
                    if (typed.isEmpty() && newText.isNotEmpty()) {
                        startTime = System.currentTimeMillis()
                    }
                    typed = newText

                    // Finished when the last letter is typed
                    if (newText.length == target.length) {
                        val now = System.currentTimeMillis()
                        val finished = calculateResult(target, newText, now - startTime, now)
                        result = finished
                        TypingStore.add(context, finished)
                        historyVersion++
                    }
                }
            },
            enabled = result == null,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            placeholder = { Text("Start typing here…") },
            minLines = 2,
            shape = RoundedCornerShape(20.dp),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.None
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        val finished = result
        if (finished == null) {
            Text(
                text = if (typed.isEmpty()) {
                    "The timer starts when you type the first letter."
                } else {
                    "Keep going. The test ends after the last letter."
                },
                fontSize = 14.sp,
                color = TextMuted,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            TextButton(onClick = { attempt++ }) {
                Text("New sentence", color = PrimaryPurple, fontWeight = FontWeight.Bold)
            }
        } else {
            // Result card
            val previous = history.filter { it.timeMillis != finished.timeMillis }
            val newBest = previous.isNotEmpty() && finished.wpm > previous.maxOf { it.wpm }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .border(2.dp, PrimaryPurple, cardShape)
                    .padding(24.dp)
            ) {
                Text(
                    text = if (newBest) "New personal best!" else "Your result",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (newBest) SuccessGreen else TextDark
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ResultNumber("WPM", finished.wpm.toString(), Modifier.weight(1f))
                    ResultNumber("Accuracy", "${finished.accuracy}%", Modifier.weight(1f))
                    ResultNumber(
                        "Time",
                        String.format(Locale.ENGLISH, "%.1fs", finished.durationMillis / 1000.0),
                        Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(text = "Try again", onClick = { attempt++ })
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Typing speed in other apps (needs the user's consent)
        SystemTypingSection()

        Spacer(modifier = Modifier.height(28.dp))

        // History
        Text(
            text = "Your progress",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (history.isEmpty()) {
            Text(
                text = "No tests yet. Finish one to see your progress here.",
                fontSize = 16.sp,
                color = TextMuted,
                lineHeight = 22.sp
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatTile("Best", "${history.maxOf { it.wpm }} WPM", Modifier.weight(1f))
                StatTile(
                    "Average",
                    "${history.map { it.wpm }.average().roundToInt()} WPM",
                    Modifier.weight(1f)
                )
                StatTile("Tests", history.size.toString(), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            HistoryChart(results = history.takeLast(10))

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "The chart shows words per minute for your last ${minOf(history.size, 10)} tests.",
                fontSize = 13.sp,
                color = TextMuted,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            val dateFormat = remember { DateTimeFormatter.ofPattern("MMM d, HH:mm", Locale.ENGLISH) }
            history.takeLast(5).reversed().forEach { r ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Instant.ofEpochMilli(r.timeMillis)
                            .atZone(ZoneId.systemDefault())
                            .format(dateFormat),
                        fontSize = 15.sp,
                        color = TextMuted,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${r.wpm} WPM  •  ${r.accuracy}%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                }
            }

            TextButton(onClick = {
                TypingStore.clear(context)
                historyVersion++
                Toast.makeText(context, "Typing history cleared", Toast.LENGTH_SHORT).show()
            }) {
                Text("Clear typing history", color = ErrorRed)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/** Counts the correct letters and turns them into WPM and accuracy. */
private fun calculateResult(
    target: String,
    typed: String,
    durationMillis: Long,
    now: Long
): TypingResult {
    var correct = 0
    for (i in typed.indices) {
        if (i < target.length && typed[i] == target[i]) correct++
    }
    // One "word" is 5 letters. A minimum of one second avoids dividing by almost zero.
    val minutes = maxOf(durationMillis, 1_000L) / 60_000.0
    val wpm = ((correct / 5.0) / minutes).roundToInt()
    val accuracy = if (typed.isEmpty()) 0 else (correct * 100.0 / typed.length).roundToInt()
    return TypingResult(now, wpm, accuracy, durationMillis)
}

@Composable
private fun ResultNumber(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextDark
        )
        Text(text = label, fontSize = 14.sp, color = TextMuted)
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .border(2.dp, CardBorder, shape)
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Text(text = label, fontSize = 13.sp, color = TextMuted)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextDark
        )
    }
}

@Composable
private fun HistoryChart(results: List<TypingResult>) {
    val shape = RoundedCornerShape(28.dp)
    val maxWpm = results.maxOf { it.wpm }.coerceAtLeast(1)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(2.dp, CardBorder, shape)
            .padding(16.dp)
    ) {
        results.forEach { r ->
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .fillMaxWidth()
                            .fillMaxHeight((r.wpm.toFloat() / maxWpm).coerceIn(0.04f, 1f))
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.verticalGradient(listOf(ChartBlueTop, ChartCyanBottom))
                            )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = r.wpm.toString(),
                    fontSize = 11.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}