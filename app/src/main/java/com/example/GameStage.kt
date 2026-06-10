package com.example

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlin.math.sin

@Composable
fun GameStage(
    locationId: String,
    locationName: String,
    introTitle: String,
    dialogues: List<DialogueLine>,
    activeDialogueIndex: Int,
    floatingChanges: Map<CharacterKey, String>,
    lang: GameLanguage,
    modifier: Modifier = Modifier
) {
    val activeDialogue = dialogues.getOrNull(activeDialogueIndex)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(310.dp)
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(getStageBackground(locationId))
            .border(2.dp, Color(0xFFE0E0E0), RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
    ) {
        // Draw decorative environment layers
        DecorativeStageBackground(locationId = locationId)

        // Super Mario Mini Map / Progress Tracker at the very top
        MarioProgressMap(
            currentLocationId = locationId,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
        )

        // Title Tag of the location
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 68.dp, start = 12.dp)
                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Column {
                Text(
                    text = introTitle.uppercase(),
                    color = Color(0xFFFFD700),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = locationName,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Render characters side by side (hiding Nima if he is lost!)
        Row(
            modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 60.dp)
            .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            CharacterKey.values().forEach { charKey ->
                if (charKey == CharacterKey.NIMA && locationId != "victory") {
                    // Hide Nima from the active hiking group during levels
                    return@forEach
                }

                val isSpeaking = activeDialogue?.characterKey == charKey
                val animState = if (isSpeaking) activeDialogue.animationState else "Idle"
                val floatingVal = floatingChanges[charKey]

                CharacterSpriteContainer(
                    characterKey = charKey,
                    isSpeaking = isSpeaking,
                    animationState = animState,
                    floatingChange = floatingVal,
                    lang = lang
                )
            }
        }

        // Speech Bubble at the bottom of the stage
        if (activeDialogue != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(12.dp))
                    .border(2.dp, activeDialogue.characterKey.themeColor, RoundedCornerShape(12.dp))
                    .shadow(3.dp, RoundedCornerShape(12.dp))
                    .padding(8.dp)
                    .testTag("speech_bubble")
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${activeDialogue.characterKey.displayName(lang)} [${activeDialogue.animationState}]",
                            color = activeDialogue.characterKey.themeColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (lang == GameLanguage.FA) 
                                "دیالوگ ${activeDialogueIndex + 1}/${dialogues.size}" 
                            else 
                                "Dialogue ${activeDialogueIndex + 1}/${dialogues.size}",
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = activeDialogue.text(lang),
                        color = Color(0xFF212121),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MarioProgressMap(
    currentLocationId: String,
    modifier: Modifier = Modifier
) {
    val stageIndex = when (currentLocationId) {
        "Class01" -> 0
        "Gard01" -> 1
        "Sci01" -> 2
        "Cafe01" -> 3
        "Secret01" -> 4
        "victory" -> 5
        else -> 0
    }

    Row(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(vertical = 4.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val stages = listOf("1-1 🏫", "1-2 🌲", "1-3 🧪", "1-4 🍔", "1-5 🏁")
        stages.forEachIndexed { index, name ->
            val isActive = index == stageIndex
            val isCleared = index < stageIndex

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (isActive) Color(0xFFFFD700) 
                            else if (isCleared) Color(0xFF4CAF50) 
                            else Color(0xFF5D4037)
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isActive) {
                        Text("🏃‍♀️", fontSize = 11.sp)
                    } else if (isCleared) {
                        Text("⭐", fontSize = 9.sp, color = Color.White)
                    } else {
                        Text("?", fontSize = 9.sp, color = Color.White.copy(alpha = 0.5f))
                    }
                }
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = name,
                    color = if (isActive) Color(0xFFFFD700) else Color.White.copy(alpha = 0.6f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (index < stages.size - 1) {
                Text(
                    text = "»",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }
    }
}

@Composable
fun DecorativeStageBackground(locationId: String) {
    val transition = rememberInfiniteTransition()
    
    // Smooth looping translation to animate Mario-style obstacles
    val animOffset by transition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        when (locationId) {
            "Class01" -> {
                // Drawing classroom with moving platforms and rusty blockades
                drawRect(
                    color = Color(0xFF2E2E2E),
                    topLeft = Offset(0f, height * 0.75f),
                    size = Size(width, height * 0.25f)
                )
                // Drawing 2D Mario Brick blocks
                for (x in 0..10) {
                    drawRect(
                        color = Color(0xFF8D6E63),
                        topLeft = Offset(width * (x / 10f) + 4f, height * 0.77f),
                        size = Size(width / 10f - 8f, 25f)
                    )
                    drawRect(
                        color = Color(0xFF5D4037),
                        topLeft = Offset(width * (x / 10f) + 4f, height * 0.77f),
                        size = Size(width / 10f - 8f, 25f),
                        style = Stroke(width = 3f)
                    )
                }

                // Drawing Active obstacle: Rolling chalk barrels (World 1-1)
                val barrelCenter = Offset(width * 0.5f + (animOffset * 9f), height * 0.65f)
                drawCircle(color = Color(0xFFD84315), radius = 24f, center = barrelCenter)
                drawCircle(color = Color(0xFFFFE0B2), radius = 16f, center = barrelCenter)
                drawCircle(color = Color(0xFF424242), radius = 24f, center = barrelCenter, style = Stroke(width = 4f))
                
                // Draw a hazard warning sign
                drawRect(color = Color(0xFFFFD700), topLeft = Offset(width * 0.12f, height * 0.52f), size = Size(30f, 30f))
                drawLine(color = Color.Black, start = Offset(width * 0.12f + 15f, height * 0.52f + 5f), end = Offset(width * 0.12f + 15f, height * 0.52f + 25f), strokeWidth = 4f)
            }
            "Gard01" -> {
                // Background mountains and sky
                drawRect(
                    color = Color(0xFF4CAF50),
                    topLeft = Offset(0f, height * 0.82f),
                    size = Size(width, height * 0.18f)
                )

                // Active obstacle: Mutated Stinger Piranha Vine plant swinging (World 1-2)
                val stemPath = Path().apply {
                    moveTo(width * 0.5f, height * 0.85f)
                    quadraticTo(width * 0.48f + animOffset * 3f, height * 0.65f, width * 0.52f + animOffset * 6f, height * 0.55f)
                }
                drawPath(stemPath, color = Color(0xFF388E3C), style = Stroke(width = 8f))

                // Draw piranha plant head
                val headCenter = Offset(width * 0.52f + animOffset * 6f, height * 0.55f)
                drawCircle(color = Color(0xFFC62828), radius = 20f, center = headCenter) // Red head
                drawCircle(color = Color.White, radius = 5f, center = Offset(headCenter.x - 6f, headCenter.y - 6f)) // White dots
                drawCircle(color = Color.White, radius = 4f, center = Offset(headCenter.x + 8f, headCenter.y + 4f))

                // Fangs
                val fangsPath = Path().apply {
                    moveTo(headCenter.x - 12f, headCenter.y)
                    lineTo(headCenter.x - 6f, headCenter.y + 12f)
                    lineTo(headCenter.x, headCenter.y)
                    lineTo(headCenter.x + 6f, headCenter.y + 12f)
                    lineTo(headCenter.x + 12f, headCenter.y)
                }
                drawPath(fangsPath, color = Color.White)
            }
            "Sci01" -> {
                // Dark background ground
                drawRect(color = Color(0xFF37474F), topLeft = Offset(0f, height * 0.8f), size = Size(width, height * 0.2f))

                // Active obstacle: Glowing Acid/Lava puddle bubbling (World 1-3)
                val lavaColors = listOf(Color(0xFF00E5FF), Color(0xFFD500F9), Color(0xFF00E5FF))
                drawRect(
                    brush = Brush.horizontalGradient(lavaColors),
                    topLeft = Offset(width * 0.2f, height * 0.83f),
                    size = Size(width * 0.6f, height * 0.17f),
                    alpha = 0.85f
                )

                // Bubble bursts
                drawCircle(color = Color.White.copy(alpha = 0.6f), radius = 6f + (animOffset + 10f)/4f, center = Offset(width * 0.35f, height * 0.88f - (animOffset + 10f)))
                drawCircle(color = Color.White.copy(alpha = 0.6f), radius = 4f + (10f - animOffset)/4f, center = Offset(width * 0.55f, height * 0.85f - (10f - animOffset)))
            }
            "Cafe01" -> {
                // Ground
                drawRect(color = Color(0xFF795548), topLeft = Offset(0f, height * 0.82f), size = Size(width, height * 0.18f))

                // Active obstacle: Bouncing giant missile meatball rolling (World 1-4)
                val meatballX = width * 0.55f + animOffset * 10f
                val meatballY = height * 0.68f + kotlin.math.abs(animOffset * 2.5f)
                drawCircle(color = Color(0xFF8D6E63), radius = 22f, center = Offset(meatballX, meatballY))
                drawCircle(color = Color(0xFF5D4037), radius = 22f, center = Offset(meatballX, meatballY), style = Stroke(width = 4f))

                // Splashes of hot marinara sauce on projectile
                drawCircle(color = Color(0xFFC62828), radius = 6f, center = Offset(meatballX - 6f, meatballY - 5f))
                drawCircle(color = Color(0xFFC62828), radius = 8f, center = Offset(meatballX + 7f, meatballY + 6f))
            }
            "Secret01" -> {
                // Mystic mechanical floor tile
                drawRect(color = Color(0xFF263238), topLeft = Offset(0f, height * 0.78f), size = Size(width, height * 0.22f))
                
                // Active obstacle: Rotating laser system protection barrier (World 1-5)
                val laserX = width * 0.5f
                drawLine(
                    color = Color(0xFFFF1744),
                    start = Offset(laserX - 100f, height * 0.72f + animOffset * 4f),
                    end = Offset(laserX + 100f, height * 0.72f - animOffset * 4f),
                    strokeWidth = 6f
                )
                // Draw secure capsule where Nima is locked
                drawRoundRect(
                    color = Color(0xFF00E5FF),
                    topLeft = Offset(width * 0.72f, height * 0.44f),
                    size = Size(80f, 100f),
                    cornerRadius = CornerRadius(12f),
                    style = Stroke(width = 4f)
                )
                // Tiny outline of Nima inside the locked pod
                drawCircle(color = Color(0xFFFFD1A4), radius = 12f, center = Offset(width * 0.72f + 40f, height * 0.44f + 35f))
            }
            "victory" -> {
                // Drawing beautiful peaceful fireworks and golden streamers
                drawRect(color = Color(0xFF4CAF50), topLeft = Offset(0f, height * 0.85f), size = Size(width, height * 0.15f))
                
                // Colorful celebrate circles floating around
                val floatPos = (animOffset + 10f) / 20f
                drawCircle(color = Color(0xFFFFE082), radius = 12f, center = Offset(width * 0.25f, height * (0.35f + floatPos * 0.1f)))
                drawCircle(color = Color(0xFF81C784), radius = 10f, center = Offset(width * 0.75f, height * (0.4f - floatPos * 0.1f)))
                drawCircle(color = Color(0xFFF48FB1), radius = 15f, center = Offset(width * 0.5f, height * (0.28f + floatPos * 0.15f)))
            }
        }
    }
}

fun getStageBackground(locationId: String): Brush {
    return when (locationId) {
        "Class01" -> Brush.verticalGradient(listOf(Color(0xFFFFF9C4), Color(0xFFFBC02D))) // Saffron golden school theme
        "Gard01" -> Brush.verticalGradient(listOf(Color(0xFFE1F5FE), Color(0xFF81C784))) // Fresh grass and sky
        "Sci01" -> Brush.verticalGradient(listOf(Color(0xFF1F1C2C), Color(0xFF928DAB))) // Mystical dark laboratory
        "Cafe01" -> Brush.verticalGradient(listOf(Color(0xFFFFECB3), Color(0xFFFFB300))) // Warm cozy restaurant feel
        "Secret01" -> Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF263238))) // Final dark cyber core
        else -> Brush.verticalGradient(listOf(Color(0xFFECEFF1), Color(0xFF90A4AE))) // Neutrals
    }
}

@SuppressLint("DiscouragedApi")
@Composable
fun CharacterSpriteContainer(
    characterKey: CharacterKey,
    isSpeaking: Boolean,
    animationState: String,
    floatingChange: String?,
    lang: GameLanguage
) {
    val context = LocalContext.current
    var hasImageResource by remember(characterKey) { mutableStateOf(false) }
    var imageResId by remember(characterKey) { mutableStateOf(0) }

    // Dynamically query if the user's PNG image is in drawables
    LaunchedEffect(characterKey) {
        val resId = context.resources.getIdentifier(characterKey.avatarResName, "drawable", context.packageName)
        if (resId != 0) {
            imageResId = resId
            hasImageResource = true
        }
    }

    // Set up active animations
    val infiniteTransition = rememberInfiniteTransition()

    // 1. Hover/Float bounce animation when speaking
    val hoverOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isSpeaking) -12f else -2f,
        animationSpec = infiniteRepeatable(
            animation = twistyAnimationSpec(isSpeaking),
            repeatMode = RepeatMode.Reverse
        )
    )

    // 2. Playful sway / wobble based on energy
    val angleSway by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (animationState == "Energetic" || animationState == "Laughing") 220 else 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // 3. Shiver/Shaking offset if nervous
    val horizontalShiver by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 40, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Combined spring transformations
    val scaleFactor by animateFloatAsState(
        targetValue = if (isSpeaking) 1.25f else 0.88f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow)
    )
    val alphaDim by animateFloatAsState(
        targetValue = if (isSpeaking) 1f else 0.72f,
        animationSpec = tween(300)
    )

    val finalTranslationY = hoverOffset
    val finalTranslationX = if (animationState == "Nervous") horizontalShiver else 0f
    val finalRotation = if (animationState == "Energetic" || animationState == "Laughing" || isSpeaking) angleSway else 0f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier
            .width(66.dp)
            .height(210.dp)
            .graphicsLayer {
                translationY = finalTranslationY.dp.toPx()
                translationX = finalTranslationX.dp.toPx()
                scaleX = scaleFactor
                scaleY = scaleFactor
                rotationZ = finalRotation
                alpha = alphaDim
            }
            .testTag("character_box_${characterKey.name}")
    ) {
        // Floating score popup code: displays dynamic +5 / -3 above heads
        Column(
            modifier = Modifier.height(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = !floatingChange.isNullOrEmpty(),
                enter = fadeIn() + expandVerticallyOutProgress(),
                exit = fadeOut()
            ) {
                if (floatingChange != null) {
                    val isPositive = !floatingChange.startsWith("-")
                    val label = if (lang == GameLanguage.FA) "دوستی" else "F&K"
                    Text(
                        text = "$label $floatingChange",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPositive) Color(0xFF00C853) else Color(0xFFD50000),
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                            .border(1.dp, if (isPositive) Color(0xFF00C853) else Color(0xFFD50000), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Draw character representation
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (hasImageResource) {
                AsyncImage(
                    model = imageResId,
                    contentDescription = characterKey.displayName(lang),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Fallback: Custom beautifully designed Vector Canvas
                VectorCharacterCanvas(characterKey, animationState)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Small name indicator tag underneath
        Text(
            text = characterKey.displayName(lang).replace("@", ""),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            modifier = Modifier
                .background(characterKey.themeColor, RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

fun twistyAnimationSpec(isSpeaking: Boolean): DurationBasedAnimationSpec<Float> {
    return tween(durationMillis = if (isSpeaking) 320 else 1200, easing = FastOutSlowInEasing)
}

fun expandVerticallyOutProgress() = fadeIn(animationSpec = tween(400))

@Composable
fun VectorCharacterCanvas(characterKey: CharacterKey, state: String) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        when (characterKey) {
            CharacterKey.NIMA -> {
                // Wise teacher face (round, grey/black beard, hair)
                drawCircle(color = Color(0xFFFFD1A4), radius = w * 0.35f, center = Offset(w * 0.5f, h * 0.5f))
                // Grey hair and beard outline
                drawArc(
                    color = Color(0xFF757575),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(w * 0.15f, h * 0.15f),
                    size = Size(w * 0.7f, h * 0.45f)
                )
                // Draw grey beard at bottom
                drawArc(
                    color = Color(0xFF757575),
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(w * 0.22f, h * 0.55f),
                    size = Size(w * 0.56f, h * 0.3f)
                )
                // Big black intellectual glasses
                drawCircle(color = Color.Black, radius = w * 0.11f, center = Offset(w * 0.36f, h * 0.45f), style = Stroke(width = 6f))
                drawCircle(color = Color.Black, radius = w * 0.11f, center = Offset(w * 0.64f, h * 0.45f), style = Stroke(width = 6f))
                drawLine(color = Color.Black, start = Offset(w * 0.47f, h * 0.45f), end = Offset(w * 0.53f, h * 0.45f), strokeWidth = 6f)
                // Friendly eyes
                drawCircle(color = Color(0xFF3E2723), radius = 6f, center = Offset(w * 0.36f, h * 0.45f))
                drawCircle(color = Color(0xFF3E2723), radius = 6f, center = Offset(w * 0.64f, h * 0.45f))

                // Green teacher utility vest badge (authority)
                drawRect(Color(0xFF2E7D32), topLeft = Offset(w * 0.35f, h * 0.85f), size = Size(w * 0.3f, h * 0.15f))
            }
            CharacterKey.GHOOL_GHOOLAK -> {
                // Blue furry creature with COOL glasses 😎 and sandwich bread suit
                drawCircle(color = Color(0xFF0D47A1), radius = w * 0.36f, center = Offset(w * 0.5f, h * 0.5f))
                // Round bear ears
                drawCircle(color = Color(0xFF0D47A1), radius = w * 0.12f, center = Offset(w * 0.22f, h * 0.22f))
                drawCircle(color = Color(0xFF0D47A1), radius = w * 0.12f, center = Offset(w * 0.78f, h * 0.22f))
                drawCircle(color = Color(0xFF82B1FF), radius = w * 0.06f, center = Offset(w * 0.22f, h * 0.22f))
                drawCircle(color = Color(0xFF82B1FF), radius = w * 0.06f, center = Offset(w * 0.78f, h * 0.22f))

                // COOL 8-BIT SHADES 😎 (always present)
                drawRect(color = Color.Black, topLeft = Offset(w * 0.18f, h * 0.35f), size = Size(w * 0.28f, h * 0.12f))
                drawRect(color = Color.Black, topLeft = Offset(w * 0.54f, h * 0.35f), size = Size(w * 0.28f, h * 0.12f))
                drawLine(color = Color.Black, start = Offset(w * 0.46f, h * 0.4f), end = Offset(w * 0.54f, h * 0.4f), strokeWidth = 8f)
                // Sunglasses white reflection pixels
                drawRect(color = Color.White, topLeft = Offset(w * 0.22f, h * 0.37f), size = Size(w * 0.05f, h * 0.04f))
                drawRect(color = Color.White, topLeft = Offset(w * 0.58f, h * 0.37f), size = Size(w * 0.05f, h * 0.04f))

                // Huge sandwich suit: sandwich bread frame
                val path = Path().apply {
                    moveTo(w * 0.15f, h * 0.65f)
                    lineTo(w * 0.85f, h * 0.65f)
                    lineTo(w * 0.82f, h * 0.95f)
                    lineTo(w * 0.18f, h * 0.95f)
                    close()
                }
                drawPath(path, color = Color(0xFFD7CCC8)) // Bread body
                drawPath(path, color = Color(0xFF8D6E63), style = Stroke(width = 5f)) // Crust

                // Tomato and lettuce layers inside bread
                drawRect(color = Color(0xFFE53935), topLeft = Offset(w * 0.22f, h * 0.73f), size = Size(w * 0.56f, h * 0.06f)) // red tomato
                drawRect(color = Color(0xFF4CAF50), topLeft = Offset(w * 0.26f, h * 0.79f), size = Size(w * 0.48f, h * 0.06f)) // lettuce
            }
            CharacterKey.KHOSHGELAK -> {
                // Pink furry creature with cosmetics sparkle bows
                drawCircle(color = Color(0xFFF48FB1), radius = w * 0.36f, center = Offset(w * 0.5f, h * 0.5f))
                // Cute pink hair tufts
                drawArc(
                    color = Color(0xFFEC407A),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(w * 0.14f, h * 0.12f),
                    size = Size(w * 0.72f, h * 0.45f)
                )
                // Glowing eyes with gorgeous lashes
                drawCircle(color = Color(0xFF00695C), radius = 10f, center = Offset(w * 0.34f, h * 0.44f))
                drawCircle(color = Color(0xFF00695C), radius = 10f, center = Offset(w * 0.66f, h * 0.44f))
                drawCircle(color = Color.White, radius = 4.3f, center = Offset(w * 0.32f, h * 0.42f))
                drawCircle(color = Color.White, radius = 4.3f, center = Offset(w * 0.64f, h * 0.42f))

                // Beautiful lisp smile
                drawArc(
                    color = Color(0xFFEC407A),
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(w * 0.4f, h * 0.52f),
                    size = Size(w * 0.2f, h * 0.12f),
                    style = Stroke(width = 5f)
                )

                // Glittering makeup backpack straps
                drawLine(color = Color(0xFF00B0FF), start = Offset(w * 0.15f, h * 0.65f), end = Offset(w * 0.35f, h * 0.88f), strokeWidth = 10f)
                drawLine(color = Color(0xFF00B0FF), start = Offset(w * 0.85f, h * 0.65f), end = Offset(w * 0.65f, h * 0.88f), strokeWidth = 10f)

                // Glowing sparkles around beautiful hair
                if (state == "Fashionable" || state == "Self-aware") {
                    drawSparkle(this, w * 0.15f, h * 0.35f)
                    drawSparkle(this, w * 0.85f, h * 0.35f)
                }
            }
            CharacterKey.GHEL_GHELEK -> {
                // Navy blue spherical creature, big mouth, smiley shirt
                drawCircle(color = Color(0xFF1A237E), radius = w * 0.38f, center = Offset(w * 0.5f, h * 0.5f))

                // Energetic bright eyes
                drawCircle(color = Color.White, radius = 12f, center = Offset(w * 0.33f, h * 0.42f))
                drawCircle(color = Color.White, radius = 12f, center = Offset(w * 0.67f, h * 0.42f))
                drawCircle(color = Color.Black, radius = 6f, center = Offset(w * 0.33f, h * 0.42f))
                drawCircle(color = Color.Black, radius = 6f, center = Offset(w * 0.67f, h * 0.42f))

                if (state == "Laughing" || state == "Energetic") {
                    // Big laughing open mouth
                    drawArc(
                        color = Color(0xFFD50000),
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(w * 0.33f, h * 0.48f),
                        size = Size(w * 0.34f, h * 0.25f)
                    )
                } else {
                    // Huge cheerful smile
                    drawArc(
                        color = Color.Black,
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * 0.35f, h * 0.50f),
                        size = Size(w * 0.3f, h * 0.15f),
                        style = Stroke(width = 6f)
                    )
                }

                // Yellow cheerful SMILEY Face shirt
                drawRect(color = Color(0xFFFFEB3B), topLeft = Offset(w * 0.22f, h * 0.76f), size = Size(w * 0.56f, h * 0.24f))
                // Draw a small smiling black face on the shirt!
                drawCircle(color = Color.Black, radius = 12f, center = Offset(w * 0.5f, h * 0.88f), style = Stroke(width = 2.5f))
                drawCircle(color = Color.Black, radius = 1.5f, center = Offset(w * 0.45f, h * 0.85f))
                drawCircle(color = Color.Black, radius = 1.5f, center = Offset(w * 0.55f, h * 0.85f))
                drawArc(color = Color.Black, startAngle = 15f, sweepAngle = 150f, useCenter = false, topLeft = Offset(w * 0.46f, h * 0.87f), size = Size(w * 0.08f, h * 0.05f), style = Stroke(width = 2f))
            }
            CharacterKey.DIANA -> {
                // 11-year-old smart human girl (brown hair, neat glasses, blue sweater)
                drawCircle(color = Color(0xFFFFE0B2), radius = w * 0.33f, center = Offset(w * 0.5f, h * 0.5f))
                // Beautiful long brown hair flowing behind ears
                val hairPath = Path().apply {
                    moveTo(w * 0.15f, h * 0.5f)
                    quadraticTo(w * 0.3f, h * 0.1f, w * 0.5f, h * 0.1f)
                    quadraticTo(w * 0.7f, h * 0.1f, w * 0.85f, h * 0.5f)
                    lineTo(w * 0.88f, h * 0.9f)
                    lineTo(w * 0.12f, h * 0.9f)
                    close()
                }
                drawPath(hairPath, color = Color(0xFF5D4037))
                drawCircle(color = Color(0xFFFFE0B2), radius = w * 0.30f, center = Offset(w * 0.5f, h * 0.5f)) // Head overlays hair

                // Symmetrical round academic glasses
                drawCircle(color = Color(0xFFE65100), radius = w * 0.11f, center = Offset(w * 0.34f, h * 0.46f), style = Stroke(width = 4.5f))
                drawCircle(color = Color(0xFFE65100), radius = w * 0.11f, center = Offset(w * 0.66f, h * 0.46f), style = Stroke(width = 4.5f))
                drawLine(color = Color(0xFFE65100), start = Offset(w * 0.45f, h * 0.46f), end = Offset(w * 0.55f, h * 0.46f), strokeWidth = 4.5f)

                // Clever dark eyes
                drawCircle(color = Color(0xFF27170E), radius = 5f, center = Offset(w * 0.34f, h * 0.46f))
                drawCircle(color = Color(0xFF27170E), radius = 5f, center = Offset(w * 0.66f, h * 0.46f))

                // Intelligent organized smile
                drawArc(
                    color = Color(0xFFE65100),
                    startAngle = 10f,
                    sweepAngle = 160f,
                    useCenter = false,
                    topLeft = Offset(w * 0.42f, h * 0.56f),
                    size = Size(w * 0.16f, h * 0.08f),
                    style = Stroke(width = 3.5f)
                )

                // Neat blue knitted sweater collar
                drawRect(color = Color(0xFF1565C0), topLeft = Offset(w * 0.3f, h * 0.78f), size = Size(w * 0.4f, h * 0.22f))
                // Yellow collar lines
                drawLine(color = Color(0xFFFFD54F), start = Offset(w * 0.35f, h * 0.78f), end = Offset(w * 0.45f, h * 0.88f), strokeWidth = 4.2f)
                drawLine(color = Color(0xFFFFD54F), start = Offset(w * 0.65f, h * 0.78f), end = Offset(w * 0.55f, h * 0.88f), strokeWidth = 4.2f)
            }
        }
    }
}

private fun drawSparkle(scope: androidx.compose.ui.graphics.drawscope.DrawScope, x: Float, y: Float) {
    val size = 15f
    scope.drawLine(color = Color(0xFFFFF176), start = Offset(x - size, y), end = Offset(x + size, y), strokeWidth = 4f)
    scope.drawLine(color = Color(0xFFFFF176), start = Offset(x, y - size), end = Offset(x, y + size), strokeWidth = 4f)
    scope.drawCircle(color = Color.White, radius = 5f, center = Offset(x, y))
}
