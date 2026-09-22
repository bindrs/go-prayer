package com.example.data.local

import com.example.data.local.entities.AdhanTrack
import com.example.data.local.entities.Ayat
import com.example.data.local.entities.LocationProfile
import com.example.data.local.entities.PrayerConfiguration
import com.example.data.local.entities.SalahRecord
import com.example.data.local.entities.UserSettings
import java.util.Calendar
import java.util.Locale

object SeedData {
    val defaultSettings = UserSettings(
        id = 1,
        language = "en",
        theme = "system",
        calculationMethod = "MWL",
        madhhab = "STANDARD",
        highLatitudeRule = "MIDDLE_OF_NIGHT",
        timeFormat = "12H",
        quietModeEnabled = true,
        quietModeDurationMinutes = 20,
        keepScreenAwake = true,
        isOnboardingCompleted = false,
        arabicFontPreference = "Amiri / Uthmanic",
        safetyExceptionsAllowed = true,
        totalDisableOnPrayer = true,
        vpnFahishaAlertEnabled = true,
        userName = "",
        spiritualIdentity = "بندۂ خدا (Servant of Allah)",
        favoriteLoveAyahId = "az_zumar_53"
    )

    val defaultLocation = LocationProfile(
        id = 1,
        city = "Makkah",
        country = "Saudi Arabia",
        latitude = 21.4225,
        longitude = 39.8262,
        timezone = "Asia/Riyadh",
        source = "MANUAL",
        updatedAt = System.currentTimeMillis()
    )

    val defaultConfigurations = listOf(
        PrayerConfiguration("Tahajjud", adhanEnabled = false, notificationEnabled = true, quietModeEnabled = true, quietModeDurationMinutes = 30, audioTrackId = "gentle_tone", azanOffsetMinutes = 0, jamatOffsetMinutes = 0, enterSalahNotificationEnabled = true),
        PrayerConfiguration("Fajr", adhanEnabled = true, notificationEnabled = true, quietModeEnabled = true, quietModeDurationMinutes = 25, audioTrackId = "makkah_adhan", azanOffsetMinutes = 0, jamatOffsetMinutes = 25, enterSalahNotificationEnabled = true),
        PrayerConfiguration("Sunrise", adhanEnabled = false, notificationEnabled = true, quietModeEnabled = false, quietModeDurationMinutes = 0, audioTrackId = "gentle_tone", azanOffsetMinutes = 0, jamatOffsetMinutes = 0, enterSalahNotificationEnabled = true),
        PrayerConfiguration("Dhuhr", adhanEnabled = true, notificationEnabled = true, quietModeEnabled = true, quietModeDurationMinutes = 20, audioTrackId = "makkah_adhan", azanOffsetMinutes = 0, jamatOffsetMinutes = 20, enterSalahNotificationEnabled = true),
        PrayerConfiguration("Jummah", adhanEnabled = true, notificationEnabled = true, quietModeEnabled = true, quietModeDurationMinutes = 30, audioTrackId = "makkah_adhan", azanOffsetMinutes = 0, jamatOffsetMinutes = 30, enterSalahNotificationEnabled = true),
        PrayerConfiguration("Asr", adhanEnabled = true, notificationEnabled = true, quietModeEnabled = true, quietModeDurationMinutes = 20, audioTrackId = "madinah_adhan", azanOffsetMinutes = 0, jamatOffsetMinutes = 20, enterSalahNotificationEnabled = true),
        PrayerConfiguration("Maghrib", adhanEnabled = true, notificationEnabled = true, quietModeEnabled = true, quietModeDurationMinutes = 20, audioTrackId = "makkah_adhan", azanOffsetMinutes = 0, jamatOffsetMinutes = 10, enterSalahNotificationEnabled = true),
        PrayerConfiguration("Isha", adhanEnabled = true, notificationEnabled = true, quietModeEnabled = true, quietModeDurationMinutes = 25, audioTrackId = "madinah_adhan", azanOffsetMinutes = 0, jamatOffsetMinutes = 25, enterSalahNotificationEnabled = true)
    )

    val defaultAdhanTracks = listOf(
        AdhanTrack(
            id = "makkah_adhan",
            title = "Makkah Al-Mukarramah",
            subtitle = "Majestic Grand Mosque Melody (Full)",
            localUri = "raw://adhan_makkah",
            durationSeconds = 180,
            isDefault = true
        ),
        AdhanTrack(
            id = "madinah_adhan",
            title = "Madinah Al-Munawwarah",
            subtitle = "Serene Prophet's Mosque Melody (Full)",
            localUri = "raw://adhan_madinah",
            durationSeconds = 175,
            isDefault = false
        ),
        AdhanTrack(
            id = "alaqsa_adhan",
            title = "Al-Aqsa Sanctuary",
            subtitle = "Historic Palestinian Maqam (Shortened)",
            localUri = "raw://adhan_alaqsa",
            durationSeconds = 90,
            isDefault = false
        ),
        AdhanTrack(
            id = "soft_takbeer",
            title = "Soft Takbeerat",
            subtitle = "Calm Takbeer Intro Only",
            localUri = "raw://soft_takbeer",
            durationSeconds = 35,
            isDefault = false
        ),
        AdhanTrack(
            id = "gentle_tone",
            title = "Notification Chime Only",
            subtitle = "Gentle prayer chime without full vocal Adhan",
            localUri = "raw://gentle_chime",
            durationSeconds = 8,
            isDefault = false
        )
    )

    val verifiedAyatList = listOf(
        Ayat(
            id = 1,
            arabicText = "وَاسْتَعِينُوا بِالصَّبْرِ وَالصَّلَاةِ ۚ وَإِنَّهَا لَكَبِيرَةٌ إِلَّا عَلَى الْخَاشِعِينَ",
            transliteration = "Wasta'eenoo bis-sabri was-salaah; wa innahaa lakabeeratun illaa 'alal-khaashi'een",
            englishTranslation = "And seek help through patience and prayer, and indeed, it is difficult except for the humbly submissive [to Allah].",
            urduTranslation = "اور صبر اور نماز کے ساتھ مدد طلب کرو، اور بیشک یہ بہت بھاری ہے مگر ان پر جو خشوع کرنے والے ہیں۔",
            surahName = "Al-Baqarah",
            surahArabicName = "البقرة",
            ayatNumber = "45",
            referenceSource = "Surah Al-Baqarah (2:45)",
            verificationStatus = "VERIFIED_AUTHENTIC",
            themeTopic = "Patience & Devotion"
        ),
        Ayat(
            id = 2,
            arabicText = "حَافِظُوا عَلَى الصَّلَوَاتِ وَالصَّلَاةِ الْوُسْطَىٰ وَقُومُوا لِلَّهِ قَانِتِينَ",
            transliteration = "Haafizoo 'alas-salawaati was-salaatil-wustaa wa qoomoo lillaahi qaaniteen",
            englishTranslation = "Maintain with care the [obligatory] prayers and [in particular] the middle prayer and stand before Allah, devoutly obedient.",
            urduTranslation = "سب نمازوں کی حفاظت کرو بالخصوص درمیانی نماز کی، اور اللہ کے حضور باادب فرمانبردار بن کر کھڑے رہو۔",
            surahName = "Al-Baqarah",
            surahArabicName = "البقرة",
            ayatNumber = "238",
            referenceSource = "Surah Al-Baqarah (2:238)",
            verificationStatus = "VERIFIED_AUTHENTIC",
            themeTopic = "Guarding Prayer"
        ),
        Ayat(
            id = 3,
            arabicText = "إِنَّ الصَّلَاةَ كَانَتْ عَلَى الْمُؤْمِنِينَ كِتَابًا مَّوْقُوتًا",
            transliteration = "Innas-salaata kaanat 'alal-mu'mineena kitaabam-mawqvootaa",
            englishTranslation = "Indeed, prayer has been decreed upon the believers a decree of specified times.",
            urduTranslation = "بیشک نماز مومنوں پر مقررہ اوقات میں فرض کی گئی ہے۔",
            surahName = "An-Nisa",
            surahArabicName = "النساء",
            ayatNumber = "103",
            referenceSource = "Surah An-Nisa (4:103)",
            verificationStatus = "VERIFIED_AUTHENTIC",
            themeTopic = "Timely Prayer"
        ),
        Ayat(
            id = 4,
            arabicText = "إِنَّنِي أَنَا اللَّهُ لَا إِلَٰهَ إِلَّا أَنَا فَاعْبُدْنِي وَأَقِمِ الصَّلَاةَ لِذِكْرِي",
            transliteration = "Innanee ana-Allaahu laa ilaaha illaa ana fa'budnee wa aqimis-salaata lizikree",
            englishTranslation = "Indeed, I am Allah. There is no deity except Me, so worship Me and establish prayer for My remembrance.",
            urduTranslation = "بیشک میں ہی اللہ ہوں، میرے سوا کوئی معبود نہیں، پس میری عبادت کر اور میری یاد کے لیے نماز قائم رکھ۔",
            surahName = "Taha",
            surahArabicName = "طه",
            ayatNumber = "14",
            referenceSource = "Surah Taha (20:14)",
            verificationStatus = "VERIFIED_AUTHENTIC",
            themeTopic = "Remembrance of Allah"
        ),
        Ayat(
            id = 5,
            arabicText = "اتْلُ مَا أُوحِيَ إِلَيْكَ مِنَ الْكِتَابِ وَأَقِمِ الصَّلَاةَ ۖ إِنَّ الصَّلَاةَ تَنْهَىٰ عَنِ الْفَحْشَاءِ وَالْمُنكَرِ ۗ وَلَذِكْرُ اللَّهِ أَكْبَرُ",
            transliteration = "Utlu maaa oohiya ilaika minal-Kitaabi wa aqimis-salaah; innas-salaata tanhaa 'anil-fahshaaa'i wal-munkar; wa lazikrul-laahi akbar",
            englishTranslation = "Recite what has been revealed to you of the Book and establish prayer. Indeed, prayer prohibits immorality and wrongdoing, and the remembrance of Allah is greater.",
            urduTranslation = "جو کتاب آپ کی طرف وحی کی گئی ہے اسے پڑھیں اور نماز قائم کریں۔ بیشک نماز بےحیائی اور برائی سے روکتی ہے، اور اللہ کا ذکر سب سے بڑا ہے۔",
            surahName = "Al-Ankabut",
            surahArabicName = "العنكبوت",
            ayatNumber = "45",
            referenceSource = "Surah Al-Ankabut (29:45)",
            verificationStatus = "VERIFIED_AUTHENTIC",
            themeTopic = "Purification through Prayer"
        ),
        Ayat(
            id = 6,
            arabicText = "الَّذِينَ آمَنُوا وَتَطْمَئِنُّ قُلُوبُهُم بِذِكْرِ اللَّهِ ۗ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
            transliteration = "Allazeena aamanoo wa tatma'innu quloobuhum bizikril-laah; alaa bizikril-laahi tatma'innul-quloob",
            englishTranslation = "Those who have believed and whose hearts are assured by the remembrance of Allah. Unquestionably, by the remembrance of Allah hearts are assured.",
            urduTranslation = "جو لوگ ایمان لائے اور جن کے دل اللہ کے ذکر سے اطمینان پاتے ہیں، سن لو! اللہ کے ذکر ہی سے دلوں کو سکون ملتا ہے۔",
            surahName = "Ar-Ra'd",
            surahArabicName = "الرعد",
            ayatNumber = "28",
            referenceSource = "Surah Ar-Ra'd (13:28)",
            verificationStatus = "VERIFIED_AUTHENTIC",
            themeTopic = "Peace of Heart"
        ),
        Ayat(
            id = 7,
            arabicText = "قَدْ أَفْلَحَ الْمُؤْمِنُونَ ۝ الَّذِينَ هُمْ فِي صَلَاتِهِمْ خَاشِعُونَ",
            transliteration = "Qad aflahal-mu'minoon; Allazeena hum fee salaatihim khaashi'oon",
            englishTranslation = "Certainly will the believers have succeeded: They who are during their prayer humbly submissive.",
            urduTranslation = "یقیناً مومنین کامیاب ہو گئے، جو اپنی نمازوں میں عاجزی و خشوع اختیار کرتے ہیں۔",
            surahName = "Al-Mu'minun",
            surahArabicName = "المؤمنون",
            ayatNumber = "1-2",
            referenceSource = "Surah Al-Mu'minun (23:1-2)",
            verificationStatus = "VERIFIED_AUTHENTIC",
            themeTopic = "Khushoo in Salah"
        ),
        Ayat(
            id = 8,
            arabicText = "أَقِمِ الصَّلَاةَ لِدُلُوكِ الشَّمْسِ إِلَىٰ غَسَقِ اللَّيْلِ وَقُرْآنَ الْفَجْرِ ۖ إِنَّ قُرْآنَ الْفَجْرِ كَانَ مَشْهُودًا",
            transliteration = "Aqimis-salaata lidulookish-shamsi ilaa ghasaqil-layli wa qur-aanal-fajr; inna qur-aanal-fajri kaana mash-hoodaa",
            englishTranslation = "Establish prayer at the decline of the sun until the darkness of the night and [also] the Quran of dawn. Indeed, the recitation of dawn is ever witnessed.",
            urduTranslation = "نماز قائم کیجیے سورج کے ڈھلنے سے رات کی تاریکی تک اور فجر کا قرآن پڑھنا بھی، بیشک فجر کے وقت قرآن کا پڑھا جانا فرشتوں کے سامنے حاضر کیا جاتا ہے۔",
            surahName = "Al-Isra",
            surahArabicName = "الإسراء",
            ayatNumber = "78",
            referenceSource = "Surah Al-Isra (17:78)",
            verificationStatus = "VERIFIED_AUTHENTIC",
            themeTopic = "Times of Salah"
        ),
        Ayat(
            id = 101,
            arabicText = "وَلَا تَقْرَبُوا الْفَوَاحِشَ مَا ظَهَرَ مِنْهَا وَمَا بَطَنَ ۖ وَلَا تَقْتُلُوا النَّفْسَ الَّتِي حَرَّمَ اللَّهُ إِلَّا بِالْحَقِّ ۚ ذَٰلِكُمْ وَصَّاكُم بِهِ لَعَلَّكُمْ تَعْقِلُونَ",
            transliteration = "Wa laa taqrabool-fawaahisha maa zahara minhaa wa maa batana; wa laa taqtuloon-nafsal-latee harramal-laahu illaa bilhaqq; zaalikum wassaakum bihee la'allakum ta'qiloon",
            englishTranslation = "And do not approach immoralities (Fawahish) - what is apparent of them and what is concealed. This has He instructed you that you may use reason.",
            urduTranslation = "اور بے حیائی کے کاموں کے قریب بھی نہ جاؤ، خواہ وہ کھلے ہوں یا چھپے ہوئے۔ اس کا اللہ نے تمہیں حکم دیا ہے تاکہ تم عقل سے کام لو۔",
            surahName = "Al-An'am",
            surahArabicName = "الأنعام",
            ayatNumber = "151",
            referenceSource = "Surah Al-An'am (6:151)",
            verificationStatus = "VERIFIED_AUTHENTIC",
            themeTopic = "Fahisha & Modesty"
        ),
        Ayat(
            id = 102,
            arabicText = "قُلْ إِنَّمَا حَرَّمَ رَبِّيَ الْفَوَاحِشَ مَا ظَهَرَ مِنْهَا وَمَا بَطَنَ وَالْإِثْمَ وَالْبَغْيَ بِغَيْرِ الْحَقِّ",
            transliteration = "Qul innamaa harrama Rabbiyal-fawaahisha maa zahara minhaa wa maa batana wal-ithma wal-baghya bighayril-haqq",
            englishTranslation = "Say: My Lord has only forbidden immoralities (Fawahish) - what is apparent of them and what is concealed - and sin, and oppression without right.",
            urduTranslation = "کہہ دیجئے: میرے رب نے تو بے حیائی کے تمام کام حرام کیے ہیں، خواہ وہ ظاہر ہوں یا پوشیدہ، اور گناہ اور ناحق زیادتی بھی۔",
            surahName = "Al-A'raf",
            surahArabicName = "الأعراف",
            ayatNumber = "33",
            referenceSource = "Surah Al-A'raf (7:33)",
            verificationStatus = "VERIFIED_AUTHENTIC",
            themeTopic = "Fahisha & Modesty"
        ),
        Ayat(
            id = 103,
            arabicText = "إِنَّ الَّذِينَ يُحِبُّونَ أَن تَشِيعَ الْفَاحِشَةُ فِي الَّذِينَ آمَنُوا لَهُمْ عَذَابٌ أَلِيمٌ فِي الدُّنْيَا وَالْآخِرَةِ ۚ وَاللَّهُ يَعْلَمُ وَأَنتُمْ لَا تَعْلَمُونَ",
            transliteration = "Innal-lazeena yuhibboona an tashee'al-faahishatu fil-lazeena aamanoo lahum 'azaabun aleemun fid-dunyaa wal-aakhirah; wallaahu ya'lamu wa antum laa ta'lamoon",
            englishTranslation = "Indeed, those who like that immorality (Fahishah) should spread among those who have believed will have a painful punishment in this world and the Hereafter. And Allah knows and you do not know.",
            urduTranslation = "بے شک جو لوگ یہ چاہتے ہیں کہ ایمان والوں میں بے حیائی پھیلے، ان کے لیے دنیا اور آخرت میں دردناک عذاب ہے، اور اللہ جانتا ہے اور تم نہیں جانتے۔",
            surahName = "An-Nur",
            surahArabicName = "النور",
            ayatNumber = "19",
            referenceSource = "Surah An-Nur (24:19)",
            verificationStatus = "VERIFIED_AUTHENTIC",
            themeTopic = "Fahisha & Modesty"
        ),
        Ayat(
            id = 104,
            arabicText = "قُل لِّلْمُؤْمِنِينَ يَغُضُّوا مِنْ أَبْصَارِهِمْ وَيَحْفَظُوا فُرُوجَهُمْ ۚ ذَٰلِكَ أَزْكَىٰ لَهُمْ ۗ إِنَّ اللَّهَ خَبِيرٌ بِمَا يَصْنَعُونَ",
            transliteration = "Qul lil-mu'mineena yaghuddoo min absaarihim wa yahfazoo furoojahum; zaalika azkaa lahum; innal-laaha khabeerum bimaa yasna'oon",
            englishTranslation = "Tell the believing men to reduce [some] of their vision and guard their private parts. That is purer for them. Indeed, Allah is Acquainted with what they do.",
            urduTranslation = "مومن مردوں سے کہہ دیجئے کہ وہ اپنی نگاہیں نیچی رکھیں اور اپنی شرمگاہوں کی حفاظت کریں۔ یہ ان کے لیے زیادہ پاکیزہ ہے، بیشک اللہ اس سے باخبر ہے جو وہ کرتے ہیں۔",
            surahName = "An-Nur",
            surahArabicName = "النور",
            ayatNumber = "30",
            referenceSource = "Surah An-Nur (24:30)",
            verificationStatus = "VERIFIED_AUTHENTIC",
            themeTopic = "Fahisha & Modesty"
        ),
        Ayat(
            id = 105,
            arabicText = "وَلَا تَقْرَبُوا الزِّنَىٰ ۖ إِنَّهُ كَانَ فَاحِشَةً وَسَاءَ سَبِيلًا",
            transliteration = "Wa laa taqrabuz-zinaaa; innahoo kaana faahishatanw wa saaa'a sabeelaa",
            englishTranslation = "And do not approach unlawful sexual intimacy. Indeed, it is ever an immorality (Fahishah) and is an evil way.",
            urduTranslation = "اور زنا (بدکاری) کے قریب بھی مت جاؤ، بے شک وہ بڑی بے حیائی ہے اور برا راستہ ہے۔",
            surahName = "Al-Isra",
            surahArabicName = "الإسراء",
            ayatNumber = "32",
            referenceSource = "Surah Al-Isra (17:32)",
            verificationStatus = "VERIFIED_AUTHENTIC",
            themeTopic = "Fahisha & Modesty"
        ),
        Ayat(
            id = 106,
            arabicText = "أَلَمْ يَعْلَم بِأَنَّ اللَّهَ يَرَىٰ",
            transliteration = "Alam ya'lam bi-annal-laaha yaraa",
            englishTranslation = "Does he not know that Allah sees?",
            urduTranslation = "کیا وہ نہیں جانتا کہ اللہ اسے دیکھ رہا ہے؟",
            surahName = "Al-Alaq",
            surahArabicName = "العلق",
            ayatNumber = "14",
            referenceSource = "Surah Al-Alaq (96:14)",
            verificationStatus = "VERIFIED_AUTHENTIC",
            themeTopic = "Fahisha & Modesty"
        ),
        Ayat(
            id = 107,
            arabicText = "يَعْلَمُ خَائِنَةَ الْأَعْيُنِ وَمَا تُخْفِي الصُّدُورُ",
            transliteration = "Ya'lamu khaa'inatal-a'yuni wa maa tukhfis-sudoor",
            englishTranslation = "He knows that which deceives the eyes and what the breasts conceal.",
            urduTranslation = "وہ آنکھوں کی چوری (خیانت) کو بھی جانتا ہے اور ان باتوں کو بھی جو سینے چھپاتے ہیں۔",
            surahName = "Ghafir",
            surahArabicName = "غافر",
            ayatNumber = "19",
            referenceSource = "Surah Ghafir (40:19)",
            verificationStatus = "VERIFIED_AUTHENTIC",
            themeTopic = "Fahisha & Modesty"
        ),
        Ayat(
            id = 108,
            arabicText = "إِنَّ الصَّلَاةَ تَنْهَىٰ عَنِ الْفَحْشَاءِ وَالْمُنكَرِ ۗ وَلَذِكْرُ اللَّهِ أَكْبَرُ ۗ وَاللَّهُ يَعْلَمُ مَا تَصْنَعُونَ",
            transliteration = "Innas-salaata tanhaa 'anil-fahshaaa'i wal-munkar; wa lazikrul-laahi akbar; wallaahu ya'lamu maa tasna'oon",
            englishTranslation = "Indeed, prayer prohibits immorality (Fahsha) and wrongdoing, and the remembrance of Allah is greater. And Allah knows that which you do.",
            urduTranslation = "بیشک نماز بے حیائی اور برائی سے روکتی ہے، اور اللہ کا ذکر سب سے بڑا ہے، اور اللہ جانتا ہے جو تم کرتے ہو۔",
            surahName = "Al-Ankabut",
            surahArabicName = "العنكبوت",
            ayatNumber = "45",
            referenceSource = "Surah Al-Ankabut (29:45)",
            verificationStatus = "VERIFIED_AUTHENTIC",
            themeTopic = "Fahisha & Modesty"
        ),
        Ayat(
            id = 201,
            arabicText = "فَوَيْلٌ لِّلْمُصَلِّينَ ۝ الَّذِينَ هُمْ عَن صَلَاتِهِمْ سَاهُونَ",
            transliteration = "Fa-wailul-lil-musalleen; Allazeena hum 'an salaatihim saahoon",
            englishTranslation = "So woe to those who pray, [but] who are heedless of their prayer.",
            urduTranslation = "پس تباہی اور بربادی ہے ان نمازیوں کے لیے جو اپنی نماز سے غافل اور لاپروا ہیں۔",
            surahName = "Al-Ma'un",
            surahArabicName = "الماعون",
            ayatNumber = "4-5",
            referenceSource = "Surah Al-Ma'un (107:4-5)",
            verificationStatus = "VERIFIED_AUTHENTIC",
            themeTopic = "Salah Warning & Azaab"
        ),
        Ayat(
            id = 202,
            arabicText = "مَا سَلَكَكُمْ فِي سَقَرَ ۝ قَالُوا لَمْ نَكُ مِنَ الْمُصَلِّينَ",
            transliteration = "Maa salakakum fee saqar; Qaaloo lam naku minal-musalleen",
            englishTranslation = "What led you into Hellfire (Saqar)? They will say: We were not of those who prayed.",
            urduTranslation = "تمہیں کس چیز نے دوزخ کے عذاب میں جھونک دیا؟ وہ کہیں گے: ہم نماز ادا کرنے والوں میں سے نہ تھے۔",
            surahName = "Al-Muddaththir",
            surahArabicName = "المدثر",
            ayatNumber = "42-43",
            referenceSource = "Surah Al-Muddaththir (74:42-43)",
            verificationStatus = "VERIFIED_AUTHENTIC",
            themeTopic = "Salah Warning & Azaab"
        ),
        Ayat(
            id = 203,
            arabicText = "فَخَلَفَ مِن بَعْدِهِمْ خَلْفٌ أَضَاعُوا الصَّلَاةَ وَاتَّبَعُوا الشَّهَوَاتِ ۖ فَسَوْفَ يَلْقَوْنَ غَيًّا",
            transliteration = "Fakhalafa min ba'dihim khalfun adaa'us-salaata wattaba'ush-shahawaati fasawfa yalqawna ghayyaa",
            englishTranslation = "Then there succeeded after them successors who neglected prayer and followed desires; so they will face destruction in Hell (Ghayya).",
            urduTranslation = "پھر ان کے بعد ایسے ناخلف آئے جنہوں نے نماز کو ضائع کر دیا اور خواہشات کے پیچھے چل پڑے، پس عنقریب وہ جہنم کی ہلاکت میں جا گریں گے۔",
            surahName = "Maryam",
            surahArabicName = "مريم",
            ayatNumber = "59",
            referenceSource = "Surah Maryam (19:59)",
            verificationStatus = "VERIFIED_AUTHENTIC",
            themeTopic = "Salah Warning & Azaab"
        ),
        Ayat(
            id = 204,
            arabicText = "فَلَا صَدَّقَ وَلَا صَلَّىٰ ۝ وَلَٰكِن كَذَّبَ وَتَوَلَّىٰ",
            transliteration = "Falaa saddaqa wa laa sallaa; Wa laakin kazzaba wa tawallaa",
            englishTranslation = "For he neither believed nor prayed, but he denied and turned away!",
            urduTranslation = "پس نہ اس نے سچ مانا اور نہ نماز ادا کی، بلکہ اس نے جھٹلایا اور پیٹھ پھیر لی۔",
            surahName = "Al-Qiyamah",
            surahArabicName = "القيامة",
            ayatNumber = "31-32",
            referenceSource = "Surah Al-Qiyamah (75:31-32)",
            verificationStatus = "VERIFIED_AUTHENTIC",
            themeTopic = "Salah Warning & Azaab"
        ),
        Ayat(
            id = 205,
            arabicText = "الْعَهْدُ الَّذِي بَيْنَنَا وَبَيْنَهُمُ الصَّلَاةُ فَمَنْ تَرَكَهَا فَقَدْ كَفَرَ",
            transliteration = "Al-'ahdul-lazee bainanaa wa bainahumus-salaatu faman tarakahaa faqad kafar",
            englishTranslation = "The Prophet (ﷺ) said: 'The covenant between us and them is prayer; whoever abandons it has committed disbelief.'",
            urduTranslation = "رسول اللہ ﷺ نے فرمایا: ہمارے اور ان کے درمیان عہد نماز ہے، جس نے اسے ترک کیا اس نے کفر کیا۔",
            surahName = "Hadith Sahih",
            surahArabicName = "الحديث الشريف",
            ayatNumber = "463",
            referenceSource = "Sunan an-Nasa'i 463, Jami` at-Tirmidhi 2621",
            verificationStatus = "VERIFIED_AUTHENTIC",
            themeTopic = "Salah Warning & Azaab"
        )
    )

    val fahishaAyatList = verifiedAyatList.filter { it.themeTopic == "Fahisha & Modesty" }
    val warningAyatList = verifiedAyatList.filter { it.themeTopic == "Salah Warning & Azaab" }

    fun generateInitialSalahRecords(): List<SalahRecord> {
        // Start from absolute zero - no fake seed records
        return emptyList()
    }
}

