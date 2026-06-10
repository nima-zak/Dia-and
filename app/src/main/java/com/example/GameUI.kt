package com.example

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameMainScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val currentScene by viewModel.currentScene.collectAsState()
    val fkScores by viewModel.fkScores.collectAsState()
    val hearts by viewModel.hearts.collectAsState()
    val currentDialogueIndex by viewModel.currentDialogueIndex.collectAsState()
    val floatingChanges by viewModel.floatingScoreChanges.collectAsState()
    val isAiMode by viewModel.isAiMode.collectAsState()
    val aiLoading by viewModel.aiLoading.collectAsState()
    val aiError by viewModel.aiError.collectAsState()
    val lang by viewModel.currentLanguage.collectAsState()

    val totalDialogues = currentScene.dialogues.size
    val isAtEnd = currentDialogueIndex >= totalDialogues - 1

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFFD700)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = if (lang == GameLanguage.FA) "فرار بزرگ نیما" else "Nima's Super Rescue",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.testTag("app_title")
                            )
                        }
                    },
                    actions = {
                        // Display Super Mario Lives system ❤️❤️❤️
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            repeat(3) { index ->
                                val active = index < hearts
                                Text(
                                    text = if (active) "❤️" else "🖤",
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 1.dp)
                                )
                            }
                        }

                        // Language Switcher Toggle
                        TextButton(
                            onClick = { viewModel.toggleLanguage() },
                            modifier = Modifier.testTag("lang_toggle")
                        ) {
                            Text(
                                text = if (lang == GameLanguage.FA) "🇬🇧 EN" else "🇮🇷 فارسی",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Help / Restart
                        IconButton(
                            onClick = { viewModel.restartGame() },
                            modifier = Modifier.testTag("restart_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Restart Game",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            modifier = modifier
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .testTag("game_lazy_column"),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // 1. DYNAMIC ANIMATED 2D STAGE AREA
                item {
                    GameStage(
                        locationId = currentScene.locationId,
                        locationName = currentScene.locationName(lang),
                        introTitle = currentScene.introTitle(lang),
                        dialogues = currentScene.dialogues,
                        activeDialogueIndex = currentDialogueIndex,
                        floatingChanges = floatingChanges,
                        lang = lang,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // 2. DIALOGUE SEGMENT SLIDER / CONTROLS
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back button
                        Button(
                            onClick = { viewModel.prevDialogue() },
                            enabled = currentDialogueIndex > 0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("prev_dialog_button")
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (lang == GameLanguage.FA) "قبلی" else "PREV",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Next / Action Label pointer
                        if (!isAtEnd) {
                            Button(
                                onClick = { viewModel.nextDialogue() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(44.dp)
                                    .testTag("next_dialog_button")
                            ) {
                                Text(
                                    text = if (lang == GameLanguage.FA) "مطالعه خط بعدی" else "READ NEXT",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = "Next", modifier = Modifier.size(16.dp))
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(44.dp)
                                    .background(
                                        Brush.horizontalGradient(listOf(Color(0xFF00C853), Color(0xFF64DD17))),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (lang == GameLanguage.FA) "پاسخ دهید!" else "DECIDE BELOW!",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Obstacle Warning Indicator
                if (currentScene.obstacle(lang).isNotEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFF3E0)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .border(1.5.dp, Color(0xFFFF9800), RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⚠️", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (lang == GameLanguage.FA) "مانع فعال مرحله:" else "ACTIVE LEVEL OBSTACLE:",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp,
                                        color = Color(0xFFE65100)
                                    )
                                    Text(
                                        text = currentScene.obstacle(lang),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF5D4037)
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. FRIENDSHIP & KINDNESS (F&K) PERFORMANCE BOARD
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = if (lang == GameLanguage.FA) "دفترچه دوستی و مهربانی (F&K)" else "Friendship & Kindness (F&K) Ledger",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Student cards horizontal split
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val activeStudents = CharacterKey.values().filter { it != CharacterKey.NIMA }
                            activeStudents.forEach { student ->
                                val score = fkScores[student] ?: student.defaultFk
                                val maxScore = 50f
                                val progress = (score / maxScore).coerceIn(0f, 1f)

                                val ratingText = when {
                                    score >= 22 -> if (lang == GameLanguage.FA) "همکار و مشتاق ❤️" else "COOPERATIVE ❤️"
                                    score >= 12 -> if (lang == GameLanguage.FA) "دوستانه 😊" else "FRIENDLY 😊"
                                    score >= 6 -> if (lang == GameLanguage.FA) "خنثی 😐" else "NEUTRAL 😐"
                                    else -> if (lang == GameLanguage.FA) "بحرانی ⚠️ (از همکاری خودداری می‌کند)" else "CRITICAL ⚠️ (REFUSES TO HELP)"
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(1.dp, RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                        .border(
                                            1.dp,
                                            if (score < 6) Color(0xFFFF8A80) else Color.Transparent,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Mini icon preview
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(student.themeColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = student.displayName(lang).take(2),
                                            color = student.themeColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = student.displayName(lang),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = student.themeColor
                                            )
                                            Text(
                                                text = "$score / 50",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { progress },
                                            color = if (score < 6) Color(0xFFD50000) else student.themeColor,
                                            trackColor = MaterialTheme.colorScheme.secondaryContainer,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = ratingText,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (score < 6) Color(0xFFD50000) else Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. GEMINI AI PROTOTYPE SETTINGS & SANDBOX
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFF00B0FF),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (lang == GameLanguage.FA) "هوش مصنوعی فعال (جمینای)" else "Gemini AI Sandbox Mode",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Switch(
                                    checked = isAiMode,
                                    onCheckedChange = { viewModel.toggleAiMode(it) },
                                    modifier = Modifier
                                        .scale(0.75f)
                                        .testTag("ai_toggle")
                                )
                            }

                            if (isAiMode) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (lang == GameLanguage.FA) 
                                        "✨ تولید بی‌نهایت سناریو بر اساس پاسخ‌های شما با مدل قدرتمند Gemini 3.5. هر انتخاب مسیر متفاوتی پدید می‌آورد."
                                    else 
                                        "✨ Dynamically generates infinite school scenarios using Gemini 3.5 Flash! Each choice shapes a unique storyline.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (BuildConfig.GEMINI_API_KEY.isEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(8.dp))
                                            .padding(8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (lang == GameLanguage.FA) 
                                                    "کلید جمینای یافت نشد. بازی به طور خودکار به صورت محلی و آفلاین اجرا می‌شود."
                                                else 
                                                    "No Gemini Key found in secrets. App runs in local offline mode automatically.",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFFE65100)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. INTERACTIVE CHOICES OR LOADING OVERLAYS
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (lang == GameLanguage.FA) "کوره تصمیم‌گیری: هدایت تیم نجات" else "Decision Forge: Direct the Guild",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        if (aiLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .shadow(2.dp, RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(color = Color(0xFF00E5FF), modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = if (lang == GameLanguage.FA) "مشورت با هوش کیهانی جمینای..." else "Consulting the Gemini Cosmos...",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF00B0FF)
                                    )
                                    Text(
                                        text = if (lang == GameLanguage.FA) "کتابت گفتگوی شخصیت‌ها و ترسیم موانع..." else "Scribing dialogue and scoring personality vectors...",
                                        fontSize = 10.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        } else {
                            if (aiError != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                        .background(Color(0xFFFFEBEE), RoundedCornerShape(12.dp))
                                        .border(1.dp, Color(0xFFFFCDD2), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = aiError!!,
                                        color = Color(0xFFC62828),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Choices list
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                currentScene.choices.forEachIndexed { idx, choice ->
                                    val choiceColor = when (choice.type) {
                                        ChoiceType.HELPFUL -> Color(0xFFE8F5E9)
                                        ChoiceType.CREATIVE -> Color(0xFFE3F2FD)
                                        ChoiceType.SELFISH -> Color(0xFFFFF3E0)
                                    }
                                    val borderColor = when (choice.type) {
                                        ChoiceType.HELPFUL -> Color(0xFF4CAF50)
                                        ChoiceType.CREATIVE -> Color(0xFF2196F3)
                                        ChoiceType.SELFISH -> Color(0xFFFF9800)
                                    }

                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = choiceColor
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                                            .clickable(enabled = isAtEnd) {
                                                viewModel.makeChoice(choice)
                                            }
                                            .testTag("choice_card_${choice.type.name}_$idx")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = when (choice.type) {
                                                    ChoiceType.HELPFUL -> Icons.Default.Favorite
                                                    ChoiceType.CREATIVE -> Icons.Default.Edit
                                                    ChoiceType.SELFISH -> Icons.Default.Warning
                                                },
                                                contentDescription = null,
                                                tint = borderColor,
                                                modifier = Modifier.size(20.dp)
                                            )

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column {
                                                Text(
                                                    text = when (choice.type) {
                                                        ChoiceType.HELPFUL -> if (lang == GameLanguage.FA) "همکاری و مهربانی (بای‌پس)" else "KINDNESS & HELP"
                                                        ChoiceType.CREATIVE -> if (lang == GameLanguage.FA) "خلاقیت گروهی (بای‌پس)" else "GROUP CREATIVITY"
                                                        ChoiceType.SELFISH -> if (lang == GameLanguage.FA) "شخصی و تنبلی (برخورد به مانع: کسر قلب!)" else "SELFISH & LAZY (OBSTACLE HIT: LOSE HEART!)"
                                                    },
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = borderColor,
                                                    letterSpacing = 0.5.sp
                                                )
                                                Spacer(modifier = Modifier.height(1.dp))
                                                Text(
                                                    text = choice.text(lang),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF212121)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (!isAtEnd) {
                                Text(
                                    text = if (lang == GameLanguage.FA) 
                                        "🔒 خطوط دیالوگ بالا را تا انتها مطالعه کنید تا دکمه‌های تصمیم‌گیری فعال شوند."
                                    else 
                                        "🔒 Complete reading all character dialogue lines above to activate choices.",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Mario-Style GAME OVER Screen overlay if hearts == 0!
        if (hearts <= 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f))
                    .clickable(enabled = false) {}, // absorb clicks
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "💀",
                        fontSize = 72.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = "GAME OVER",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFF1744),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (lang == GameLanguage.FA) "بازی تمام شد!" else "YOU HIT TOO MANY OBSTACLES",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (lang == GameLanguage.FA) 
                            "تیم نجات در اثر موانع سخت خسته شد و آسیب دید. برای نجات آقا نیما دوباره تلاش کنید!" 
                        else 
                            "The rescue team coordinates fell after hitting too many platformer traps. Try again to locate teacher Nima!",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                    Button(
                        onClick = { viewModel.restartGame() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E5FF),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(50.dp)
                            .testTag("retry_game_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (lang == GameLanguage.FA) "تلاش مجدد و شروع دوباره" else "RETRY QUEST",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
