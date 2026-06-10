package com.example

import androidx.compose.ui.graphics.Color

enum class GameLanguage { EN, FA }

enum class CharacterKey(
    val id: String,
    val displayNameEn: String,
    val displayNameFa: String,
    val avatarResName: String,
    val themeColor: Color,
    val defaultFk: Int
) {
    NIMA("@Nima", "Agha Nima", "آقا نیما", "image_0", Color(0xFF4CAF50), 100),
    GHOOL_GHOOLAK("@GhoolGhoolak", "GhoolGhoolak", "غول غولک", "ghoolghoolak_model_2d", Color(0xFF2196F3), 10),
    KHOSHGELAK("@Khoshgelak", "Khoshgelak", "خوشگلک", "khoshgelak_model_2d", Color(0xFFE91E63), 15),
    GHEL_GHELEK("@GhelGhelek", "GhelGhelek", "قلقلک", "ghelghelek_model_2d", Color(0xFF3F51B5), 8),
    DIANA("@Diana", "Diana", "دیانا", "diana_model_2d", Color(0xFFFF9800), 20);

    fun displayName(lang: GameLanguage): String = if (lang == GameLanguage.FA) displayNameFa else displayNameEn
}

data class DialogueLine(
    val characterKey: CharacterKey,
    val animationState: String,
    val textEn: String,
    val textFa: String
) {
    fun text(lang: GameLanguage): String = if (lang == GameLanguage.FA) textFa else textEn
}

data class GameChoice(
    val id: String,
    val textEn: String,
    val textFa: String,
    val type: ChoiceType,
    val fkEffects: Map<CharacterKey, Int>,
    val nextSceneId: String
) {
    fun text(lang: GameLanguage): String = if (lang == GameLanguage.FA) textFa else textEn
}

enum class ChoiceType {
    HELPFUL, CREATIVE, SELFISH
}

data class Scene(
    val id: String,
    val locationId: String,
    val locationNameEn: String,
    val locationNameFa: String,
    val introTitleEn: String,
    val introTitleFa: String,
    val dialogues: List<DialogueLine>,
    val choices: List<GameChoice>,
    val obstacleEn: String = "",
    val obstacleFa: String = ""
) {
    fun locationName(lang: GameLanguage): String = if (lang == GameLanguage.FA) locationNameFa else locationNameEn
    fun introTitle(lang: GameLanguage): String = if (lang == GameLanguage.FA) introTitleFa else introTitleEn
    fun obstacle(lang: GameLanguage): String = if (lang == GameLanguage.FA) obstacleFa else obstacleEn
}

object StoryDatabase {
    val scenes: Map<String, Scene> = mapOf(
        "start" to Scene(
            id = "start",
            locationId = "Class01",
            locationNameEn = "Saffron Hallway (World 1-1)",
            locationNameFa = "راهروی زعفرانی (مرحله ۱-۱)",
            introTitleEn = "Chapter 1: The Disappearance of Nima",
            introTitleFa = "بخش ۱: گم شدن ناگهانی آقا نیما",
            obstacleEn = "Closed Iron Dungeon Gate blocking the basement route.",
            obstacleFa = "دروازه آهنی زنگ‌زده بوفه و زیرزمین مخفی مدرسه.",
            dialogues = listOf(
                DialogueLine(
                    CharacterKey.DIANA, "Organized",
                    "Agha Nima is gone! His coffee is still warm, but his research notes are scattered. We must follow him step-by-step like a platformer game!",
                    "آقا نیما غیبش زده! قهوه‌اش هنوز گرم است، اما یادداشت‌هایش پخش شده‌اند. ما باید مرحله به مرحله مثل بازی قارچ‌خور دنبالش بگردیم!"
                ),
                DialogueLine(
                    CharacterKey.GHOOL_GHOOLAK, "Hungry",
                    "Could he have gone to get sandwiches? Oh, please let there be no spiky traps on the route down to the basement!",
                    "نکند رفته برای ما ساندویچ لقمه بگیرد؟ اوه، امیدوارم در مسیر زیرزمین هیچ مانع و تله‌ی خارکوب تیز وجود نداشته باشد!"
                ),
                DialogueLine(
                    CharacterKey.KHOSHGELAK, "Fashionable",
                    "Don't worry Diana, we will find your fabulouhs father. But look, this heavy locked dungeon gate blocks our entire platform!",
                    "دیانا نگران نباش، ما پدر محشر تو را پیدا می‌کنیم. اما نگاه کن، این دروازه سنگین قفل شده راه کل پلتفرم ما را بسته!"
                ),
                DialogueLine(
                    CharacterKey.GHEL_GHELEK, "Energetic",
                    "We can do this! Let's solve the gears riddle and jump onwards to Level 1-2! Wheee!",
                    "ما می‌توانیم! بیایید معمای چرخ‌دنده‌ها را حل کنیم و به سمت مرحله ۱-۲ بپریم! وییی!"
                )
            ),
            choices = listOf(
                GameChoice(
                    id = "gate_helpful",
                    textEn = "🌻 (Helpful) Teamwork: Use Diana's physics checklist to safely balance and slide open the gear lock.",
                    textFa = "🌻 (همکاری) کار تیمی: استفاده از دفترچه دیانا برای توازن چرخ‌دنده‌ها و باز کردن ایمن قفل دروازه.",
                    type = ChoiceType.HELPFUL,
                    fkEffects = mapOf(
                        CharacterKey.DIANA to 6,
                        CharacterKey.GHEL_GHELEK to 3,
                        CharacterKey.KHOSHGELAK to 4,
                        CharacterKey.GHOOL_GHOOLAK to 3
                    ),
                    nextSceneId = "stage2"
                ),
                GameChoice(
                    id = "gate_creative",
                    textEn = "🎨 (Creative) Speed Smash: Let GhoolGhoolak eat a power-sandwich and bash the rusty hinge panel!",
                    textFa = "🎨 (خلاقانه) ضربه پرسرعت: غول‌غولک یک ساندویچ قدرتی بخورد و به لولای زنگ‌زده بکوبد تا باز شود!",
                    type = ChoiceType.CREATIVE,
                    fkEffects = mapOf(
                        CharacterKey.GHOOL_GHOOLAK to 8,
                        CharacterKey.GHEL_GHELEK to 6,
                        CharacterKey.KHOSHGELAK to 3,
                        CharacterKey.DIANA to 2
                    ),
                    nextSceneId = "stage2"
                ),
                GameChoice(
                    id = "gate_selfish",
                    textEn = "💤 (Selfish) Play Solo: Try to run through alone, ignoring the gears. (Ouch! Hits the gate directly!)",
                    textFa = "💤 (شخصی) تک‌روی: بدون توجه به بقیه بدوید و تلاش کنید از شکاف رد شوید. (آخ! محکم به دروازه می‌خورید!)",
                    type = ChoiceType.SELFISH,
                    fkEffects = mapOf(
                        CharacterKey.DIANA to -4,
                        CharacterKey.GHOOL_GHOOLAK to -3,
                        CharacterKey.GHEL_GHELEK to -4,
                        CharacterKey.KHOSHGELAK to -3
                    ),
                    nextSceneId = "start"
                )
            )
        ),

        "stage2" to Scene(
            id = "stage2",
            locationId = "Gard01",
            locationNameEn = "Toxic Garden Path (World 1-2)",
            locationNameFa = "مسیر باغچه سمی (مرحله ۱-۲)",
            introTitleEn = "Chapter 2: The Thorny Jungle",
            introTitleFa = "بخش ۲: جنگل درختچه‌های خاردار پیرانها",
            obstacleEn = "Giant Mutated Piranha Vines swinging and blocking the courtyard bridge.",
            obstacleFa = "مانع: بوته‌های گزنده گیاهی پیرانها که در پل حیاط می‌جنبند و مانع حرکت می‌شوند.",
            dialogues = listOf(
                DialogueLine(
                    CharacterKey.DIANA, "Smart",
                    "We are in the courtyard, but the vegetation has grown into giant stinging Piranha Vines! They look exactly like Mario obstacles!",
                    "ما به حیاط رسیده‌ایم، اما گیاهان به بوته‌های گزنده پیرانها تبدیل شده‌اند! دقیقاً شبیه به موانع متحرک بازی ماریو هستند!"
                ),
                DialogueLine(
                    CharacterKey.GHEL_GHELEK, "Laughing",
                    "Wow! They bite! Ghel Ghel! If we jump at the right timing, can we vault over them onto that brick platform?",
                    "وای! گاز می‌گیرند! قلقلک! اگر سر تایم مناسب بپریم، می‌توانیم از رویشان روی آن بلوک آجری جهش کنیم؟"
                ),
                DialogueLine(
                    CharacterKey.KHOSHGELAK, "Fashionable",
                    "Oh dear, those thorny green monsters are horribly out of shyle! They will totally scratch my glitter backpack!",
                    "وای نه، این هیولاهای سبز خاردار کاملاً بی‌استایل هستند! کوله‌پشتی اکلیلی مرا کاملاً نخ‌کش می‌کنند!"
                ),
                DialogueLine(
                    CharacterKey.GHOOL_GHOOLAK, "Cool",
                    "Maybe they are hungry for energy food? I have some spicy pickles... Let's figure out how to pass safely!",
                    "نکند گرسنه‌اند؟ من چند ترشی تند دارم... بیایید راهی پیدا کنیم تا بدون آسیب دیدن رد شویم!"
                )
            ),
            choices = listOf(
                GameChoice(
                    id = "garden_helpful",
                    textEn = "🌻 (Helpful) Chemical Spray: Diana guides the group to spray organic vinegar mist to make the plants shrink back.",
                    textFa = "🌻 (همکاری) اسپری بیولوژیک: دیانا گروه را راهنمایی می‌کند تا اسپری سرکه ارگانیک بزنند تا گیاهان جمع شوند.",
                    type = ChoiceType.HELPFUL,
                    fkEffects = mapOf(
                        CharacterKey.DIANA to 7,
                        CharacterKey.KHOSHGELAK to 5,
                        CharacterKey.GHOOL_GHOOLAK to 3,
                        CharacterKey.GHEL_GHELEK to 2
                    ),
                    nextSceneId = "stage3"
                ),
                GameChoice(
                    id = "garden_creative",
                    textEn = "🎨 (Creative) Paint Distraction: Draw funny smiley faces on the pots to make the vines dizzy with laugh!",
                    textFa = "🎨 (خلاقانه) نقاشی خنده‌دار: قلقلک روی سفال‌ها نقاشی صورتک بکشد تا حواس پیچک‌ها پرت شود!",
                    type = ChoiceType.CREATIVE,
                    fkEffects = mapOf(
                        CharacterKey.GHEL_GHELEK to 9,
                        CharacterKey.KHOSHGELAK to 6,
                        CharacterKey.GHOOL_GHOOLAK to 5,
                        CharacterKey.DIANA to 3
                    ),
                    nextSceneId = "stage3"
                ),
                GameChoice(
                    id = "garden_selfish",
                    textEn = "💤 (Selfish) Rush Alone: Try to jump alone, pushing others back. (Ouch! Bitten by the Piranha vine!)",
                    textFa = "💤 (شخصی) تک‌روی پرخطر: به تنهایی بپرید و دیگران را عقب بکشید. (آی! بوته گواوا گازتان می‌گیرد!)",
                    type = ChoiceType.SELFISH,
                    fkEffects = mapOf(
                        CharacterKey.DIANA to -5,
                        CharacterKey.KHOSHGELAK to -4,
                        CharacterKey.GHEL_GHELEK to -5,
                        CharacterKey.GHOOL_GHOOLAK to -3
                    ),
                    nextSceneId = "stage2"
                )
            )
        ),

        "stage3" to Scene(
            id = "stage3",
            locationId = "Sci01",
            locationNameEn = "Chem-Lava Laboratory (World 1-3)",
            locationNameFa = "آزمایشگاه گدازه شیمی (مرحله ۱-۳)",
            introTitleEn = "Chapter 3: The Glowing Acid Flow",
            introTitleFa = "بخش ۳: جریان گدازه‌های شیمیایی درخشان",
            obstacleEn = "Bubbling Hot Neon-Acid Lava flowing across the experiment floor.",
            obstacleFa = "مانع: استخر بزرگی از اسید شیمیایی درخشان و سوزان مثل مواد مذاب آتشفشانی ماریو.",
            dialogues = listOf(
                DialogueLine(
                    CharacterKey.DIANA, "Smart",
                    "Oh, look at the floor! One of Nima's automatic valves burst, forming a glowing lava-pool of boiling acid!",
                    "سر را به زیر بیندازید! یکی از سوپاپ‌های خودکار نیما ترکیده و حوضچه‌ای از اسید جوشان گدازه‌ای ساخته است!"
                ),
                DialogueLine(
                    CharacterKey.GHOOL_GHOOLAK, "Hungry",
                    "It smells like carbonated soda, but GhelGhelek threw a plastic test cup and it dissolved instantly! We can't walk on that!",
                    "بویش شبیه به نوشابه گازدار است، اما قلقلک یک فنجان پلاستیکی پرتاب کرد و فوراً ذوب شد! نمی‌توانیم از روی آن رد شویم!"
                ),
                DialogueLine(
                    CharacterKey.KHOSHGELAK, "Fashionable",
                    "Oh my looksh! This neon glow is shplendid on my makeup kit, but the acid fumes are super toxic! We need a bridge!",
                    "وای! این تابش نئونی بر جعبه آرایشی من می‌درخشد، اما گاز اسیدی بسیار سمی است! ما به پل نیاز داریم!"
                ),
                DialogueLine(
                    CharacterKey.GHEL_GHELEK, "Energetic",
                    "I want to jump on the floating tables like moving platforms! Who's ready to fly? Ghel Ghel!",
                    "من می‌خواهم مثل پلتفرم‌های متحرک روی میزهای شناور بپرم! چه کسی آماده پرواز است؟ قلقلک!"
                )
            ),
            choices = listOf(
                GameChoice(
                    id = "science_helpful",
                    textEn = "🌻 (Helpful) Acid Neutralizer: Co-op with Diana to pour bicarbonate salts to solidify the floor.",
                    textFa = "🌻 (همکاری) خنثی‌سازی شیمیایی: کمک به دیانا برای ریختن نمک‌های کربنات روی اسید تا جامد و بی‌خطر شود.",
                    type = ChoiceType.HELPFUL,
                    fkEffects = mapOf(
                        CharacterKey.DIANA to 8,
                        CharacterKey.KHOSHGELAK to 4,
                        CharacterKey.GHOOL_GHOOLAK to 3,
                        CharacterKey.GHEL_GHELEK to -1
                    ),
                    nextSceneId = "stage4"
                ),
                GameChoice(
                    id = "science_creative",
                    textEn = "🎨 (Creative) Foam Balloons: Release high-pressure foam storage tanks to create a solid bouncy pathway!",
                    textFa = "🎨 (خلاقانه) بالون‌های اسفنجی: مخازن کف پرفشار را باز کنید تا مسیر ایمن و نرم برای جهش ایجاد شود!",
                    type = ChoiceType.CREATIVE,
                    fkEffects = mapOf(
                        CharacterKey.GHEL_GHELEK to 9,
                        CharacterKey.KHOSHGELAK to 7,
                        CharacterKey.GHOOL_GHOOLAK to 6,
                        CharacterKey.DIANA to 4
                    ),
                    nextSceneId = "stage4"
                ),
                GameChoice(
                    id = "science_selfish",
                    textEn = "💤 (Selfish) Blind Jump: Push a heavy table into the pool, splashing hot acid everywhere. (Ouch! Hit by acid splash!)",
                    textFa = "💤 (شخصی) پرش بدون فکر: یک میز گران‌بها را به داخل هل دهید تا به تنهایی بپرید. (وای! قطرات اسید دستتان را می‌سوزاند!)",
                    type = ChoiceType.SELFISH,
                    fkEffects = mapOf(
                        CharacterKey.DIANA to -6,
                        CharacterKey.GHEL_GHELEK to -4,
                        CharacterKey.KHOSHGELAK to -4,
                        CharacterKey.GHOOL_GHOOLAK to -3
                    ),
                    nextSceneId = "stage3"
                )
            )
        ),

        "stage4" to Scene(
            id = "stage4",
            locationId = "Cafe01",
            locationNameEn = "The Buffet Castle (World 1-4)",
            locationNameFa = "دژ سالن غذاخوری (مرحله ۱-۴)",
            introTitleEn = "Chapter 4: The Meatball Barrage",
            introTitleFa = "بخش ۴: شلیک کوفته‌های سنگی کلان",
            obstacleEn = "Automated Buffet Drone throwing rolling giant meatballs down the stair steps.",
            obstacleFa = "مانع: پهپاد خودکار بوفه خراب شده که کوفته‌های سنگی داغ غول‌پیکر را مثل بمب‌های ماریو شلیک می‌کند.",
            dialogues = listOf(
                DialogueLine(
                    CharacterKey.DIANA, "Smart",
                    "We have reached the basement portal stairs beneath the Cafeteria. But the automatic kitchen defense system is offline and throws massive hot meatballs!",
                    "ما به پله‌های زیرزمین در پشت بوفه رسیده‌ایم. اما سیستم خودکار آشپزخانه قاطی کرده و کوفته‌های داغ بزرگی پرتاب می‌کند!"
                ),
                DialogueLine(
                    CharacterKey.GHOOL_GHOOLAK, "Hungry",
                    "OH MY GOSH! Rolling meatballs! They smell delicious, but they look like burning Bowser fireballs! My stomach is excited but terrified!",
                    "وای خدای من! کوفته‌های غلتان! بویشان عالی است، اما شبیه گلوله‌های جنگی ماریو هستند! هم گرسنه‌ام هم وحشت‌زده!"
                ),
                DialogueLine(
                    CharacterKey.KHOSHGELAK, "Fashionable",
                    "Ahhh! Red greasy sauce is going to rain on our glamorous style! We must disable that food machine!",
                    "آاااخ! باران سس سرخ‌رنگ دارد روی استایل بی‌نظیر ما می‌بارد! ما باید فوراً آن ماشین آشپزی را از کار بیندازیم!"
                ),
                DialogueLine(
                    CharacterKey.GHEL_GHELEK, "Laughing",
                    "This is exactly like Bowser's Castle! Ghel Ghel! Let's shut the main steam lever or dodge with perfect jumps!",
                    "دقیقاً شبیه قلعه پایانی قارچ‌خور است! قلقلک! بیایید شیر بخار اصلی را ببندیم یا با پرش‌های فوق‌العاده جاخالی بدهیم!"
                )
            ),
            choices = listOf(
                GameChoice(
                    id = "food_helpful",
                    textEn = "🌻 (Helpful) System Shutoff: Let Diana navigate the control panel to bypass the security firewall safely.",
                    textFa = "🌻 (همکاری) قطع سیستم: دیانا را همراهی کنید تا پنل امنیتی را هک کند و اتصالات پهپاد را قطع نماید.",
                    type = ChoiceType.HELPFUL,
                    fkEffects = mapOf(
                        CharacterKey.DIANA to 8,
                        CharacterKey.KHOSHGELAK to 5,
                        CharacterKey.GHOOL_GHOOLAK to 3,
                        CharacterKey.GHEL_GHELEK to 2
                    ),
                    nextSceneId = "stage5"
                ),
                GameChoice(
                    id = "food_creative",
                    textEn = "🎨 (Creative) Food Jam: Feed GhoolGhoolak's supreme sandwich suit into the nozzle to block the cannon!",
                    textFa = "🎨 (خلاقانه) مسدود کردن تفنگ نوزل: ساندویچ بزرگ غول‌غولک را داخل لوله پرتاپ شلیک فرو کنید تا کوفته‌ها گیر کنند!",
                    type = ChoiceType.CREATIVE,
                    fkEffects = mapOf(
                        CharacterKey.GHOOL_GHOOLAK to 10,
                        CharacterKey.GHEL_GHELEK to 8,
                        CharacterKey.KHOSHGELAK to 5,
                        CharacterKey.DIANA to 3
                    ),
                    nextSceneId = "stage5"
                ),
                GameChoice(
                    id = "food_selfish",
                    textEn = "💤 (Selfish) Shield Behind: Hide behind GhelGhelek while meatballs rumble downward. (Ouch! Trapped by sauce!)",
                    textFa = "💤 (شخصی) سپر کردن دوست: پشت قلقلک پنهان شوید تا بمب کوفته به او بخورد. (وای! کوفته منفجر شده و با سس داغ مخدوش می‌شوید!)",
                    type = ChoiceType.SELFISH,
                    fkEffects = mapOf(
                        CharacterKey.GHOOL_GHOOLAK to -5,
                        CharacterKey.DIANA to -4,
                        CharacterKey.KHOSHGELAK to -4,
                        CharacterKey.GHEL_GHELEK to -6
                    ),
                    nextSceneId = "stage4"
                )
            )
        ),

        "stage5" to Scene(
            id = "stage5",
            locationId = "Secret01",
            locationNameEn = "Nima's Labyrinth Core (World 1-5)",
            locationNameFa = "هسته آزمایشگاهی نیما (مرحله ۱-۵)",
            introTitleEn = "Chapter 5: The Final Laser Locks",
            introTitleFa = "بخش ۵: سپرهای نوری و معمای نهایی",
            obstacleEn = "Rotating Laser Barrier and Biometric Decoder Lock.",
            obstacleFa = "مانع: سپرهای نوری چرخنده و قفل حسگر اثر انگشت دیحیتالی.",
            dialogues = listOf(
                DialogueLine(
                    CharacterKey.DIANA, "Smart",
                    "We made it to the secret underground chamber! Look, Agha Nima is trapped inside the capsule behind the glowing laser barrier!",
                    "ما بالاخره به محوطه سری زیرزمین رسیدیم! نگاه کنید، آقا نیما پشت سپر لیزری چرخان داخل کپسول گیر افتاده است!"
                ),
                DialogueLine(
                    CharacterKey.GHOOL_GHOOLAK, "Cool",
                    "Look, Agha Nima is waving at us through the glass! He is safe! But we must decode this numeric cipher block to open it!",
                    "ببینید، آقا نیما از پشت شیشه کپسول برایمان دست تکان می‌دهد! او زنده است! اما باید رمز دیجیتال را حل کنیم تا کپسول باز شود!"
                ),
                DialogueLine(
                    CharacterKey.KHOSHGELAK, "Self-aware",
                    "This ish the final bowsh fight! Agha Nima's blueprint says the biometric cipher is unlocked by the sum of our Friendship and Kindness!",
                    "این غول آخر بازی ماریوی ماست! دفترچه آقا نیما می‌گوید رمز بیومتریک با فاکتورهای دوستی و ارزش همکاری ما باز خواهد شد!"
                ),
                DialogueLine(
                    CharacterKey.GHEL_GHELEK, "Energetic",
                    "Oh yeah! Let's input our total unified score and save Agha Nima! Let's make an energetic final push! 🌟",
                    "آره! بیایید میانگین همبستگی دوستی گروه نجات را وارد کنید تا نیما آزاد شود! یک تلاش پرانرژی نهایی! 🌟"
                )
            ),
            choices = listOf(
                GameChoice(
                    id = "final_helpful",
                    textEn = "🌻 (Helpful) Combined Code: Enter the exact combined Friendship ledger data to deactivate the laser cleanly.",
                    textFa = "🌻 (همکاری) ورود رمز همبستگی: وارد کردن کدهای همبستگی و دوستی تیمی برای غیرفعال کردن بی خطر سپر لیزری.",
                    type = ChoiceType.HELPFUL,
                    fkEffects = mapOf(
                        CharacterKey.DIANA to 10,
                        CharacterKey.GHOOL_GHOOLAK to 10,
                        CharacterKey.KHOSHGELAK to 10,
                        CharacterKey.GHEL_GHELEK to 10
                    ),
                    nextSceneId = "victory"
                ),
                GameChoice(
                    id = "final_creative",
                    textEn = "🎨 (Creative) Overload: Connect GhelGhelek's yellow smiley batterypack to safely trigger a power drain bypass!",
                    textFa = "🎨 (خلاقانه) کنترل ولتاژ: باتری خندان زرد قلقلک را وصل کنید تا منبع تغذیه سپر لیزری تخلیه و باز شود!",
                    type = ChoiceType.CREATIVE,
                    fkEffects = mapOf(
                        CharacterKey.GHEL_GHELEK to 12,
                        CharacterKey.GHOOL_GHOOLAK to 8,
                        CharacterKey.KHOSHGELAK to 8,
                        CharacterKey.DIANA to 5
                    ),
                    nextSceneId = "victory"
                ),
                GameChoice(
                    id = "final_selfish",
                    textEn = "💤 (Selfish) Smash Screen: Smash the keyboard screen with a heavy rod. (Ouch! High-voltage feedback shock!)",
                    textFa = "💤 (شخصی) کوبیدن به مانیتور: با یک میله فلزی به مانیتور بکوبید. (شوربختانه دچار شوک الکتریکی قوی می‌شوید!)",
                    type = ChoiceType.SELFISH,
                    fkEffects = mapOf(
                        CharacterKey.DIANA to -6,
                        CharacterKey.GHEL_GHELEK to -5,
                        CharacterKey.KHOSHGELAK to -5,
                        CharacterKey.GHOOL_GHOOLAK to -5
                    ),
                    nextSceneId = "stage5"
                )
            )
        ),

        "victory" to Scene(
            id = "victory",
            locationId = "victory",
            locationNameEn = "Safe Haven (World 1-Clear)",
            locationNameFa = "نجات کامل آقا نیما و پیروزی (پایان فرار)",
            introTitleEn = "MISSION COMPLETE! Nima is Rescued!",
            introTitleFa = "ماموریت کامل شد! آقا نیما نجات یافت!",
            obstacleEn = "",
            obstacleFa = "",
            dialogues = listOf(
                DialogueLine(
                    CharacterKey.NIMA, "Calm",
                    "Diana! Class! You actually bypassed all school basement platform levels and deactivated the locked safety capsule! I am so proud of you!",
                    "دیانا! بچه‌ها! شما واقعاً تمام مراحل پلتفرمر زیرزمین مدرسه را با موفقیت پشت سر گذاشتید و مرا نجات دادید! به وجودتان افتخار می‌کنم!"
                ),
                DialogueLine(
                    CharacterKey.DIANA, "Smart",
                    "Daddy! Our rescue guild operated step-by-step just like Super Mario, keeping our Friendship and Kindness ledger fully charged!",
                    "بابا! گروه نجات ما مرحله به مرحله مثل بازی سوپر ماریو پیش رفت و نگذاشتیم امتیاز دفترچه دوستی‌مان خالی شود!"
                ),
                DialogueLine(
                    CharacterKey.GHOOL_GHOOLAK, "Cool",
                    "Yes, Agha Nima! And I saved half of my power burger to celebrate our ultimate family union! Nom nom! 🍔",
                    "بله آقا نیما! و من نصف همبرگر قدرتی‌ام را نگه داشته‌ام تا این اتحاد شاد خانوادگی را جشن بگیریم! یوم یوم! 🍔"
                ),
                DialogueLine(
                    CharacterKey.KHOSHGELAK, "Fashionable",
                    "And my backpack survived without a shingle shcratch! Truly a shplendid and beautiful happy ending!",
                    "و کوله‌پشتی اکلیلی صورتی من هم بدون کوچکترین خط و خشی سالم ماند! واقعاً یک پایان خوش باشکوه و زیبا!"
                ),
                DialogueLine(
                    CharacterKey.GHEL_GHELEK, "Laughing",
                    "HAHAHA! The great Mario Rescue Guild wins! We solved the obstacles and saved our teacher father Nima! Let's party!",
                    "هاهاها! صنف نجات ماریو برنده شد! ما از موانع گذشتیم و معلم و پدر باهوشمان نیما را نجات دادیم! وقت پایکوبی است!"
                )
            ),
            choices = listOf(
                GameChoice(
                    id = "restart_game",
                    textEn = "🏁 Start a New Super Rescue Quest!",
                    textFa = "🏁 شروع ماموریت نجات ماریویی جدید از ابتدا!",
                    type = ChoiceType.HELPFUL,
                    fkEffects = emptyMap(),
                    nextSceneId = "start"
                )
            )
        )
    )
}
