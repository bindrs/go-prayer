package com.example.data.remote

import android.util.Log
import com.example.data.local.entities.Ayat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class QuranAyahItem(
    val id: Int,
    val surahNumber: Int,
    val surahNameEnglish: String,
    val surahNameArabic: String,
    val verseNumber: Int,
    val arabicText: String,
    val transliteration: String,
    val englishTranslation: String,
    val urduTranslation: String = "",
    val juzNumber: Int = 1
)

object UmmahApiClient {
    private const val TAG = "UmmahApiClient"
    private const val BASE_URL = "https://ummahapi.com"
    private const val API_KEY = "umh_0e646423e3651ec633951e2002b48b2070d61390"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .build()

    suspend fun fetchDailyQuranPortion(targetLines: Int, surahNumber: Int = 1, startAyah: Int = 1): List<QuranAyahItem> = withContext(Dispatchers.IO) {
        // First Primary API: AlQuranCloud API for full authentic Uthmani text and translations
        try {
            val url = "https://api.alquran.cloud/v1/surah/$surahNumber/editions/quran-uthmani,en.sahih"
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val bodyStr = response.body?.string()
                if (!bodyStr.isNullOrEmpty()) {
                    val root = JSONObject(bodyStr)
                    if (root.optInt("code") == 200 && root.optString("status") == "OK") {
                        val dataArray = root.optJSONArray("data")
                        if (dataArray != null && dataArray.length() >= 2) {
                            val arObj = dataArray.getJSONObject(0)
                            val enObj = dataArray.getJSONObject(1)
                            val arAyahs = arObj.optJSONArray("ayahs")
                            val enAyahs = enObj.optJSONArray("ayahs")

                            if (arAyahs != null && arAyahs.length() > 0) {
                                val surahNameEn = arObj.optString("englishName", getSurahNameEn(surahNumber))
                                val surahNameAr = arObj.optString("name", getSurahNameAr(surahNumber))
                                val result = mutableListOf<QuranAyahItem>()

                                for (i in 0 until arAyahs.length()) {
                                    val arItem = arAyahs.getJSONObject(i)
                                    val enItem = enAyahs?.optJSONObject(i)
                                    val vNum = arItem.optInt("numberInSurah", i + 1)
                                    val arText = arItem.optString("text", "")
                                    val enText = enItem?.optString("text", "") ?: ""
                                    val juzNum = arItem.optInt("juz", com.example.domain.quran.QuranMetadata.getJuzNumber(surahNumber, vNum))

                                    result.add(
                                        QuranAyahItem(
                                            id = (surahNumber * 1000) + vNum,
                                            surahNumber = surahNumber,
                                            surahNameEnglish = surahNameEn,
                                            surahNameArabic = surahNameAr,
                                            verseNumber = vNum,
                                            arabicText = arText,
                                            transliteration = "",
                                            englishTranslation = enText,
                                            juzNumber = juzNum
                                        )
                                    )
                                }

                                if (result.isNotEmpty()) {
                                    val candidateList = if (startAyah > 1) {
                                        val filtered = result.filter { it.verseNumber >= startAyah }
                                        if (filtered.isNotEmpty()) filtered else result
                                    } else {
                                        result
                                    }
                                    // For Surah Al-Kahf (18) or when targetLines >= full size, return complete candidate list
                                    if (surahNumber == 18 || targetLines >= candidateList.size) {
                                        return@withContext candidateList
                                    } else {
                                        return@withContext candidateList.take(Math.max(targetLines, 1))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "AlQuranCloud API call failed, trying secondary UmmahAPI: ${e.message}")
        }

        // Secondary API: UmmahAPI
        try {
            val url = "$BASE_URL/api/v1/quran/surah/$surahNumber?apikey=$API_KEY"
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val bodyStr = response.body?.string()
                if (!bodyStr.isNullOrEmpty()) {
                    val root = JSONObject(bodyStr)
                    if (root.optBoolean("success")) {
                        val dataObj = root.optJSONObject("data")
                        val versesArray = dataObj?.optJSONArray("verses") ?: root.optJSONArray("data")

                        if (versesArray != null && versesArray.length() > 0) {
                            val result = mutableListOf<QuranAyahItem>()
                            val surahNameEn = dataObj?.optString("name_english") ?: getSurahNameEn(surahNumber)
                            val surahNameAr = dataObj?.optString("name_arabic") ?: getSurahNameAr(surahNumber)

                            for (i in 0 until versesArray.length()) {
                                val item = versesArray.getJSONObject(i)
                                val vNum = item.optInt("verse_number", i + 1)
                                val arText = item.optString("text_uthmani", item.optString("arabic", item.optString("text", "")))
                                val translit = item.optString("transliteration", "")
                                val enTrans = item.optString("translation_en", item.optString("english", ""))
                                val juzNum = com.example.domain.quran.QuranMetadata.getJuzNumber(surahNumber, vNum)

                                result.add(
                                    QuranAyahItem(
                                        id = (surahNumber * 1000) + vNum,
                                        surahNumber = surahNumber,
                                        surahNameEnglish = surahNameEn,
                                        surahNameArabic = surahNameAr,
                                        verseNumber = vNum,
                                        arabicText = if (arText.isNotBlank()) arText else getFallbackAyahText(surahNumber, vNum),
                                        transliteration = translit,
                                        englishTranslation = enTrans,
                                        juzNumber = juzNum
                                    )
                                )
                            }
                            if (result.isNotEmpty()) {
                                val candidateList = if (startAyah > 1) {
                                    val filtered = result.filter { it.verseNumber >= startAyah }
                                    if (filtered.isNotEmpty()) filtered else result
                                } else {
                                    result
                                }
                                val count = if (surahNumber == 18) candidateList.size else Math.min(targetLines, candidateList.size)
                                return@withContext candidateList.take(count)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "UmmahAPI call fallback to offline dataset: ${e.message}")
        }

        // Return local offline Quran portion dataset
        return@withContext getOfflineQuranPortion(surahNumber, if (surahNumber == 18) 110 else targetLines, startAyah)
    }

    fun getOfflineQuranPortion(surahNumber: Int, targetLines: Int, startAyah: Int = 1): List<QuranAyahItem> {
        val fullList = when (surahNumber) {
            1 -> FATIHA_AYAH_LIST
            67 -> MULK_AYAH_LIST
            36 -> YASIN_AYAH_LIST
            18 -> KAHF_AYAH_LIST
            55 -> RAHMAN_AYAH_LIST
            112 -> IKHLAS_AYAH_LIST
            else -> BAQARAH_SAMPLE_AYAH_LIST
        }
        val candidate = if (startAyah > 1) {
            val filtered = fullList.filter { it.verseNumber >= startAyah }
            if (filtered.isNotEmpty()) filtered else fullList
        } else {
            fullList
        }
        val count = Math.min(targetLines, candidate.size)
        return candidate.take(count)
    }

    private fun getSurahNameEn(num: Int): String {
        return com.example.domain.quran.QuranMetadata.getSurahInfo(num).nameEnglish
    }

    private fun getSurahNameAr(num: Int): String {
        return com.example.domain.quran.QuranMetadata.getSurahInfo(num).nameArabic
    }

    private fun getFallbackAyahText(surah: Int, verse: Int): String {
        return "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"
    }

    private val FATIHA_AYAH_LIST = listOf(
        QuranAyahItem(1001, 1, "Al-Fatiha", "الفاتحة", 1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "Bismillahir-Rahmanir-Rahim", "In the name of Allah, the Entirely Merciful, the Especially Merciful.", juzNumber = 1),
        QuranAyahItem(1002, 1, "Al-Fatiha", "الفاتحة", 2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "Al-hamdu lillahi Rabbil-'alamin", "[All] praise is [due] to Allah, Lord of the worlds -", juzNumber = 1),
        QuranAyahItem(1003, 1, "Al-Fatiha", "الفاتحة", 3, "الرَّحْمَٰنِ الرَّحِيمِ", "Ar-Rahmanir-Rahim", "The Entirely Merciful, the Especially Merciful,", juzNumber = 1),
        QuranAyahItem(1004, 1, "Al-Fatiha", "الفاتحة", 4, "مَالِكِ يَوْمِ الدِّينِ", "Maliki Yawmid-Din", "Sovereign of the Day of Recompense.", juzNumber = 1),
        QuranAyahItem(1005, 1, "Al-Fatiha", "الفاتحة", 5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "Iyyaka na'budu wa iyyaka nasta'in", "It is You we worship and You we ask for help.", juzNumber = 1),
        QuranAyahItem(1006, 1, "Al-Fatiha", "الفاتحة", 6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", "Ihdinas-siratal-mustaqim", "Guide us to the straight path -", juzNumber = 1),
        QuranAyahItem(1007, 1, "Al-Fatiha", "الفاتحة", 7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "Siratalladhina an'amta 'alayhim ghayril-maghdubi 'alayhim wa lad-dallin", "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray.", juzNumber = 1)
    )

    private val BAQARAH_SAMPLE_AYAH_LIST = listOf(
        QuranAyahItem(2001, 2, "Al-Baqarah", "البقرة", 1, "الم", "Alif-Lam-Mim", "Alif, Lam, Meem.", juzNumber = 1),
        QuranAyahItem(2002, 2, "Al-Baqarah", "البقرة", 2, "ذَٰلِكَ الْكِتَابُ لَا رَيْبَ ۛ فِيهِ ۛ هُدًى لِّلْمُتَّقِينَ", "Dhalikal-kitabu la rayba fih, hudal-lil-muttaqin", "This is the Book about which there is no doubt, a guidance for those conscious of Allah -", juzNumber = 1),
        QuranAyahItem(2003, 2, "Al-Baqarah", "البقرة", 3, "الَّذِينَ يُؤْمِنُونَ بِالْغَيْبِ وَيُقِيمُونَ الصَّلَاةَ وَمِمَّا رَزَقْنَاهُمْ يُنفِقُونَ", "Alladhina yu'minuna bil-ghaybi wa yuqimunas-salata wa mimma razaqnahum yunfiqun", "Who believe in the unseen, establish prayer, and spend out of what We have provided for them,", juzNumber = 1),
        QuranAyahItem(2004, 2, "Al-Baqarah", "البقرة", 4, "وَالَّذِينَ يُؤْمِنُونَ بِمَا أُنزِلَ إِلَيْكَ وَمَا أُنزِلَ مِن قَبْلِكَ وَبِالْآخِرَةِ هُمْ يُوقِنُونَ", "Walladhina yu'minuna bima unzila ilayka wa ma unzila min qablika wa bil-akhirati hum yuqinun", "And who believe in what has been revealed to you, [O Muhammad], and what was revealed before you, and of the Hereafter they are certain [in faith].", juzNumber = 1),
        QuranAyahItem(2005, 2, "Al-Baqarah", "البقرة", 5, "أُولَٰئِكَ عَلَىٰ هُدًى مِّن رَّبِّهِمْ ۖ وَأُولَٰئِكَ هُمُ الْمُفْلِحُونَ", "Ula'ika 'ala hudam-mir-Rabbihim wa ula'ika humul-muflihun", "Those are upon [right] guidance from their Lord, and it is those who are the successful.", juzNumber = 1),
        QuranAyahItem(2006, 2, "Al-Baqarah", "البقرة", 255, "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ", "Allahu la ilaha illa Huwal-Hayyul-Qayyum. La ta'khudhuhu sinatuw-wa la nawm...", "Allah - there is no deity except Him, the Ever-Living, the Sustainer of [all] existence. Neither drowsiness overtakes Him nor sleep...", juzNumber = 3),
        QuranAyahItem(2007, 2, "Al-Baqarah", "البقرة", 285, "آمَنَ الرَّسُولُ بِمَا أُنزِلَ إِلَيْهِ مِن رَّبِّهِ وَالْمُؤْمِنُونَ ۚ كُلٌّ آمَنَ بِاللَّهِ وَمَلَائِكَتِهِ وَكُتُبِهِ وَرُسُلِهِ", "Amanar-Rasulu bima unzila ilayhi mir-Rabbihi wal-mu'minun...", "The Messenger has believed in what was revealed to him from his Lord, and [so have] the believers...", juzNumber = 3),
        QuranAyahItem(2008, 2, "Al-Baqarah", "البقرة", 286, "لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا ۚ لَهَا مَا كَسَبَتْ وَعَلَيْهَا مَا اكْتَسَبَتْ", "La yukallifullahu nafsan illa wus'aha...", "Allah does not charge a soul except [with that within] its capacity...", juzNumber = 3)
    )

    private val MULK_AYAH_LIST = listOf(
        QuranAyahItem(67001, 67, "Al-Mulk", "الملك", 1, "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ", "Tabarakalladhi biyadihil-mulku wa Huwa 'ala kulli shay'in Qadir", "Blessed is He in whose hand is dominion, and He is over all things competent -", juzNumber = 29),
        QuranAyahItem(67002, 67, "Al-Mulk", "الملك", 2, "الَّذِي خَلَقَ الْمَوْتَ وَالْحَيَاةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا ۚ وَهُوَ الْعَزِيزُ الْغَفُورُ", "Alladhi khalaqal-mawta wal-hayata liyabluwakum ayyukum ahsanu 'amala...", "He who created death and life to test you as to which of you is best in deed - and He is the Exalted in Might, the Forgiving -", juzNumber = 29),
        QuranAyahItem(67003, 67, "Al-Mulk", "الملك", 3, "الَّذِي خَلَقَ سَبْعَ سَمَاوَاتٍ طِبَاقًا ۖ مَّا تَرَىٰ فِي خَلْقِ الرَّحْمَٰنِ مِن تَفَاوُتٍ", "Alladhi khalaqa sab'a samawatin tibaqan...", "Who created seven heavens in layers. You do not see in the creation of the Most Merciful any inconsistency...", juzNumber = 29),
        QuranAyahItem(67004, 67, "Al-Mulk", "الملك", 4, "ثُمَّ ارْجِعِ الْبَصَرَ كَرَّتَيْنِ يَنقَلِبْ إِلَيْكَ الْبَصَرُ خَاسِئًا وَهُوَ حَسِيرٌ", "Thummar-ji'il-basara karratayni yanqalib ilaykal-basaru khasi'an wa huwa hasir", "Then return [your] vision twice again. [Your] vision will return to you humbled while it is fatigued.", juzNumber = 29),
        QuranAyahItem(67005, 67, "Al-Mulk", "الملك", 5, "وَلَقَدْ زَيَّنَّا السَّمَاءَ الدُّنْيَا بِمَصَابِيحَ وَجَعَلْنَاهَا رُجُومًا لِّلشَّيَاطِينِ", "Wa laqad zayyannas-sama'ad-dunya bimasabiha wa ja'alnaham rujumal-lish-sayatin", "And We have certainly beautified the nearest heaven with lamps and have made from them what is thrown at devils...", juzNumber = 29)
    )

    private val YASIN_AYAH_LIST = listOf(
        QuranAyahItem(36001, 36, "Ya-Sin", "يس", 1, "يس", "Ya-Sin", "Ya, Seen.", juzNumber = 22),
        QuranAyahItem(36002, 36, "Ya-Sin", "يس", 2, "وَالْقُرْآنِ الْحَكِيمِ", "Wal-Qur'anil-Hakim", "By the wise Qur'an,", juzNumber = 22),
        QuranAyahItem(36003, 36, "Ya-Sin", "يس", 3, "إِنَّكَ لَمِنَ الْمُرْسَلِينَ", "Innaka laminal-mursalin", "Indeed you, [O Muhammad], are from among the messengers,", juzNumber = 22),
        QuranAyahItem(36004, 36, "Ya-Sin", "يس", 4, "عَلَىٰ صِرَاطٍ مُّسْتَقِيمٍ", "'Ala siratim-mustaqim", "On a straight path.", juzNumber = 22),
        QuranAyahItem(36005, 36, "Ya-Sin", "يس", 5, "تَنزِيلَ الْعَزِيزِ الرَّحِيمِ", "Tanzilal-'Azizir-Rahim", "[This is] a revelation of the Exalted in Might, the Merciful,", juzNumber = 22)
    )

    private val KAHF_AYAH_LIST = listOf(
        QuranAyahItem(18001, 18, "Al-Kahf", "الكهف", 1, "الْحَمْدُ لِلَّهِ الَّذِي أَنزَلَ عَلَىٰ عَبْدِهِ الْكِتَابَ وَلَمْ يَجْعَل لَّهُ عِوَجًا ۜ", "Al-hamdu lillahilladhi anzala 'ala 'abdihil-kitaba wa lam yaj'al lahu 'iwaja", "[All] praise is [due] to Allah, who has sent down upon His Servant the Book and has not made therein any deviance.", juzNumber = 15),
        QuranAyahItem(18002, 18, "Al-Kahf", "الكهف", 2, "قَيِّمًا لِّيُنذِرَ بَأْسًا شَدِيدًا مِّن لَّدُنْهُ وَيُبَشِّرَ الْمُؤْمِنِينَ الَّذِينَ يَعْمَلُونَ الصَّالِحَاتِ أَنَّ لَهُمْ أَجْرًا حَسَنًا", "Qayyimal-liyundhira ba'san shadidam-mil-ladunhu wa yubashshiral-mu'minin...", "[He has made it] straight, to warn of severe punishment from Him and to give good tidings to the believers...", juzNumber = 15),
        QuranAyahItem(18003, 18, "Al-Kahf", "الكهف", 3, "مَّاكِثِينَ فِيهِ أَبَدًا", "Makithina fihi abada", "In which they will remain forever", juzNumber = 15),
        QuranAyahItem(18004, 18, "Al-Kahf", "الكهف", 4, "وَيُنذِرَ الَّذِينَ قَالُوا اتَّخَذَ اللَّهُ وَلَدًا", "Wa yundhiralladhina qaluttakhadhallahu walada", "And to warn those who say, \"Allah has taken a son.\"", juzNumber = 15),
        QuranAyahItem(18005, 18, "Al-Kahf", "الكهف", 5, "مَّا لَهُم بِهِ مِنْ عِلْمٍ وَلَا لِآبَائِهِمْ ۚ كَبُرَتْ كَلِمَةً تَخْرُجُ مِنْ أَفْوَاهِهِمْ ۚ إِن يَقُولُونَ إِلَّا كَذِبًا", "Ma lahum bihi min 'ilmiw-wa la li'aba'ihim...", "They have no knowledge of it, nor had their fathers...", juzNumber = 15)
    )

    private val RAHMAN_AYAH_LIST = listOf(
        QuranAyahItem(55001, 55, "Ar-Rahman", "الرحمن", 1, "الرَّحْمَٰنُ", "Ar-Rahman", "The Most Merciful", juzNumber = 27),
        QuranAyahItem(55002, 55, "Ar-Rahman", "الرحمن", 2, "عَلَّمَ الْقُرْآنَ", "'Allamal-Qur'an", "Taught the Qur'an,", juzNumber = 27),
        QuranAyahItem(55003, 55, "Ar-Rahman", "الرحمن", 3, "خَلَقَ الْإِنسَانَ", "Khalaqal-insan", "Created man,", juzNumber = 27),
        QuranAyahItem(55004, 55, "Ar-Rahman", "الرحمن", 4, "عَلَّمَهُ الْبَيَانَ", "'Allamahul-bayan", "Taught him eloquence.", juzNumber = 27),
        QuranAyahItem(55005, 55, "Ar-Rahman", "الرحمن", 5, "الشَّمْسُ وَالْقَمَرُ بِحُسْبَانٍ", "Ash-shamsu wal-qamaru bihusban", "The sun and the moon [move] by precise calculation,", juzNumber = 27)
    )

    private val IKHLAS_AYAH_LIST = listOf(
        QuranAyahItem(112001, 112, "Al-Ikhlas", "الإخلاص", 1, "قُلْ هُوَ اللَّهُ أَحَدٌ", "Qul Huwallahu Ahad", "Say, \"He is Allah, [who is] One,", juzNumber = 30),
        QuranAyahItem(112002, 112, "Al-Ikhlas", "الإخلاص", 2, "اللَّهُ الصَّمَدُ", "Allahus-Samad", "Allah, the Eternal Refuge.", juzNumber = 30),
        QuranAyahItem(112003, 112, "Al-Ikhlas", "الإخلاص", 3, "لَمْ يَلِدْ وَلَمْ يُولَدْ", "Lam yalid wa lam yulad", "He neither begets nor is born,", juzNumber = 30),
        QuranAyahItem(112004, 112, "Al-Ikhlas", "الإخلاص", 4, "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ", "Wa lam yakul-lahu kufuwan ahad", "Nor is there to Him any equivalent.\"", juzNumber = 30)
    )
}
