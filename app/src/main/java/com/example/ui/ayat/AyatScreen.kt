package com.example.ui.ayat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.Ayat
import com.example.ui.components.GlassAppBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.IslamicStarDeco
import com.example.ui.theme.AccentGold
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.PrayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AyatScreen(
    viewModel: PrayerViewModel,
    onNavigateBack: () -> Unit,
    onSelectAyat: (Ayat) -> Unit
) {
    val allAyat by viewModel.allAyat.collectAsStateWithLifecycle()
    val currentAyat by viewModel.currentAyat.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    val filteredAyat = remember(allAyat, searchQuery) {
        if (searchQuery.isBlank()) {
            allAyat
        } else {
            val q = searchQuery.lowercase()
            allAyat.filter {
                it.surahName.lowercase().contains(q) ||
                it.englishTranslation.lowercase().contains(q) ||
                it.themeTopic.lowercase().contains(q) ||
                it.arabicText.contains(q)
            }
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
                        Text(
                            "Quranic Verses Library",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("ayat_screen_back_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = com.example.ui.theme.PrimaryDeepTeal)
                )
            }
        },
        containerColor = Color.Transparent,
        modifier = Modifier.testTag("ayat_screen_root")
    ) { padding ->
        GlassAppBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ayat_search_field"),
                    placeholder = { Text("Search by Surah or theme (e.g. Salah, Baqarah)...", color = Color(0xFF64748B)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = EmeraldPrimary) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.88f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.78f),
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = Color.White,
                        focusedTextColor = Color(0xFF0F172A),
                        unfocusedTextColor = Color(0xFF0F172A)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredAyat) { ayat ->
                        val isSelected = currentAyat?.id == ayat.id
                        AyatLibraryCard(
                            ayat = ayat,
                            isSelected = isSelected,
                            onSelect = {
                                viewModel.selectAyat(ayat)
                                onSelectAyat(ayat)
                            }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun AyatLibraryCard(
    ayat: Ayat,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccentGold.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentGold, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = ayat.verificationStatus,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFB45309), fontSize = 10.sp)
                        )
                    }
                }

                IslamicStarDeco(sizeDp = 18, color = EmeraldPrimary)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Arabic text
            Text(
                text = ayat.arabicText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 36.sp,
                    color = Color(0xFF064E3B),
                    textAlign = TextAlign.Right
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Transliteration
            Text(
                text = ayat.transliteration,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // English Translation
            Text(
                text = ayat.englishTranslation,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 22.sp,
                    color = Color(0xFF334155)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Urdu Translation
            Text(
                text = ayat.urduTranslation,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Right
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${ayat.surahName} (${ayat.surahArabicName}) : ${ayat.ayatNumber}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                )

                Button(
                    onClick = onSelect,
                    shape = RoundedCornerShape(10.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFF047857) else EmeraldPrimary
                    )
                ) {
                    Text(if (isSelected) "Active Ayah" else "Select", color = Color.White)
                }
            }
        }
    }
}
