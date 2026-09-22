package com.example.ui.duayn

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassAppBackground
import com.example.ui.components.GlassCard
import com.example.ui.theme.AccentGold
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.PrayerViewModel

data class DuaItem(
    val id: Int,
    val titleEn: String,
    val titleUr: String,
    val category: String,
    val arabicText: String,
    val urduTranslation: String,
    val englishTranslation: String,
    val reference: String
)

val authenticDuaList = listOf(
    DuaItem(
        id = 1,
        titleEn = "Dua Before Sleeping",
        titleUr = "سRule یا سوتے وقت کی دعا",
        category = "Sleep",
        arabicText = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا۔",
        urduTranslation = "اے اللہ! میں تیرے ہی نام کے ساتھ مرتا (سوتا) ہوں اور جیتا (جاگتا) ہوں۔",
        englishTranslation = "In Your name, O Allah, I die and I live.",
        reference = "صحيح البخاري 6312"
    ),
    DuaItem(
        id = 2,
        titleEn = "Dua Upon Waking Up",
        titleUr = "بیدار ہوتے وقت (جاگنے کی) دعا",
        category = "Sleep",
        arabicText = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ۔",
        urduTranslation = "تمام تعریفیں اللہ کے لیے ہیں جس نے ہمیں مارنے کے بعد زندہ کیا اور اسی کی طرف لوٹ کر جانا ہے۔",
        englishTranslation = "All praise is due to Allah Who gave us life after causing us to die, and unto Him is the resurrection.",
        reference = "صحيح البخاري 6312"
    ),
    DuaItem(
        id = 3,
        titleEn = "Dua Before Eating Food",
        titleUr = "کھانا کھانے سے پہلے کی دعا",
        category = "Food",
        arabicText = "بِسْمِ اللَّهِ وَعَلَى بَرَكَةِ اللَّهِ۔",
        urduTranslation = "اللہ کے نام سے اور اللہ کی برکت پر (ہم نے کھانا شروع کیا۔)",
        englishTranslation = "In the name of Allah and upon the blessings of Allah.",
        reference = "المستدرك للحاكم 7084"
    ),
    DuaItem(
        id = 4,
        titleEn = "Dua After Finishing Meal",
        titleUr = "کھانا کھانے کے بعد کی دعا",
        category = "Food",
        arabicText = "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنَا وَسَقَانَا وَجَعَلَنَا مِنَ الْمُسْلِمِينَ۔",
        urduTranslation = "تمام تعریفیں اللہ ہی کے لیے ہیں جس نے ہمیں کھلایا اور پلایا اور ہمیں مسلمانوں میں سے بنایا۔",
        englishTranslation = "All praise is due to Allah Who fed us and gave us drink and made us Muslims.",
        reference = "جامع الترمذي 3457"
    ),
    DuaItem(
        id = 5,
        titleEn = "Dua When Leaving Home",
        titleUr = "گھر سے نکلتے وقت کی دعا",
        category = "Travel",
        arabicText = "بِسْمِ اللَّهِ، تَوَكَّلْتُ عَلَى اللَّهِ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ۔",
        urduTranslation = "اللہ کے نام سے، میں نے اللہ پر بھروسہ کیا، اور گناہوں سے بچنے کی طاقت اور نیکی کرنے کی توفیق صرف اللہ ہی کی طرف سے ہے۔",
        englishTranslation = "In the name of Allah, I place my trust in Allah; there is no power and no strength except with Allah.",
        reference = "سنن أبي داود 5095"
    ),
    DuaItem(
        id = 6,
        titleEn = "Dua For Traveling",
        titleUr = "سفر کی سواری پر بیٹھنے کی دعا",
        category = "Travel",
        arabicText = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ، وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ۔",
        urduTranslation = "پاک ہے وہ ذات جس نے اس کو ہمارے لیے مسخر کر دیا حالانکہ ہم اسے قابو میں لانے والے نہ تھے، اور ہم اپنے رب کی طرف لوٹنے والے ہیں۔",
        englishTranslation = "Glory be to Him Who has subjected this to us, and we could never have accomplished it by ourselves; and to our Lord we shall surely return.",
        reference = "سورة الزخرف 13-14 • صحيح مسلم 1342"
    ),
    DuaItem(
        id = 7,
        titleEn = "Dua When Entering Mosque",
        titleUr = "مسجد میں داخل ہونے کی دعا",
        category = "Mosque",
        arabicText = "اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ۔",
        urduTranslation = "اے اللہ! میرے لیے اپنی رحمت کے دروازے کھول دے۔",
        englishTranslation = "O Allah, open for me the doors of Your mercy.",
        reference = "صحيح مسلم 713"
    ),
    DuaItem(
        id = 8,
        titleEn = "Dua When Leaving Mosque",
        titleUr = "مسجد سے نکلنے کی دعا",
        category = "Mosque",
        arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ۔",
        urduTranslation = "اے اللہ! میں تجھ سے تیرے فضل کا سوال کرتا ہوں۔",
        englishTranslation = "O Allah, I ask You from Your favor.",
        reference = "صحيح مسلم 713"
    ),
    DuaItem(
        id = 9,
        titleEn = "Dua For Parents",
        titleUr = "والدین کی مغفرت اور رحم کی دعا",
        category = "Family",
        arabicText = "رَبِّ ارْحَمْهُمَا كَمَا رَبَّيَانِي صَغِيرًا۔",
        urduTranslation = "اے میرے رب! ان دونوں (والدین) پر رحم فرما جیسا کہ انہوں نے مجھے بچپن میں پالا۔",
        englishTranslation = "My Lord, have mercy upon them both as they brought me up when I was small.",
        reference = "سورة الإسراء 24"
    ),
    DuaItem(
        id = 10,
        titleEn = "Dua For Good Health & Well-being",
        titleUr = "عافیت اور صحت کی دعا",
        category = "Health",
        arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي الدُّنْيَا وَالْآخِرَةِ۔",
        urduTranslation = "اے اللہ! میں تجھ سے دنیا اور آخرت میں معافی اور عافیت کا سوال کرتا ہوں۔",
        englishTranslation = "O Allah, I ask You for forgiveness and health in this world and the Hereafter.",
        reference = "سنن ابن ماجه 3871"
    ),
    DuaItem(
        id = 11,
        titleEn = "Dua When Visiting Sick",
        titleUr = "عیادت (بیمار پرسی) کے وقت کی دعا",
        category = "Health",
        arabicText = "لَا بَأْسَ طَهُورٌ إِنْ شَاءَ اللَّهُ۔",
        urduTranslation = "کوئی بات نہیں، اللہ نے چاہا تو یہ بیماری گناہوں سے پاک کرنے والی ہے۔",
        englishTranslation = "No harm, it will be a purification if Allah wills.",
        reference = "صحيح البخاري 3616"
    ),
    DuaItem(
        id = 12,
        titleEn = "Dua For Protection From Evil Eye & Harm",
        titleUr = "نظرِ بد اور ہر برائی سے تحفظ کی دعا",
        category = "Protection",
        arabicText = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّةِ مِنْ كُلِّ شَيْطَانٍ وَهَامَّةٍ وَمِنْ كُلِّ عَيْنٍ لَامَّةٍ۔",
        urduTranslation = "میں اللہ کے کامل کلمات کی پناہ مانگتا ہوں ہر شیطان، موذی جانور اور بد نظر سے۔",
        englishTranslation = "I seek refuge in the perfect words of Allah from every devil, poisonous creature, and evil eye.",
        reference = "صحيح البخاري 3371"
    )
)

val duaCategories = listOf("All", "Sleep", "Food", "Travel", "Mosque", "Family", "Health", "Protection")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuaynScreen(
    viewModel: PrayerViewModel,
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var bookmarkedIds by remember { mutableStateOf(setOf<Int>()) }

    val filteredDuas = remember(searchQuery, selectedCategory) {
        authenticDuaList.filter { dua ->
            val matchesCategory = selectedCategory == "All" || dua.category.equals(selectedCategory, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() ||
                    dua.titleEn.contains(searchQuery, ignoreCase = true) ||
                    dua.titleUr.contains(searchQuery, ignoreCase = true) ||
                    dua.arabicText.contains(searchQuery) ||
                    dua.urduTranslation.contains(searchQuery)
            matchesCategory && matchesQuery
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = com.example.ui.theme.PrimaryDeepTeal,
                shadowElevation = 3.dp
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Daily Duas & Supplications",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Authentic Quranic & Prophetic Invocations",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = com.example.ui.theme.WarmOrangeAccent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    },
                    navigationIcon = {
                        if (onNavigateBack != null) {
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = androidx.compose.ui.Modifier.testTag("duayn_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = com.example.ui.theme.PrimaryDeepTeal
                    )
                )
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        GlassAppBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = {
                        Text(
                            "Search Dua (e.g. Sleep, Food, Protection)...",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF64748B))
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = EmeraldPrimary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF64748B))
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.88f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.78f),
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = Color.White,
                        focusedTextColor = Color(0xFF0F172A),
                        unfocusedTextColor = Color(0xFF0F172A)
                    ),
                    singleLine = true
                )

                // Category Chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(duaCategories) { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = {
                                Text(
                                    text = category,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White.copy(alpha = 0.82f),
                                labelColor = Color(0xFF334155)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) EmeraldPrimary else Color.White
                            )
                        )
                    }
                }

                // List of Duas
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp)
                ) {
                    items(filteredDuas, key = { it.id }) { dua ->
                        val isBookmarked = bookmarkedIds.contains(dua.id)

                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                // Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = EmeraldPrimary.copy(alpha = 0.12f),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${dua.id}",
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = EmeraldPrimary
                                                    )
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = dua.titleEn,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF0F172A)
                                                )
                                            )
                                            Text(
                                                text = dua.category,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = EmeraldPrimary,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            )
                                        }
                                    }

                                    IconButton(onClick = {
                                        bookmarkedIds = if (isBookmarked) bookmarkedIds - dua.id else bookmarkedIds + dua.id
                                    }) {
                                        Icon(
                                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                            contentDescription = "Bookmark",
                                            tint = if (isBookmarked) AccentGold else Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Arabic Text (Prominent & Clear)
                                Text(
                                    text = dua.arabicText,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF064E3B),
                                        fontSize = 21.sp,
                                        lineHeight = 38.sp
                                    ),
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // English Translation
                                Text(
                                    text = dua.englishTranslation,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFF334155),
                                        lineHeight = 22.sp
                                    )
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Urdu Translation
                                Text(
                                    text = dua.urduTranslation,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF64748B),
                                        lineHeight = 20.sp
                                    ),
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Footer & Copy
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = dua.reference,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFFB45309),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )

                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Islamic Dua", "${dua.titleEn}\n\n${dua.arabicText}\n\nTranslation: ${dua.englishTranslation}\n\nReference: ${dua.reference}")
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Dua copied to clipboard", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
