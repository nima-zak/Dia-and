package com.example

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GameViewModel(application: Application) : AndroidViewModel(application) {

    // Language State
    private val _currentLanguage = MutableStateFlow(GameLanguage.FA) // Start with Persian as default
    val currentLanguage: StateFlow<GameLanguage> = _currentLanguage.asStateFlow()

    fun toggleLanguage() {
        _currentLanguage.value = if (_currentLanguage.value == GameLanguage.EN) GameLanguage.FA else GameLanguage.EN
    }

    // F&K Score Tracking
    private val _fkScores = MutableStateFlow<Map<CharacterKey, Int>>(
        mapOf(
            CharacterKey.GHOOL_GHOOLAK to CharacterKey.GHOOL_GHOOLAK.defaultFk,
            CharacterKey.KHOSHGELAK to CharacterKey.KHOSHGELAK.defaultFk,
            CharacterKey.GHEL_GHELEK to CharacterKey.GHEL_GHELEK.defaultFk,
            CharacterKey.DIANA to CharacterKey.DIANA.defaultFk
        )
    )
    val fkScores: StateFlow<Map<CharacterKey, Int>> = _fkScores.asStateFlow()

    // Super Mario Life System (3 Hearts)
    private val _hearts = MutableStateFlow(3)
    val hearts: StateFlow<Int> = _hearts.asStateFlow()

    // Current Scene State
    private val _currentScene = MutableStateFlow<Scene>(StoryDatabase.scenes["start"]!!)
    val currentScene: StateFlow<Scene> = _currentScene.asStateFlow()

    // Active dialogue step for sequential animation
    private val _currentDialogueIndex = MutableStateFlow(0)
    val currentDialogueIndex: StateFlow<Int> = _currentDialogueIndex.asStateFlow()

    // Interactive floating offsets for F&K change animations
    private val _floatingScoreChanges = MutableStateFlow<Map<CharacterKey, String>>(emptyMap())
    val floatingScoreChanges: StateFlow<Map<CharacterKey, String>> = _floatingScoreChanges.asStateFlow()

    // AI Sandbox Mode Toggles
    private val _isAiMode = MutableStateFlow(false)
    val isAiMode: StateFlow<Boolean> = _isAiMode.asStateFlow()

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    // History to maintain context for Gemini API
    private val storyHistory = mutableListOf<String>()

    init {
        storyHistory.add("Started Mario-style rescue adventure to find Diana's lost father Nima (World 1-1).")
    }

    fun restartGame() {
        _fkScores.value = mapOf(
            CharacterKey.GHOOL_GHOOLAK to CharacterKey.GHOOL_GHOOLAK.defaultFk,
            CharacterKey.KHOSHGELAK to CharacterKey.KHOSHGELAK.defaultFk,
            CharacterKey.GHEL_GHELEK to CharacterKey.GHEL_GHELEK.defaultFk,
            CharacterKey.DIANA to CharacterKey.DIANA.defaultFk
        )
        _hearts.value = 3
        _currentScene.value = StoryDatabase.scenes["start"]!!
        _currentDialogueIndex.value = 0
        _floatingScoreChanges.value = emptyMap()
        _aiError.value = null
        storyHistory.clear()
        storyHistory.add("Restarted rescue adventure.")
    }

    fun toggleAiMode(enabled: Boolean) {
        _isAiMode.value = enabled
        if (enabled && BuildConfig.GEMINI_API_KEY.isEmpty()) {
            _aiError.value = "Gemini API Key is missing. Please add your key to the Secrets panel in AI Studio to unlock dynamic AI adventures."
        } else {
            _aiError.value = null
        }
    }

    fun nextDialogue() {
        val currentListSize = _currentScene.value.dialogues.size
        if (_currentDialogueIndex.value < currentListSize - 1) {
            _currentDialogueIndex.value += 1
        }
    }

    fun prevDialogue() {
        if (_currentDialogueIndex.value > 0) {
            _currentDialogueIndex.value -= 1
        }
    }

    fun makeChoice(choice: GameChoice) {
        viewModelScope.launch {
            // Apply Life deduction for SELFISH choice (hitting obstacles)
            if (choice.type == ChoiceType.SELFISH) {
                _hearts.value = (_hearts.value - 1).coerceAtLeast(0)
            }

            // Apply F&K score changes
            val updatedScores = _fkScores.value.toMutableMap()
            val floatingChanges = mutableMapOf<CharacterKey, String>()

            choice.fkEffects.forEach { (charKey, diff) ->
                if (charKey != CharacterKey.NIMA) {
                    val currentVal = updatedScores[charKey] ?: charKey.defaultFk
                    updatedScores[charKey] = (currentVal + diff).coerceIn(0, 50)
                    floatingChanges[charKey] = if (diff >= 0) "+$diff" else "$diff"
                }
            }

            _fkScores.value = updatedScores
            _floatingScoreChanges.value = floatingChanges

            // Animate scores by delaying the Scene change
            delay(1200)
            _floatingScoreChanges.value = emptyMap()

            // Construct history log in English for the systemic GM
            val logText = "User chose: '${choice.textEn}'. Effects: ${choice.fkEffects.map { "${it.key.id}: ${it.value}" }}."
            storyHistory.add(logText)

            // If dead, block traversal and let the UI show Game Over
            if (_hearts.value <= 0) {
                return@launch
            }

            if (_isAiMode.value && BuildConfig.GEMINI_API_KEY.isNotEmpty()) {
                generateNextSceneWithAi(choice.textEn)
            } else {
                // Local scene traversal
                val nextScene = StoryDatabase.scenes[choice.nextSceneId]
                if (nextScene != null) {
                    _currentScene.value = nextScene
                    _currentDialogueIndex.value = 0
                } else {
                    // Fallback to start if path not found
                    _currentScene.value = StoryDatabase.scenes["start"]!!
                    _currentDialogueIndex.value = 0
                }
            }
        }
    }

    private fun generateNextSceneWithAi(choiceText: String) {
        viewModelScope.launch {
            _aiLoading.value = true
            _aiError.value = null
            try {
                val responseJson = fetchStorySceneFromGemini(choiceText)
                if (responseJson != null) {
                    val parsedScene = parseSceneFromJson(responseJson)
                    _currentScene.value = parsedScene
                    _currentDialogueIndex.value = 0
                    storyHistory.add("Generated dynamic scene '${parsedScene.locationNameEn}' with Gemini AI.")
                } else {
                    _aiError.value = "Failed to parse AI response. Falling back to offline story local engine."
                    loadFallbackLocalScene()
                }
            } catch (e: Exception) {
                Log.e("GameViewModel", "AI Error", e)
                _aiError.value = "AI Engine Error: ${e.localizedMessage}. Using offline story fallbacks."
                loadFallbackLocalScene()
            } finally {
                _aiLoading.value = false
            }
        }
    }

    private fun loadFallbackLocalScene() {
        val nextLocs = listOf("start", "stage2", "stage3", "stage4", "stage5")
        val randomLoc = nextLocs.random()
        _currentScene.value = StoryDatabase.scenes[randomLoc] ?: StoryDatabase.scenes["start"]!!
        _currentDialogueIndex.value = 0
    }

    private suspend fun fetchStorySceneFromGemini(choiceText: String): String? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty()) return@withContext null

        val systemPrompt = """
            You are the Game Master AI for an interactive 2D fantasy school adventure styled like Super Mario.
            Diana's father, Agha Nima (@Nima, 42-year-old, wise teacher), is lost inside the school basement labyrinth. 
            The team (Diana, GhoolGhoolak, Khoshgelak, GhelGhelek) is moving step-by-step (Level 1-1 to Level 1-5) to rescue him!
            Each stage has a physical danger or obstacle blocking them. Answering correctly bypasses the obstacle. 
            Choosing selfish or lazy choices hits the obstacle and hurts the team's heart count (we have a total of 3 hearts).

            Characters:
            1. @Nima: Wisest authority figure, trapped in the capsule. Speak only if found or presenting a holographic hint. Animation: [Calm] or [Teaching]
            2. @GhoolGhoolak: Blue furry bear creature. Cool, wears 8-bit black sunglasses 😎. Obsessed with food, specifically sandwiches, talks about eating often. Loyalty incarnate. Animation: [Cool] or [Hungry]
            3. @Khoshgelak: Pink furry girl creature. Highly fashionable, wears a glittering makeup backpack. Speaks with an 's' to 'sh' lisp in English ("shuch a beautiful glasshroom", "yessh"). Aesthetic queen. Animation: [Fashionable] or [Self-aware]
            4. @GhelGhelek: Navy blue furry creature. High energy, funny, loves drawing smiley faces and laughing! Wears a yellow smiley shirt. Animation: [Energetic] or [Laughing]
            5. @Diana: 11-year-old human girl. Long brown hair, smart, highly organized, wears blue knitted sweater. Father Nima is her hero! Animation: [Smart] or [Organized]

            Write the next dialogue scene in the adventure following the user's latest choice: "$choiceText".
            The conversation history is:
            ${storyHistory.joinToString("\n")}

            The game must display all dialogue lines, titles, and choices in BOTH English (En) and Persian/Farsi (Fa). 
            Provide extremely natural bilingual translations!
            For @Khoshgelak's English lines, keep the "sh" lisp, and write fluid Persian lines in Persian.
            For @GhoolGhoolak's English and Persian lines, always mention sandwiches, food, or lunch!

            You MUST write the response in EXQUISITE JSON matching this JSON SCHEMA:
            {
              "locationId": "Class01|Gard01|Sci01|Cafe01|Secret01",
              "locationNameEn": "Name of the current location in English",
              "locationNameFa": "نام محل فعلی به فارسی",
              "introTitleEn": "World Level Header (e.g. World 1-3) in English",
              "introTitleFa": "مرحله پلتفرمر به فارسی (مثلا مرحله ۱-۳)",
              "obstacleEn": "Physical active obstacle blocking the path in English",
              "obstacleFa": "مانع فعال و خطرناک مسدودکننده مسیر به فارسی",
              "dialogues": [
                {
                  "characterId": "@Nima|@GhoolGhoolak|@Khoshgelak|@GhelGhelek|@Diana",
                  "animationState": "Cool|Hungry|Fashionable|Energetic|Smart|Organized|Calm|Teaching|Laughing|Nervous",
                  "textEn": "Dialogue speech line in English",
                  "textFa": "دیالوگ به زبان فارسی"
                }
              ],
              "choices": [
                {
                  "textEn": "Choice in English starting with emoji + (Helpful) or (Creative) or (Selfish)",
                  "textFa": "گزینه به فارسی با ایموجی + (همکاری) یا (خلاقانه) یا (شخصی)",
                  "type": "HELPFUL|CREATIVE|SELFISH",
                  "fkEffects": {
                     "GhoolGhoolak": 3,
                     "Khoshgelak": -2,
                     "GhelGhelek": 4,
                     "Diana": 1
                  }
                }
              ]
            }

            Adhere to these rules:
            - Give exactly 4 to 6 dialogue lines. Include at least 3 characters talking, reacting to each other.
            - Keep choices fun and balanced. Provide exactly 3 choices: HELPFUL, CREATIVE, and SELFISH.
            - F&K Score effects should always be in range [-8, 8].
            - Ensure the JSON is completely valid, clean, and has no markdown other than the raw JSON body.
        """.trimIndent()

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "Complete the next scene please.")
                        })
                    })
                })
            })
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", systemPrompt)
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.7)
            })
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val rawBody = response.body?.string() ?: return@use null
                    val obj = JSONObject(rawBody)
                    val candidateText = obj.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                    candidateText
                } else {
                    Log.e("GameViewModel", "Gemini HTTP failed: ${response.code} ${response.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("GameViewModel", "API Exception", e)
            null
        }
    }

    private fun parseSceneFromJson(rawJson: String): Scene {
        val root = JSONObject(rawJson)
        val locId = root.optString("locationId", "Class01")
        val locNameEn = root.optString("locationNameEn", "Fantasy School Room")
        val locNameFa = root.optString("locationNameFa", "کلاس مدرسه فانتزی")
        val introTitleEn = root.optString("introTitleEn", "A Dynamic School Step")
        val introTitleFa = root.optString("introTitleFa", "گام فعال در مدرسه")

        val dialoguesList = mutableListOf<DialogueLine>()
        val dialoguesArr = root.getJSONArray("dialogues")
        for (i in 0 until dialoguesArr.length()) {
            val dItem = dialoguesArr.getJSONObject(i)
            val charTag = dItem.getString("characterId")
            val animState = dItem.optString("animationState", "Calm")
            val speechEn = dItem.optString("textEn", "")
            val speechFa = dItem.optString("textFa", "")

            val charKey = when (charTag) {
                "@Nima" -> CharacterKey.NIMA
                "@GhoolGhoolak" -> CharacterKey.GHOOL_GHOOLAK
                "@Khoshgelak" -> CharacterKey.KHOSHGELAK
                "@GhelGhelek" -> CharacterKey.GHEL_GHELEK
                else -> CharacterKey.DIANA
            }
            dialoguesList.add(DialogueLine(charKey, animState, speechEn, speechFa))
        }

        val choicesList = mutableListOf<GameChoice>()
        val choicesArr = root.getJSONArray("choices")
        for (i in 0 until choicesArr.length()) {
            val cItem = choicesArr.getJSONObject(i)
            val cTextEn = cItem.optString("textEn", "")
            val cTextFa = cItem.optString("textFa", "")
            val cTypeStr = cItem.getString("type")
            val cType = when (cTypeStr) {
                "CREATIVE" -> ChoiceType.CREATIVE
                "SELFISH" -> ChoiceType.SELFISH
                else -> ChoiceType.HELPFUL
            }

            val fkEffectsMap = mutableMapOf<CharacterKey, Int>()
            val effectsObj = cItem.optJSONObject("fkEffects")
            if (effectsObj != null) {
                val keys = effectsObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val valInt = effectsObj.getInt(k)
                    val matchedChar = when (k) {
                        "GhoolGhoolak" -> CharacterKey.GHOOL_GHOOLAK
                        "Khoshgelak" -> CharacterKey.KHOSHGELAK
                        "GhelGhelek" -> CharacterKey.GHEL_GHELEK
                        "Diana" -> CharacterKey.DIANA
                        else -> null
                    }
                    if (matchedChar != null) {
                        fkEffectsMap[matchedChar] = valInt
                    }
                }
            }

            choicesList.add(
                GameChoice(
                    id = "ai_choice_$i",
                    textEn = cTextEn,
                    textFa = cTextFa,
                    type = cType,
                    fkEffects = fkEffectsMap,
                    nextSceneId = "start"
                )
            )
        }

        val obstacleEn = root.optString("obstacleEn", "")
        val obstacleFa = root.optString("obstacleFa", "")

        return Scene(
            id = "ai_generated_scene",
            locationId = locId,
            locationNameEn = locNameEn,
            locationNameFa = locNameFa,
            introTitleEn = introTitleEn,
            introTitleFa = introTitleFa,
            dialogues = dialoguesList,
            choices = choicesList,
            obstacleEn = obstacleEn,
            obstacleFa = obstacleFa
        )
    }
}
