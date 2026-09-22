package com.example.domain.quran

data class SurahInfo(
    val number: Int,
    val nameEnglish: String,
    val nameArabic: String,
    val englishMeaning: String,
    val totalVerses: Int,
    val startingJuz: Int
)

data class JuzInfo(
    val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val startSurahNumber: Int,
    val startVerseNumber: Int,
    val endSurahNumber: Int,
    val endVerseNumber: Int
)

object QuranMetadata {

    fun getJuzNumber(surah: Int, verse: Int): Int {
        return when {
            surah == 1 -> 1
            surah == 2 -> when {
                verse < 142 -> 1
                verse < 253 -> 2
                else -> 3
            }
            surah == 3 -> if (verse < 93) 3 else 4
            surah == 4 -> when {
                verse < 24 -> 4
                verse < 148 -> 5
                else -> 6
            }
            surah == 5 -> if (verse < 82) 6 else 7
            surah == 6 -> if (verse < 111) 7 else 8
            surah == 7 -> if (verse < 88) 8 else 9
            surah == 8 -> if (verse < 41) 9 else 10
            surah == 9 -> if (verse < 93) 10 else 11
            surah == 10 -> 11
            surah == 11 -> if (verse < 6) 11 else 12
            surah == 12 -> if (verse < 53) 12 else 13
            surah == 13 || surah == 14 -> 13
            surah == 15 -> 14
            surah == 16 -> 14
            surah == 17 -> 15
            surah == 18 -> if (verse < 75) 15 else 16
            surah == 19 || surah == 20 -> 16
            surah == 21 || surah == 22 -> 17
            surah == 23 || (surah == 24) || (surah == 25 && verse < 21) -> 18
            (surah == 25 && verse >= 21) || surah == 26 || (surah == 27 && verse < 56) -> 19
            (surah == 27 && verse >= 56) || surah == 28 || (surah == 29 && verse < 46) -> 20
            (surah == 29 && verse >= 46) || surah == 30 || surah == 31 || surah == 32 || (surah == 33 && verse < 31) -> 21
            (surah == 33 && verse >= 31) || surah == 34 || surah == 35 || (surah == 36 && verse < 28) -> 22
            (surah == 36 && verse >= 28) || surah == 37 || surah == 38 || (surah == 39 && verse < 32) -> 23
            (surah == 39 && verse >= 32) || surah == 40 || (surah == 41 && verse < 47) -> 24
            (surah == 41 && verse >= 47) || surah == 42 || surah == 43 || surah == 44 || surah == 45 -> 25
            surah in 46..50 || (surah == 51 && verse < 31) -> 26
            (surah == 51 && verse >= 31) || surah in 52..57 -> 27
            surah in 58..66 -> 28
            surah in 67..77 -> 29
            surah in 78..114 -> 30
            else -> 1
        }
    }

    fun getSurahInfo(number: Int): SurahInfo {
        return ALL_SURAHS.find { it.number == number } ?: SurahInfo(
            number = number,
            nameEnglish = "Surah $number",
            nameArabic = "سورة $number",
            englishMeaning = "Chapter $number",
            totalVerses = 0,
            startingJuz = getJuzNumber(number, 1)
        )
    }

    val ALL_JUZ = listOf(
        JuzInfo(1, "الم", "Alif-Lam-Mim", 1, 1, 2, 141),
        JuzInfo(2, "سَيَقُولُ", "Sayaqool", 2, 142, 2, 252),
        JuzInfo(3, "تِلْكَ الرُّسُلُ", "Tilkal Rusul", 2, 253, 3, 92),
        JuzInfo(4, "لَنْ تَنَالُوا", "Lan Tanaalu", 3, 93, 4, 23),
        JuzInfo(5, "وَالْمُحْصَنَاتُ", "Wal Muhsanat", 4, 24, 4, 147),
        JuzInfo(6, "لَا يُحِبُّ اللَّهُ", "La Yuhibbullah", 4, 148, 5, 81),
        JuzInfo(7, "وَإِذَا سَمِعُوا", "Wa Iza Samiu", 5, 82, 6, 110),
        JuzInfo(8, "وَلَوْ أَنَّنَا", "Wa Law Annana", 6, 111, 7, 87),
        JuzInfo(9, "قَالَ الْمَلَأُ", "Qalal Malao", 7, 88, 8, 40),
        JuzInfo(10, "وَاعْلَمُوا", "Wa'lamu", 8, 41, 9, 92),
        JuzInfo(11, "يَعْتَذِرُونَ", "Ya'tadhirun", 9, 93, 11, 5),
        JuzInfo(12, "وَمَا مِنْ دَابَّةٍ", "Wa Mamin Da'abbah", 11, 6, 12, 52),
        JuzInfo(13, "وَمَا أُبَرِّئُ", "Wa Ma Ubarri'u", 12, 53, 14, 52),
        JuzInfo(14, "رُبَمَا", "Rubama", 15, 1, 16, 128),
        JuzInfo(15, "سُبْحَانَ الَّذِي", "Subhanalladhi", 17, 1, 18, 74),
        JuzInfo(16, "قَالَ أَلَمْ", "Qala Alam", 18, 75, 20, 135),
        JuzInfo(17, "اقْتَرَبَ", "Iqtaraba", 21, 1, 22, 78),
        JuzInfo(18, "قَدْ أَفْلَحَ", "Qad Aflaha", 23, 1, 25, 20),
        JuzInfo(19, "وَقَالَ الَّذِينَ", "Wa Qalalladhina", 25, 21, 27, 55),
        JuzInfo(20, "أَمَّنْ خَلَقَ", "Amman Khalaqa", 27, 56, 29, 45),
        JuzInfo(21, "اتْلُ مَا أُوحِيَ", "Utlu Ma Oohiya", 29, 46, 33, 30),
        JuzInfo(22, "وَمَنْ يَقْنُتْ", "Wa Man Yaqnut", 33, 31, 36, 27),
        JuzInfo(23, "وَمَا لِيَ", "Wa Maliya", 36, 28, 39, 31),
        JuzInfo(24, "فَمَنْ أَظْلَمُ", "Faman Azlamu", 39, 32, 41, 46),
        JuzInfo(25, "إِلَيْهِ يُرَدُّ", "Ilayhi Yuraddu", 41, 47, 45, 37),
        JuzInfo(26, "حم", "Ha-Meem", 46, 1, 51, 30),
        JuzInfo(27, "قَالَ فَمَا خَطْبُكُمْ", "Qala Fama Khatbukum", 51, 31, 57, 29),
        JuzInfo(28, "قَدْ سَمِعَ اللَّهُ", "Qad Sami'allah", 58, 1, 66, 12),
        JuzInfo(29, "تَبَارَكَ الَّذِي", "Tabarakalladhi", 67, 1, 77, 50),
        JuzInfo(30, "عَمَّ", "'Amma", 78, 1, 114, 6)
    )

    val ALL_SURAHS = listOf(
        SurahInfo(1, "Al-Fatiha", "الفاتحة", "The Opening", 7, 1),
        SurahInfo(2, "Al-Baqarah", "البقرة", "The Cow", 286, 1),
        SurahInfo(3, "Aal-E-Imran", "آل عمران", "The Family of Imran", 200, 3),
        SurahInfo(4, "An-Nisa", "النساء", "The Women", 176, 4),
        SurahInfo(5, "Al-Ma'idah", "المائدة", "The Table Spread", 120, 6),
        SurahInfo(6, "Al-An'am", "الأنعام", "The Cattle", 165, 7),
        SurahInfo(7, "Al-A'raf", "الأعراف", "The Heights", 206, 8),
        SurahInfo(8, "Al-Anfal", "الأنفال", "The Spoils of War", 75, 9),
        SurahInfo(9, "At-Tawbah", "التوبة", "The Repentance", 129, 10),
        SurahInfo(10, "Yunus", "يونس", "Jonah", 109, 11),
        SurahInfo(11, "Hud", "هود", "Hud", 123, 11),
        SurahInfo(12, "Yusuf", "يوسف", "Joseph", 111, 12),
        SurahInfo(13, "Ar-Ra'd", "الرعد", "The Thunder", 43, 13),
        SurahInfo(14, "Ibrahim", "إبراهيم", "Abraham", 52, 13),
        SurahInfo(15, "Al-Hijr", "الحجر", "The Rocky Tract", 99, 14),
        SurahInfo(16, "An-Nahl", "النحل", "The Bee", 128, 14),
        SurahInfo(17, "Al-Isra", "الإسراء", "The Night Journey", 111, 15),
        SurahInfo(18, "Al-Kahf", "الكهف", "The Cave", 110, 15),
        SurahInfo(19, "Maryam", "مريم", "Mary", 98, 16),
        SurahInfo(20, "Ta-Ha", "طه", "Ta-Ha", 135, 16),
        SurahInfo(21, "Al-Anbiya", "الأنبياء", "The Prophets", 112, 17),
        SurahInfo(22, "Al-Hajj", "الحج", "The Pilgrimage", 78, 17),
        SurahInfo(23, "Al-Mu'minun", "المؤمنون", "The Believers", 118, 18),
        SurahInfo(24, "An-Nur", "النور", "The Light", 64, 18),
        SurahInfo(25, "Al-Furqan", "الفرقان", "The Criterion", 77, 18),
        SurahInfo(26, "Ash-Shu'ara", "الشعراء", "The Poets", 227, 19),
        SurahInfo(27, "An-Naml", "النمل", "The Ant", 93, 19),
        SurahInfo(28, "Al-Qasas", "القصص", "The Stories", 88, 20),
        SurahInfo(29, "Al-Ankabut", "العنكبوت", "The Spider", 69, 20),
        SurahInfo(30, "Ar-Rum", "الروم", "The Romans", 60, 21),
        SurahInfo(31, "Luqman", "لقمان", "Luqman", 34, 21),
        SurahInfo(32, "As-Sajdah", "السجدة", "The Prostration", 30, 21),
        SurahInfo(33, "Al-Ahzab", "الأحزاب", "The Combined Forces", 73, 21),
        SurahInfo(34, "Saba", "سبأ", "Sheba", 54, 22),
        SurahInfo(35, "Fatir", "فاطر", "The Originator", 45, 22),
        SurahInfo(36, "Ya-Sin", "يس", "Ya-Sin", 83, 22),
        SurahInfo(37, "As-Saffat", "الصافات", "Those Who Set The Ranks", 182, 23),
        SurahInfo(38, "Sad", "ص", "Sad", 88, 23),
        SurahInfo(39, "Az-Zumar", "الزمر", "The Groups", 75, 23),
        SurahInfo(40, "Ghafir", "غافر", "The Forgiver", 85, 24),
        SurahInfo(41, "Fussilat", "فصلت", "Explained in Detail", 54, 24),
        SurahInfo(42, "Ash-Shura", "الشورى", "The Consultation", 53, 25),
        SurahInfo(43, "Az-Zukhruf", "الزخرف", "The Gold Adornments", 89, 25),
        SurahInfo(44, "Ad-Dukhan", "الدخان", "The Smoke", 59, 25),
        SurahInfo(45, "Al-Jathiyah", "الجاثية", "The Crouching", 37, 25),
        SurahInfo(46, "Al-Ahqaf", "الأحقاف", "The Wind-Curved Sandhills", 35, 26),
        SurahInfo(47, "Muhammad", "محمد", "Muhammad", 38, 26),
        SurahInfo(48, "Al-Fath", "الفتح", "The Victory", 29, 26),
        SurahInfo(49, "Al-Hujurat", "الحجرات", "The Rooms", 18, 26),
        SurahInfo(50, "Qaf", "ق", "Qaf", 45, 26),
        SurahInfo(51, "Adh-Dhariyat", "الذاريات", "The Winnowing Winds", 60, 26),
        SurahInfo(52, "At-Tur", "الطور", "The Mount", 49, 27),
        SurahInfo(53, "An-Najm", "النجم", "The Star", 62, 27),
        SurahInfo(54, "Al-Qamar", "القمر", "The Moon", 55, 27),
        SurahInfo(55, "Ar-Rahman", "الرحمن", "The Beneficent", 78, 27),
        SurahInfo(56, "Al-Waqi'ah", "الواقعة", "The Inevitable", 96, 27),
        SurahInfo(57, "Al-Hadid", "الحديد", "The Iron", 29, 27),
        SurahInfo(58, "Al-Mujadila", "المجادلة", "The Pleading Woman", 22, 28),
        SurahInfo(59, "Al-Hashr", "الحشر", "The Exile", 24, 28),
        SurahInfo(60, "Al-Mumtahanah", "الممتحنة", "She That Is To Be Examined", 13, 28),
        SurahInfo(61, "As-Saff", "الصف", "The Ranks", 14, 28),
        SurahInfo(62, "Al-Jumu'ah", "الجمعة", "The Congregation (Friday)", 11, 28),
        SurahInfo(63, "Al-Munafiqun", "المنافقون", "The Hypocrites", 11, 28),
        SurahInfo(64, "At-Taghabun", "التغابن", "The Mutual Disillusion", 18, 28),
        SurahInfo(65, "At-Talaq", "الطلاق", "The Divorce", 12, 28),
        SurahInfo(66, "At-Tahrim", "التحريم", "The Prohibition", 12, 28),
        SurahInfo(67, "Al-Mulk", "الملك", "The Sovereignty", 30, 29),
        SurahInfo(68, "Al-Qalam", "القلم", "The Pen", 52, 29),
        SurahInfo(69, "Al-Haqqah", "الحاقة", "The Reality", 52, 29),
        SurahInfo(70, "Al-Ma'arij", "المعارج", "The Ascending Stairways", 44, 29),
        SurahInfo(71, "Nuh", "نوح", "Noah", 28, 29),
        SurahInfo(72, "Al-Jinn", "الجن", "The Jinn", 28, 29),
        SurahInfo(73, "Al-Muzzammil", "المزمل", "The Enshrouded One", 20, 29),
        SurahInfo(74, "Al-Muddaththir", "المدثر", "The Cloaked One", 56, 29),
        SurahInfo(75, "Al-Qiyamah", "القيامة", "The Resurrection", 40, 29),
        SurahInfo(76, "Al-Insan", "الإنسان", "The Man", 31, 29),
        SurahInfo(77, "Al-Mursalat", "المرسلات", "The Emissaries", 50, 29),
        SurahInfo(78, "An-Naba", "النبأ", "The Tidings", 40, 30),
        SurahInfo(79, "An-Nazi'at", "النازعات", "Those Who Drag Forth", 46, 30),
        SurahInfo(80, "'Abasa", "عبس", "He Frowned", 42, 30),
        SurahInfo(81, "At-Takwir", "التكوير", "The Overthrowing", 29, 30),
        SurahInfo(82, "Al-Infitar", "الانفطار", "The Cleaving", 19, 30),
        SurahInfo(83, "Al-Mutaffifin", "المطففين", "The Defrauding", 36, 30),
        SurahInfo(84, "Al-Inshiqaq", "الانشقاق", "The Splitting Open", 25, 30),
        SurahInfo(85, "Al-Buruj", "البروج", "The Mansions of the Stars", 22, 30),
        SurahInfo(86, "At-Tariq", "الطارق", "The Morning Star", 17, 30),
        SurahInfo(87, "Al-A'la", "الأعلى", "The Most High", 19, 30),
        SurahInfo(88, "Al-Ghashiyah", "الغاشية", "The Overwhelming", 26, 30),
        SurahInfo(89, "Al-Fajr", "الفجر", "The Dawn", 30, 30),
        SurahInfo(90, "Al-Balad", "البلد", "The City", 20, 30),
        SurahInfo(91, "Ash-Shams", "الشمس", "The Sun", 15, 30),
        SurahInfo(92, "Al-Layl", "الليل", "The Night", 21, 30),
        SurahInfo(93, "Ad-Duha", "الضحى", "The Morning Hours", 11, 30),
        SurahInfo(94, "Ash-Sharh", "الشرح", "The Relief", 8, 30),
        SurahInfo(95, "At-Tin", "التين", "The Fig", 8, 30),
        SurahInfo(96, "Al-'Alaq", "العلق", "The Clot", 19, 30),
        SurahInfo(97, "Al-Qadr", "القدر", "The Power", 5, 30),
        SurahInfo(98, "Al-Bayyinah", "البينة", "The Clear Proof", 8, 30),
        SurahInfo(99, "Az-Zalzalah", "الزلزلة", "The Earthquake", 8, 30),
        SurahInfo(100, "Al-'Adiyat", "العاديات", "The Courser", 11, 30),
        SurahInfo(101, "Al-Qari'ah", "القارعة", "The Calamity", 11, 30),
        SurahInfo(102, "At-Takathur", "التكاثر", "The Rivalry in World Increase", 8, 30),
        SurahInfo(103, "Al-'Asr", "العصر", "The Declining Day", 3, 30),
        SurahInfo(104, "Al-Humazah", "الهمزة", "The Traducer", 9, 30),
        SurahInfo(105, "Al-Fil", "الفيل", "The Elephant", 5, 30),
        SurahInfo(106, "Quraysh", "قريش", "Quraysh", 4, 30),
        SurahInfo(107, "Al-Ma'un", "الماعون", "The Small Kindnesses", 7, 30),
        SurahInfo(108, "Al-Kawthar", "الكوثر", "The Abundance", 3, 30),
        SurahInfo(109, "Al-Kafirun", "الكافرون", "The Disbelievers", 6, 30),
        SurahInfo(110, "An-Nasr", "النصر", "The Divine Support", 3, 30),
        SurahInfo(111, "Al-Masad", "المسد", "The Palm Fiber", 5, 30),
        SurahInfo(112, "Al-Ikhlas", "الإخلاص", "The Sincerity", 4, 30),
        SurahInfo(113, "Al-Falaq", "الفلق", "The Daybreak", 5, 30),
        SurahInfo(114, "An-Nas", "الناس", "Mankind", 6, 30)
    )
}
