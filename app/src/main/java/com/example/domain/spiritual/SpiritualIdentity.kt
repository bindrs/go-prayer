package com.example.domain.spiritual

data class LoveAyat(
    val id: String,
    val surahName: String,
    val surahNumber: Int,
    val ayahNumber: String,
    val arabicText: String,
    val urduTranslation: String,
    val englishTranslation: String,
    val spiritualTheme: String,
    val heartPurificationNote: String
)

object SpiritualVerses {
    const val IDENTITY_AFFIRMATION_URDU = "میں اللہ کا بندہ ہوں، میری زندگی، نماز اور ہر سانس اللہ کے لیے ہے۔"
    const val IDENTITY_AFFIRMATION_ENGLISH = "I am a humble servant of Allah. My life, my prayer, and my journey are for Allah."

    val identityOptions = listOf(
        "بندۂ خدا (Servant of Allah)",
        "عبد اللہ (Abdullah - Humble Servant)",
        "طالبِ مغفرت (Seeker of Forgiveness)",
        "ذاکر و شاکر (Grateful & Mindful Servant)",
        "صالح اور پاک دل (Striving for Pure Heart)"
    )

    val loveAndPurificationAyats: List<LoveAyat> = listOf(
        LoveAyat(
            id = "az_zumar_53",
            surahName = "Az-Zumar",
            surahNumber = 39,
            ayahNumber = "53",
            arabicText = "قُلْ يَا عِبَادِيَ الَّذِينَ أَسْرَفُوا عَلَىٰ أَنفُسِهِمْ لَا تَقْنَطُوا مِن رَّحْمَةِ اللَّهِ ۚ إِنَّ اللَّهَ يَغْفِرُ الذُّنُوبَ جَمِيعًا ۚ إِنَّهُ هُوَ الْغَفُورُ الرَّحِيمُ",
            urduTranslation = "فرمادیجئے: اے میرے وہ بندو جنہوں نے اپنی جانوں پر زیادتی کی ہے! تم اللہ کی رحمت سے ناامید نہ ہو، بیشک اللہ تمام گناہوں کو معاف فرما دیتا ہے، وہ یقیناً بڑا بخشنے والا نہایت رحم فرمانے والا ہے۔",
            englishTranslation = "Say: 'O My servants who have transgressed against themselves! Despair not of the mercy of Allah. Indeed, Allah forgives all sins. Indeed, He is the Forgiving, the Merciful.'",
            spiritualTheme = "بے پایاں رحمت و مغفرت • Infinite Mercy & Hope",
            heartPurificationNote = "دل کو برائی، مایوسی اور خود سرائی سے نکال کر اللہ کی محبت اور رحمت کی پناہ میں لائیں۔ اللہ ہر غلطی کو معاف فرما کر دل کو نور عطا کرتا ہے۔"
        ),
        LoveAyat(
            id = "ar_rad_28",
            surahName = "Ar-Ra'd",
            surahNumber = 13,
            ayahNumber = "28",
            arabicText = "الَّذِينَ آمَنُوا وَتَطْمَئِنُّ قُلُوبُهُم بِذِكْرِ اللَّهِ ۗ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
            urduTranslation = "جو لوگ ایمان لائے اور ان کے دل اللہ کے ذکر سے اطمینان پاتے ہیں، سن لو! اللہ کے ذکر ہی سے دلوں کو سکون و اطمینان ملتا ہے۔",
            englishTranslation = "Those who have believed and whose hearts are assured by the remembrance of Allah. Unquestionably, by the remembrance of Allah hearts are assured.",
            spiritualTheme = "سکونِ قلب و راحت • Inner Peace & Serenity",
            heartPurificationNote = "جب بھی دل میں غصہ، شیطانی وسوسے یا برے ارادے جنم لیں، ذکرِ الٰہی سے دل کو ٹھنڈک اور طہارت عطا کریں۔"
        ),
        LoveAyat(
            id = "fussilat_34",
            surahName = "Fussilat",
            surahNumber = 41,
            ayahNumber = "34",
            arabicText = "ادْفَعْ بِالَّتِي هِيَ أَحْسَنُ فَإِذَا الَّذِي بَيْنَكَ وَبَيْنَهُ عَدَاوَةٌ كَأَنَّهُ وَلِيٌّ حَمِيمٌ",
            urduTranslation = "اور برائی کا بدلہ اس طریقے سے دو جو سب سے اچھا اور خوبصورت ہو، تو وہی شخص جس کے اور تمہارے درمیان دشمنی تھی، ایسا ہو جائے گا جیسے گہرا مخلص دوست۔",
            englishTranslation = "Repel evil with that which is better; then he between whom and you was enmity will become as though he were a devoted friend.",
            spiritualTheme = "برائی کا جواب اچھائی سے • Repelling Evil with Goodness",
            heartPurificationNote = "دل سے نفرت، کینہ اور بدلے کی آگ کو معافی، احسان اور مسکراہٹ سے بجھا کر دل کو پاکیزہ بنائیں۔"
        ),
        LoveAyat(
            id = "hud_114",
            surahName = "Hud",
            surahNumber = 11,
            ayahNumber = "114",
            arabicText = "إِنَّ الْحَسَنَاتِ يُذْهِبْنَ السَّيِّئَاتِ ۚ ذَٰلِكَ ذِكْرَىٰ لِلذَّاكِرِينَ",
            urduTranslation = "بیشک نیکیاں برائیوں کو مٹا دیتی ہیں، یہ نصیحت قبول کرنے والوں کے لیے ایک نصیحت ہے۔",
            englishTranslation = "Indeed, good deeds do away with misdeeds. That is a reminder for those who remember.",
            spiritualTheme = "نیکیاں برائیوں کو مٹا دیتی ہیں • Good Deeds Wash Away Evil",
            heartPurificationNote = "اگر کبھی دل میں خطا یا بگاڑ آ جائے تو فوراً سجدہ، استغفار اور نیکی کر کے دل کا میل صاف کریں۔"
        ),
        LoveAyat(
            id = "al_baqarah_186",
            surahName = "Al-Baqarah",
            surahNumber = 2,
            ayahNumber = "186",
            arabicText = "وَإِذَا سَأَلَكَ عِبَادِي عَنِّي فَإِنِّي قَرِيبٌ ۖ أُجِيبُ دَعْوَةَ الدَّاعِ إِذَا دَعَانِ",
            urduTranslation = "اور جب میرے بندے آپ سے میرے متعلق پوچھیں تو بیشک میں بہت قریب ہوں، میں پکارنے والے کی دعا قبول کرتا ہوں جب وہ مجھے پکارتا ہے۔",
            englishTranslation = "And when My servants ask you concerning Me, indeed I am near. I respond to the invocation of the supplicant when he calls upon Me.",
            spiritualTheme = "قربِ الٰہی و محبت • Divine Nearness & Acceptance",
            heartPurificationNote = "اللہ ہر لمحہ آپ کی شہ رگ سے زیادہ قریب ہے؛ سچی توبہ اور محبت کے ساتھ اپنے رب سے دل کی بات کہیں۔"
        ),
        LoveAyat(
            id = "ash_shams_9",
            surahName = "Ash-Shams",
            surahNumber = 91,
            ayahNumber = "9-10",
            arabicText = "قَدْ أَفْلَحَ مَن زَكَّاهَا ۝ وَقَدْ خَابَ مَن دَسَّاهَا",
            urduTranslation = "یقیناً وہ شخص کامیاب ہو گیا جس نے اپنے نفس کو پاک کر لیا، اور وہ نامراد ہوا جس نے اسے گناہوں میں دبا دیا۔",
            englishTranslation = "He has certainly succeeded who purifies it (the soul), and he has failed who corrupts it.",
            spiritualTheme = "تزکیۂ نفس و کامیابی • Purification of the Soul",
            heartPurificationNote = "اللہ کا سچا بندہ بننے کا راز یہ ہے کہ ہر روز اپنے اندر کے برے ارادوں کو اچھے ارادوں سے بدل دیا جائے۔"
        )
    )

    fun getAyatById(id: String): LoveAyat {
        return loveAndPurificationAyats.find { it.id == id } ?: loveAndPurificationAyats.first()
    }
}
