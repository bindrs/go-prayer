package com.example.ui.azkaar

data class AzkarItem(
    val id: Int,
    val category: AzkarCategory,
    val titleEn: String,
    val titleUr: String = titleEn,
    val arabicText: String,
    val englishTranslation: String,
    val urduTranslation: String = "",
    val noteEn: String? = null,
    val noteUr: String? = null,
    val targetCount: Int,
    val reference: String
)

enum class AzkarCategory {
    MORNING,
    EVENING,
    POST_SALAH,
    TASBEEH
}

data class TasbeehPreset(
    val id: String,
    val titleEn: String,
    val titleUr: String = titleEn,
    val arabicText: String,
    val transliteration: String,
    val targetCount: Int,
    val virtueEn: String,
    val virtueUr: String = virtueEn
)

val tasbeehPresets = listOf(
    TasbeehPreset(
        id = "subhanallah",
        titleEn = "SubhanAllah",
        titleUr = "سبحان اللہ",
        arabicText = "سُبْحَانَ اللَّهِ",
        transliteration = "SubhanAllah",
        targetCount = 33,
        virtueEn = "Glory be to Allah, free from all imperfections. (Recite 33 times)",
        virtueUr = "اللہ پاک ہر عیب و نقص سے پاک ہے۔ (33 مرتبہ)"
    ),
    TasbeehPreset(
        id = "alhamdulillah",
        titleEn = "Alhamdulillah",
        titleUr = "الحمد للہ",
        arabicText = "الْحَمْدُ لِلَّهِ",
        transliteration = "Alhamdulillah",
        targetCount = 33,
        virtueEn = "All praise and gratitude belong to Allah alone. (Recite 33 times)",
        virtueUr = "تمام تعریفیں اور شکر صرف اللہ کے لیے ہے۔ (33 مرتبہ)"
    ),
    TasbeehPreset(
        id = "allahuakbar",
        titleEn = "Allahu Akbar",
        titleUr = "اللہ اکبر",
        arabicText = "اللَّهُ أَكْبَرُ",
        transliteration = "Allahu Akbar",
        targetCount = 34,
        virtueEn = "Allah is the Greatest over all things. (Recite 34 times)",
        virtueUr = "اللہ سب سے بڑا ہے۔ (34 مرتبہ)"
    ),
    TasbeehPreset(
        id = "astaghfirullah",
        titleEn = "Astaghfirullah",
        titleUr = "استغفار",
        arabicText = "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",
        transliteration = "Astaghfirullah wa atoobu ilayh",
        targetCount = 100,
        virtueEn = "I seek forgiveness of Allah and repent to Him. Opens sustenance and relieves hardship.",
        virtueUr = "گناہوں کی معافی، روزی میں برکت اور پریشانیوں سے نجات۔"
    ),
    TasbeehPreset(
        id = "kalima_tawheed",
        titleEn = "Kalima Tawheed",
        titleUr = "کلمہ توحید",
        arabicText = "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
        transliteration = "La ilaha illallahu wahdahu la shareeka lah...",
        targetCount = 100,
        virtueEn = "Reward of freeing 10 slaves, 100 good deeds recorded, and 100 sins erased.",
        virtueUr = "10 غلام آزاد کرنے کا ثواب اور 100 برائیاں مٹا دی جاتی ہیں۔"
    ),
    TasbeehPreset(
        id = "subhanallah_bihamdihi",
        titleEn = "SubhanAllahi wa bihamdihi",
        titleUr = "سبحان اللہ وبحمدہ",
        arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ ، سُبْحَانَ اللَّهِ الْعَظِيمِ",
        transliteration = "SubhanAllahi wa bihamdihi, SubhanAllahil Azeem",
        targetCount = 100,
        virtueEn = "Two words light on the tongue, heavy in the scales, beloved to the Most Merciful.",
        virtueUr = "زبان پر ہلکے، میزان میں بھاری اور رحمن کو محبوب ترین کلمات۔"
    ),
    TasbeehPreset(
        id = "durood",
        titleEn = "Durood Sharif",
        titleUr = "درود پاک",
        arabicText = "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ",
        transliteration = "Allahumma Salli 'Ala Muhammad",
        targetCount = 100,
        virtueEn = "Whoever sends blessings upon the Prophet ﷺ once, Allah sends ten mercies upon them.",
        virtueUr = "ایک بار پڑھنے پر اللہ کی 10 رحمتیں نازل ہوتی ہیں۔"
    ),
    TasbeehPreset(
        id = "hawqala",
        titleEn = "La Hawla wa la Quwwata",
        titleUr = "لا حول ولا قوة إلا بالله",
        arabicText = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ الْعَلِيِّ الْعَظِيمِ",
        transliteration = "La hawla wa la quwwata illa billah",
        targetCount = 100,
        virtueEn = "A treasure from the treasures of Paradise, curing ninety-nine ailments.",
        virtueUr = "جنت کے خزانوں میں سے ایک خزانہ اور 99 بیماریوں کا علاج۔"
    ),
    TasbeehPreset(
        id = "free_tasbeeh",
        titleEn = "Free Dhikr Counter",
        titleUr = "آزادانہ تسبیح (کھلی گنتی)",
        arabicText = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
        transliteration = "Bismillahir Rahmanir Raheem",
        targetCount = 1000,
        virtueEn = "Count any supplication freely with continuous tactile and audio feedback.",
        virtueUr = "اپنی مرضی سے بغیر کسی قید کے ذکر و تسبیح کیجیے۔"
    )
)

val morningAzkarList = listOf(
    AzkarItem(
        id = 101,
        category = AzkarCategory.MORNING,
        titleEn = "Morning Kingdom Proclamation",
        titleUr = "صبح کے وقت اللہ کی بادشاہی کا اقرار",
        arabicText = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا هُوَ، وَإِلَيْهِ النُّشُورُ۔",
        englishTranslation = "We have entered the morning and the kingdom belongs to Allah; all praise is due to Allah. None has the right to be worshipped except Him, and unto Him is the resurrection.",
        urduTranslation = "ہم نے صبح کی اور اللہ کے لیے صبح ہوئی جو تمام بادشاہی کا مالک ہے...",
        noteEn = "Recite 3 times every morning.",
        targetCount = 3,
        reference = "Sahih Muslim 2723"
    ),
    AzkarItem(
        id = 102,
        category = AzkarCategory.MORNING,
        titleEn = "Steadfastness Upon Islamic Nature",
        titleUr = "دین اسلام اور کلمۂ اخلاص پر صبح",
        arabicText = "أَصْبَحْنَا عَلَى فِطْرَةِ الْإِسْلَامِ، وَكَلِمَةِ الْإِخْلَاصِ، وَعَلَى دِينِ نَبِيِّنَا مُحَمَّدٍ ﷺ، وَعَلَى مِلَّةِ أَبِينَا إِبْرَاهِيمَ حَنِيفًا وَمَا كَانَ مِنَ الْمُشْرِكِينَ۔",
        englishTranslation = "We have entered the morning upon the natural religion of Islam, the word of sincere faith, the religion of our Prophet Muhammad ﷺ, and the faith of our father Ibrahim who was a true monotheist.",
        urduTranslation = "ہم نے صبح کی اسلام کی فطرت، کلمۂ اخلاص...",
        noteEn = "Affirms pure monotheism each morning.",
        targetCount = 3,
        reference = "Musnad Ahmad 15360"
    ),
    AzkarItem(
        id = 103,
        category = AzkarCategory.MORNING,
        titleEn = "Praise Befitting Allah's Majesty",
        titleUr = "اللہ کے شایانِ شان حمد",
        arabicText = "يَا رَبِّي، لَكَ الْحَمْدُ كَمَا يَنْبَغِي لِجَلَالِ وَجْهِكَ وَعَظِيمِ سُلْطَانِكَ۔",
        englishTranslation = "O my Lord! All praise is due to You as befits the majesty of Your Countenance and the greatness of Your Authority.",
        urduTranslation = "اے میرے رب! تیرے ہی لیے تمام تعریفیں ہیں...",
        noteEn = "Recite 3 times morning and evening.",
        targetCount = 3,
        reference = "Sunan Ibn Majah 3801"
    ),
    AzkarItem(
        id = 104,
        category = AzkarCategory.MORNING,
        titleEn = "Contentment with Allah and Islam",
        titleUr = "اللہ، اسلام اور رسول ﷺ پر رضامندی",
        arabicText = "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالْإِسْلَامِ دِينًا، وَبِمُحَمَّدٍ ﷺ نَبِيًّا وَرَسُولًا۔",
        englishTranslation = "I am pleased with Allah as my Lord, with Islam as my religion, and with Muhammad ﷺ as my Prophet and Messenger.",
        urduTranslation = "میں اللہ کے رب ہونے، اسلام کے دین ہونے...",
        noteEn = "Whoever recites this 3 times, Allah has promised to make them pleased on the Day of Resurrection.",
        targetCount = 3,
        reference = "Sunan Abi Dawud 1529"
    ),
    AzkarItem(
        id = 105,
        category = AzkarCategory.MORNING,
        titleEn = "Praise Equaling the Creation's Number",
        titleUr = "مخلوق کی گنتی کے برابر تسبیح",
        arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ۔",
        englishTranslation = "Glory be to Allah and praise be to Him, according to the number of His creation, according to His pleasure, according to the weight of His Throne, and according to the ink of His words.",
        urduTranslation = "اللہ پاک ہے اپنی تعریف کے ساتھ، اپنی تمام مخلوقات کی گنتی کے برابر...",
        noteEn = "Recite 3 times. Equal in weight to hours of continuous dhikr.",
        targetCount = 3,
        reference = "Sahih Muslim 2726"
    ),
    AzkarItem(
        id = 106,
        category = AzkarCategory.MORNING,
        titleEn = "Immunity from Earthly and Heavenly Harm",
        titleUr = "آسمان و زمین کے ہر نقصان سے حفاظت",
        arabicText = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ، وَهُوَ السَّمِيعُ الْعَلِيمُ۔",
        englishTranslation = "In the name of Allah, with Whose name nothing on earth or in the heavens can cause harm, and He is the All-Hearing, the All-Knowing.",
        urduTranslation = "اللہ کے نام سے جس کے نام کی برکت سے زمین اور آسمان کی کوئی چیز نقصان نہیں پہنچا سکتی...",
        noteEn = "Recite 3 times morning and evening. Protects against all sudden calamities.",
        targetCount = 3,
        reference = "Sunan Abi Dawud 5088"
    ),
    AzkarItem(
        id = 107,
        category = AzkarCategory.MORNING,
        titleEn = "Refuge in Allah's Perfect Words",
        titleUr = "تمام مخلوق کے شر سے پناہ",
        arabicText = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ۔",
        englishTranslation = "I seek refuge in the perfect words of Allah from the evil of what He has created.",
        urduTranslation = "میں پناہ مانگتا ہوں اللہ کے مکمل کلمات کے ذریعے...",
        noteEn = "Recite 3 times for full protection from poisonous creatures and evil.",
        targetCount = 3,
        reference = "Sahih Muslim 2709"
    ),
    AzkarItem(
        id = 108,
        category = AzkarCategory.MORNING,
        titleEn = "Relief from Anxiety and Debt",
        titleUr = "پریشانی، قرض اور کمزوری سے نجات",
        arabicText = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ، وَأَعُوذُ بِكَ مِنَ الْعَجْزِ وَالْكَسَلِ، وَأَعُوذُ بِكَ مِنْ الْجُبْنِ وَالْبُخْلِ، وَأَعُوذُ بِكَ مِنْ غَلَبَةِ الدَّيْنِ وَقَهْرِ الرِّجَالِ۔",
        englishTranslation = "O Allah, I seek refuge in You from grief and sadness, weakness and laziness, cowardice and stinginess, and from the burden of debt and oppression of people.",
        urduTranslation = "اے اللہ! میں تیری پناہ مانگتا ہوں فکر و غم سے...",
        noteEn = "Recite 3 times morning and evening for tranquility and financial ease.",
        targetCount = 3,
        reference = "Sunan Abi Dawud 1555"
    ),
    AzkarItem(
        id = 109,
        category = AzkarCategory.MORNING,
        titleEn = "Supplication for Well-being & Senses",
        titleUr = "بدن، کان اور آنکھوں کی عافیت",
        arabicText = "اللَّهُمَّ عَافِنِي فِي بَدَنِي، اللَّهُمَّ عَافِنِي فِي سَمْعِي، اللَّهُمَّ عَافِنِي فِي بَصَرِي، لَا إِلَهَ إِلَّا أَنْتَ۔ اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْكُفْرِ وَالْفَقْرِ، وَأَعُوذُ بِكَ مِنْ عَذَابِ الْقَبْرِ، لَا إِلَهَ إِلَّا أَنْتَ۔",
        englishTranslation = "O Allah, grant well-being to my body; O Allah, grant well-being to my hearing; O Allah, grant well-being to my sight. There is none worthy of worship except You. O Allah, I seek refuge in You from disbelief and poverty, and from the punishment of the grave.",
        urduTranslation = "اے اللہ! مجھے عافیت دے میرے بدن میں...",
        noteEn = "Recite 3 times morning and evening.",
        targetCount = 3,
        reference = "Sunan Abi Dawud 5090"
    ),
    AzkarItem(
        id = 110,
        category = AzkarCategory.MORNING,
        titleEn = "Sayyid al-Istighfar (Master Forgiveness)",
        titleUr = "سید الاستغفار (استغفار کا سردار)",
        arabicText = "اللَّهُمَّ أَنْتَ رَبِّي، لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي، فَاغْفِرْ لِي، فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ۔",
        englishTranslation = "O Allah, You are my Lord, there is none worthy of worship but You. You created me and I am Your servant, and I abide by Your covenant and promise as best as I can. I seek refuge in You from the evil of what I have done. I acknowledge Your favor upon me and I acknowledge my sin, so forgive me, for none forgives sins but You.",
        urduTranslation = "اے اللہ! تو ہی میرا رب ہے، تیرے سوا کوئی معبود نہیں...",
        noteEn = "Whoever recites it in the morning with conviction and dies before evening will be among the people of Paradise.",
        targetCount = 1,
        reference = "Sahih al-Bukhari 6306"
    ),
    AzkarItem(
        id = 111,
        category = AzkarCategory.MORNING,
        titleEn = "Blessings upon the Prophet ﷺ (10x)",
        titleUr = "درود شریف (10 مرتبہ)",
        arabicText = "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ، كَمَا صَلَّيْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ، إِنَّكَ حَمِيدٌ مَجِيدٌ۔ اللَّهُمَّ بَارِكْ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ، كَمَا بَارَكْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ، إِنَّكَ حَمِيدٌ مَجِيدٌ۔",
        englishTranslation = "O Allah, bestow Your blessings upon Muhammad and upon the family of Muhammad, as You bestowed blessings upon Ibrahim and upon the family of Ibrahim. Indeed, You are Praiseworthy and Glorious.",
        urduTranslation = "اے اللہ! رحمت نازل فرما محمد ﷺ پر اور ان کی آل پر...",
        noteEn = "Whoever recites it 10 times in the morning and evening will gain the intercession of the Prophet ﷺ on the Day of Judgment.",
        targetCount = 10,
        reference = "Majma' al-Zawa'id 10/120"
    ),
    AzkarItem(
        id = 112,
        category = AzkarCategory.MORNING,
        titleEn = "Kaffarat al-Majlis (Expiation)",
        titleUr = "ذکر اور مجلس کا اختتام",
        arabicText = "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ، أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا أَنْتَ، أَسْتَغْفِرُكَ وَأَتُوبُ إِلَيْكَ۔",
        englishTranslation = "Glory be to You, O Allah, and praise. I bear witness that there is none worthy of worship except You. I ask Your forgiveness and turn to You in repentance.",
        urduTranslation = "پاک ہے تو اے اللہ! اور تیرے ہی لیے تمام تعریفیں ہیں...",
        noteEn = "Expiates minor oversights during dhikr and gatherings.",
        targetCount = 1,
        reference = "Jami' at-Tirmidhi 3433"
    )
)

val eveningAzkarList = listOf(
    AzkarItem(
        id = 201,
        category = AzkarCategory.EVENING,
        titleEn = "Evening Kingdom Proclamation",
        titleUr = "شام کے وقت اللہ کی بادشاہی کا اقرار",
        arabicText = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا هُوَ، وَإِلَيْهِ الْمَصِيرُ۔",
        englishTranslation = "We have entered the evening and the kingdom belongs to Allah; all praise is due to Allah. None has the right to be worshipped except Him, and unto Him is our return.",
        urduTranslation = "ہم نے شام کی اور اللہ کے لیے شام ہوئی جو تمام بادشاہی کا مالک ہے...",
        noteEn = "Recite 3 times every evening.",
        targetCount = 3,
        reference = "Sahih Muslim 2723"
    ),
    AzkarItem(
        id = 202,
        category = AzkarCategory.EVENING,
        titleEn = "Steadfastness Upon Islam at Evening",
        titleUr = "دین اسلام اور کلمۂ اخلاص پر شام",
        arabicText = "أَمْسَيْنَا عَلَى فِطْرَةِ الْإِسْلَامِ، وَكَلِمَةِ الْإِخْلَاصِ، وَعَلَى دِينِ نَبِيِّنَا مُحَمَّدٍ ﷺ، وَعَلَى مِلَّةِ أَبِينَا إِبْرَاهِيمَ حَنِيفًا وَمَا كَانَ مِنَ الْمُشْرِكِينَ۔",
        englishTranslation = "We have entered the evening upon the natural religion of Islam, the word of sincere faith, the religion of our Prophet Muhammad ﷺ, and the faith of our father Ibrahim.",
        urduTranslation = "ہم نے شام کی اسلام کی فطرت، کلمۂ اخلاص...",
        noteEn = "Recite 3 times.",
        targetCount = 3,
        reference = "Musnad Ahmad 15360"
    ),
    AzkarItem(
        id = 203,
        category = AzkarCategory.EVENING,
        titleEn = "Praise Befitting Allah's Majesty",
        titleUr = "اللہ کے شایانِ شان حمد",
        arabicText = "يَا رَبِّي، لَكَ الْحَمْدُ كَمَا يَنْبَغِي لِجَلَالِ وَجْهِكَ وَعَظِيمِ سُلْطَانِكَ۔",
        englishTranslation = "O my Lord! All praise is due to You as befits the majesty of Your Countenance and the greatness of Your Authority.",
        urduTranslation = "اے میرے پروردگار! تیرے ہی لیے حمد ہے...",
        noteEn = "Recite 3 times evening.",
        targetCount = 3,
        reference = "Sunan Ibn Majah 3801"
    ),
    AzkarItem(
        id = 204,
        category = AzkarCategory.EVENING,
        titleEn = "Contentment with Allah and His Messenger",
        titleUr = "اللہ، اسلام اور رسول ﷺ پر رضامندی",
        arabicText = "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالْإِسْلَامِ دِينًا، وَبِمُحَمَّدٍ ﷺ نَبِيًّا وَرَسُولًا۔",
        englishTranslation = "I am pleased with Allah as my Lord, with Islam as my religion, and with Muhammad ﷺ as my Prophet and Messenger.",
        urduTranslation = "میں اللہ کے رب ہونے، اسلام کے دین ہونے...",
        noteEn = "Recite 3 times every evening.",
        targetCount = 3,
        reference = "Sunan Abi Dawud 1529"
    ),
    AzkarItem(
        id = 205,
        category = AzkarCategory.EVENING,
        titleEn = "Protection from Nocturnal Creatures & Harm",
        titleUr = "شام کے وقت مخلوق کے شر اور کیڑے مکوڑوں سے پناہ",
        arabicText = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ۔",
        englishTranslation = "I seek refuge in the perfect words of Allah from the evil of what He has created.",
        urduTranslation = "میں پناہ لیتا ہوں اللہ کے تمام تر کلمات کے ذریعے...",
        noteEn = "Whoever recites this 3 times in the evening, no scorpion or harmful creature will harm them during the night.",
        targetCount = 3,
        reference = "Sahih Muslim 2709"
    ),
    AzkarItem(
        id = 206,
        category = AzkarCategory.EVENING,
        titleEn = "Complete Immunity Until Morning",
        titleUr = "شام سے صبح تک ہر برائی سے امان",
        arabicText = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ، وَهُوَ السَّمِيعُ الْعَلِيمُ۔",
        englishTranslation = "In the name of Allah, with Whose name nothing on earth or in the heavens can cause harm, and He is the All-Hearing, the All-Knowing.",
        urduTranslation = "اللہ کے بابرکت نام سے جس کے نام کے ساتھ زمین اور آسمان کی کوئی چیز نقصان نہیں دے سکتی...",
        noteEn = "Recite 3 times evening. Shields against unexpected evil until the morning.",
        targetCount = 3,
        reference = "Sunan Abi Dawud 5088"
    ),
    AzkarItem(
        id = 207,
        category = AzkarCategory.EVENING,
        titleEn = "Refuge from Sorrow and Despair",
        titleUr = "غم اور قرض سے پناہ",
        arabicText = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ، وَأَعُوذُ بِكَ مِنَ الْعَجْزِ وَالْكَسَلِ، وَأَعُوذُ بِكَ مِنْ الْجُبْنِ وَالْبُخْلِ، وَأَعُوذُ بِكَ مِنْ غَلَبَةِ الدَّيْنِ وَقَهْرِ الرِّجَالِ۔",
        englishTranslation = "O Allah, I seek refuge in You from grief and sadness, weakness and laziness, cowardice and stinginess, and from the burden of debt and oppression of people.",
        urduTranslation = "اے اللہ! میں تیری پناہ مانگتا ہوں تفکرات اور صدمات سے...",
        noteEn = "Recite 3 times.",
        targetCount = 3,
        reference = "Sunan Abi Dawud 1555"
    ),
    AzkarItem(
        id = 208,
        category = AzkarCategory.EVENING,
        titleEn = "Supplication for Physical Health",
        titleUr = "جسمانی صحت اور عذاب قبر سے حفاظت",
        arabicText = "اللَّهُمَّ عَافِنِي فِي بَدَنِي، اللَّهُمَّ عَافِنِي فِي سَمْعِي، اللَّهُمَّ عَافِنِي فِي بَصَرِي، لَا إِلَهَ إِلَّا أَنْتَ۔ اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْكُفْرِ وَالْفَقْرِ، وَأَعُوذُ بِكَ مِنْ عَذَابِ الْقَبْرِ، لَا إِلَهَ إِلَّا أَنْتَ۔",
        englishTranslation = "O Allah, grant health to my body, hearing, and sight. None has the right to be worshipped but You.",
        urduTranslation = "اے اللہ! میرے بدن کو، میرے کانوں کو اور میری آنکھوں کو سلامتی عطا فرما...",
        noteEn = "Recite 3 times in the evening.",
        targetCount = 3,
        reference = "Sunan Abi Dawud 5090"
    ),
    AzkarItem(
        id = 209,
        category = AzkarCategory.EVENING,
        titleEn = "Sayyid al-Istighfar (Evening)",
        titleUr = "سید الاستغفار (شام کا ورد)",
        arabicText = "اللَّهُمَّ أَنْتَ رَبِّي، لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي، فَاغْفِرْ لِي، فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ۔",
        englishTranslation = "O Allah, You are my Lord, there is none worthy of worship but You. You created me and I am Your servant, and I abide by Your covenant and promise as best as I can. Forgive me, for none forgives sins but You.",
        urduTranslation = "اے اللہ! تو ہی میرا پروردگار ہے...",
        noteEn = "Whoever recites it in the evening and dies that night will be among the people of Paradise.",
        targetCount = 1,
        reference = "Sahih al-Bukhari 6306"
    ),
    AzkarItem(
        id = 210,
        category = AzkarCategory.EVENING,
        titleEn = "Sufficiency of Allah in All Affairs",
        titleUr = "اللہ تعالیٰ کا ہر معاملے میں کفایت کرنا",
        arabicText = "حَسْبِيَ اللَّهُ لَا إِلَهَ إِلَّا هُوَ، عَلَيْهِ تَوَكَّلْتُ، وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ۔",
        englishTranslation = "Allah is sufficient for me. There is none worthy of worship but Him. Upon Him I rely, and He is the Lord of the Mighty Throne.",
        urduTranslation = "مجھے اللہ ہی کافی ہے...",
        noteEn = "Whoever recites it 7 times in the morning and evening, Allah will suffice them in all their worldly and afterlife concerns.",
        targetCount = 7,
        reference = "Sunan Abi Dawud 5081"
    ),
    AzkarItem(
        id = 211,
        category = AzkarCategory.EVENING,
        titleEn = "Blessings upon the Prophet ﷺ (10x)",
        titleUr = "درود شریف (10 مرتبہ)",
        arabicText = "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ، كَمَا صَلَّيْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ، إِنَّكَ حَمِيدٌ مَجِيدٌ۔ اللَّهُمَّ بَارِكْ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ، كَمَا بَارَكْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ، إِنَّكَ حَمِيدٌ مَجِيدٌ۔",
        englishTranslation = "O Allah, send blessings upon Muhammad and upon the family of Muhammad, as You sent blessings upon Ibrahim and upon the family of Ibrahim. Indeed, You are Praiseworthy and Glorious.",
        urduTranslation = "اے اللہ! رحمت و برکت نازل فرما...",
        noteEn = "Recite 10 times in the evening.",
        targetCount = 10,
        reference = "Majma' al-Zawa'id 10/120"
    ),
    AzkarItem(
        id = 212,
        category = AzkarCategory.EVENING,
        titleEn = "Ayat al-Kursi (The Verse of the Throne)",
        titleUr = "آیت الکرسی (رات بھر شیطان سے حفاظت)",
        arabicText = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ۔",
        englishTranslation = "Allah! There is no deity except Him, the Ever-Living, the Sustainer of all existence. Neither drowsiness overtakes Him nor sleep. To Him belongs whatever is in the heavens and whatever is on the earth. Who is it that can intercede with Him except by His permission? He knows what is before them and what will be after them, and they encompass not a thing of His knowledge except for what He wills. His Kursi extends over the heavens and the earth, and their preservation tires Him not. And He is the Most High, the Most Great.",
        urduTranslation = "اللہ وہ معبود برحق ہے جس کے سوا کوئی عبادت کے لائق نہیں...",
        noteEn = "Whoever recites it in the evening will remain protected by a guardian from Allah until the morning, and no devil will approach them.",
        targetCount = 1,
        reference = "Sahih al-Bukhari 2311"
    )
)

val postSalahAzkarList = listOf(
    AzkarItem(
        id = 301,
        category = AzkarCategory.POST_SALAH,
        titleEn = "Astaghfirullah & Allahumma Antas-Salam",
        titleUr = "استغفار و سلامتی کی دعا بعد از نماز",
        arabicText = "أَسْتَغْفِرُ اللَّهَ، أَسْتَغْفِرُ اللَّهَ، أَسْتَغْفِرُ اللَّهَ۔ اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ۔",
        englishTranslation = "I seek forgiveness from Allah (3 times). O Allah, You are Peace and from You comes peace. Blessed are You, O Possessor of Majesty and Honor.",
        urduTranslation = "میں اللہ سے بخشش مانگتا ہوں (3 بار)۔ اے اللہ! تو ہی سلامتی والا ہے اور تیری ہی طرف سے سلامتی ہے، تو بڑی برکت والا ہے اے عظمت اور بزرگی والے۔",
        noteEn = "Recite 3 times immediately after saying the Salam ending the obligatory prayer.",
        noteUr = "فرض نماز کے سلام پھیرنے کے بعد فوراً پڑھنا مسنون ہے۔",
        targetCount = 3,
        reference = "Sahih Muslim 591"
    ),
    AzkarItem(
        id = 302,
        category = AzkarCategory.POST_SALAH,
        titleEn = "Ayat al-Kursi (Verse of the Throne)",
        titleUr = "آیت الکرسی بعد از فرض نماز",
        arabicText = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ۔",
        englishTranslation = "Allah! There is no deity except Him, the Ever-Living, the Sustainer of all existence. Neither drowsiness overtakes Him nor sleep. To Him belongs whatever is in the heavens and whatever is on the earth. Who is it that can intercede with Him except by His permission? He knows what is before them and what will be after them, and they encompass not a thing of His knowledge except for what He wills. His Kursi extends over the heavens and the earth, and their preservation tires Him not. And He is the Most High, the Most Great.",
        urduTranslation = "اللہ تعالیٰ وہ معبود برحق ہے جس کے سوا کوئی عبادت کے لائق نہیں، وہ زندہ اور سب کو قائم رکھنے والا ہے۔ نہ اسے اونگھ آتی ہے نہ نیند...",
        noteEn = "Whoever recites it after every prescribed prayer, nothing stands between him and entering Paradise except death.",
        noteUr = "جو شخص ہر فرض نماز کے بعد آیت الکرسی پڑھے، اسے جنت میں داخل ہونے سے سوائے موت کے کوئی چیز نہیں روکتی۔",
        targetCount = 1,
        reference = "Sunan an-Nasa'i al-Kubra 9928"
    ),
    AzkarItem(
        id = 303,
        category = AzkarCategory.POST_SALAH,
        titleEn = "Tasbih Fatimi: SubhanAllah (33x)",
        titleUr = "تسبیح فاطمہ: سبحان اللہ (33 بار)",
        arabicText = "سُبْحَانَ اللَّهِ",
        englishTranslation = "Glory be to Allah, pure and free from all defects.",
        urduTranslation = "اللہ پاک ہر عیب اور نقص سے پاک ہے۔",
        noteEn = "Recite 33 times after every obligatory prayer.",
        noteUr = "ہر فرض نماز کے بعد 33 مرتبہ پڑھیں۔",
        targetCount = 33,
        reference = "Sahih Muslim 597"
    ),
    AzkarItem(
        id = 304,
        category = AzkarCategory.POST_SALAH,
        titleEn = "Tasbih Fatimi: Alhamdulillah (33x)",
        titleUr = "تسبیح فاطمہ: الحمد للہ (33 بار)",
        arabicText = "الْحَمْدُ لِلَّهِ",
        englishTranslation = "All praise and gratitude belong exclusively to Allah.",
        urduTranslation = "تمام تعریفیں اور شکر صرف اللہ ہی کے لیے ہے۔",
        noteEn = "Recite 33 times after every obligatory prayer.",
        noteUr = "ہر فرض نماز کے بعد 33 مرتبہ پڑھیں۔",
        targetCount = 33,
        reference = "Sahih Muslim 597"
    ),
    AzkarItem(
        id = 305,
        category = AzkarCategory.POST_SALAH,
        titleEn = "Tasbih Fatimi: Allahu Akbar (34x)",
        titleUr = "تسبیح فاطمہ: اللہ اکبر (34 بار)",
        arabicText = "اللَّهُ أَكْبَرُ",
        englishTranslation = "Allah is the Greatest over all creation.",
        urduTranslation = "اللہ سب سے بڑا ہے۔",
        noteEn = "Recite 34 times, or recite 33 times and seal the 100th count with Kalima Tawheed.",
        noteUr = "34 مرتبہ پڑھیں یا 33 بار پڑھ کر 100 کا عدد کلمہ توحید سے پورا کریں۔",
        targetCount = 34,
        reference = "Sahih Muslim 597"
    ),
    AzkarItem(
        id = 306,
        category = AzkarCategory.POST_SALAH,
        titleEn = "Completing the Hundred with Kalima",
        titleUr = "تسبیح کے 100 مکمل کرنے کی دعا",
        arabicText = "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ۔",
        englishTranslation = "None has the right to be worshipped except Allah alone, without partner. To Him belongs all dominion and to Him belongs all praise, and He has power over all things.",
        urduTranslation = "اللہ کے سوا کوئی معبود نہیں، وہ اکیلا ہے، اس کا کوئی شریک نہیں، اسی کی بادشاہی ہے اور اسی کی تعریف، اور وہ ہر چیز پر قادر ہے۔",
        noteEn = "Whoever recites this after the 33-33-33 Tasbih, their sins will be forgiven even if they were as vast as the foam of the sea.",
        noteUr = "جو تسبیح فاطمہ کے بعد یہ پڑھے گا، اس کے تمام گناہ معاف کر دیے جائیں گے خواہ وہ سمندر کی جھاگ کے برابر ہی کیوں نہ ہوں۔",
        targetCount = 1,
        reference = "Sahih Muslim 597"
    ),
    AzkarItem(
        id = 307,
        category = AzkarCategory.POST_SALAH,
        titleEn = "Tawheed & Protection Declaration",
        titleUr = "لا الہ الا اللہ وحدہ لا شریک لہ... لا مانع لما أعطیت",
        arabicText = "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ، وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، اللَّهُمَّ لَا مَانِعَ لِمَا أَعْطَيْتَ، وَلَا مُعْطِيَ لِمَا مَنَعْتَ، وَلَا يَنْفَعُ ذَا الْجَدِّ مِنْكَ الْجَدُّ۔",
        englishTranslation = "There is no god but Allah alone, Who has no partner. His is the sovereignty and His is the praise, and He is able to do all things. O Allah, none can withhold what You have given, and none can give what You have withheld, nor will the wealth of the wealthy avail them against You.",
        urduTranslation = "اللہ کے سوا کوئی عبادت کے لائق نہیں، وہ اکیلا ہے... اے اللہ! جو تو عطا فرمائے اسے کوئی روکنے والا نہیں، اور جسے تو روک دے اسے کوئی دینے والا نہیں...",
        noteEn = "Recited after concluding the obligatory prayer.",
        noteUr = "ہر فرض نماز کے بعد پڑھنا مسنون ہے۔",
        targetCount = 1,
        reference = "Sahih al-Bukhari 844"
    ),
    AzkarItem(
        id = 308,
        category = AzkarCategory.POST_SALAH,
        titleEn = "Dua for Aid in Remembrance & Gratitude",
        titleUr = "ذکر، شکر اور بہترین عبادت کی توفیق کی دعا",
        arabicText = "اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ، وَشُكْرِكَ، وَحُسْنِ عِبَادَتِكَ۔",
        englishTranslation = "O Allah, help me to remember You, to thank You, and to worship You in the best manner.",
        urduTranslation = "اے اللہ! میری مدد فرما اپنے ذکر پر، اپنے شکر پر، اور اپنی بہترین بندگی پر۔",
        noteEn = "The Prophet ﷺ advised Mu'adh ibn Jabal (RA) never to leave this supplication after any prayer.",
        noteUr = "نبی کریم ﷺ نے حضرت معاذ بن جبل ؓ کو وصیت فرمائی کہ ہر نماز کے بعد یہ دعا کبھی نہ چھوڑنا۔",
        targetCount = 1,
        reference = "Sunan Abi Dawud 1522"
    ),
    AzkarItem(
        id = 309,
        category = AzkarCategory.POST_SALAH,
        titleEn = "Mu'awwidhat (Surah Al-Ikhlas, Al-Falaq, An-Nas)",
        titleUr = "معوذات (سورۃ الاخلاص، الفلق، الناس)",
        arabicText = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ۝ قُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ ۝ لَمْ يَلِدْ وَلَمْ يُولَدْ ۝ وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ ۝\n\nبِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ۝ قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ۝ مِن شَرِّ مَا خَلَقَ ۝ وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ ۝ وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ۝ وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ ۝\n\nبِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ۝ قُلْ أَعُوذُ بِرَبِّ النَّاسِ ۝ مَلِكِ النَّاسِ ۝ إِلَٰهِ النَّاسِ ۝ مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ۝ الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ ۝ مِنَ الْجِنَّةِ وَالنَّاسِ ۝",
        englishTranslation = "Recite Surah Al-Ikhlas, Surah Al-Falaq, and Surah An-Nas for divine protection from all evil, jealousy, and whispers.",
        urduTranslation = "تینوں قل مبارک (سورۃ الاخلاص، سورۃ الفلق، اور سورۃ الناس) تمام شرور، جادو اور حسد سے حفاظت کے لیے۔",
        noteEn = "The Messenger of Allah ﷺ ordered the recitation of the Mu'awwidhat after every prayer (recite 3 times after Fajr and Maghrib).",
        noteUr = "رسول اللہ ﷺ نے ہر نماز کے بعد معوذات پڑھنے کا حکم فرمایا (فجر اور مغرب کے بعد 3، 3 بار پڑھنا مسنون ہے)۔",
        targetCount = 1,
        reference = "Sunan Abi Dawud 1523"
    ),
    AzkarItem(
        id = 310,
        category = AzkarCategory.POST_SALAH,
        titleEn = "Beneficial Knowledge & Pure Sustenance (Post-Fajr)",
        titleUr = "علم نافع، پاکیزہ رزق اور مقبول عمل کی دعا (بعد از فجر)",
        arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا، وَرِزْقًا طَيِّبًا، وَعَمَلًا مُتَقَبَّلًا۔",
        englishTranslation = "O Allah, I ask You for beneficial knowledge, good and pure sustenance, and accepted deeds.",
        urduTranslation = "اے اللہ! میں تجھ سے نفع بخش علم، پاکیزہ روزی اور قبول ہونے والے عمل کا سوال کرتا ہوں۔",
        noteEn = "Recited especially after the Salam of the Fajr prayer.",
        noteUr = "خصوصاً نماز فجر کے سلام کے بعد پڑھنا مسنون ہے۔",
        targetCount = 1,
        reference = "Sunan Ibn Majah 925"
    )
)

data class SubahShamZikrItem(
    val id: Int,
    val titleEn: String,
    val titleUr: String,
    val morningArabic: String,
    val eveningArabic: String? = null,
    val morningNoteUr: String? = null,
    val eveningNoteUr: String? = null,
    val targetCount: Int,
    val reference: String
)

val subahShamZikrList: List<SubahShamZikrItem> = morningAzkarList.map { item ->
    SubahShamZikrItem(
        id = item.id - 100,
        titleEn = item.titleEn,
        titleUr = item.titleUr,
        morningArabic = item.arabicText,
        eveningArabic = eveningAzkarList.find { it.id - 200 == item.id - 100 }?.arabicText,
        morningNoteUr = item.noteUr,
        eveningNoteUr = eveningAzkarList.find { it.id - 200 == item.id - 100 }?.noteUr,
        targetCount = item.targetCount,
        reference = item.reference
    )
}
