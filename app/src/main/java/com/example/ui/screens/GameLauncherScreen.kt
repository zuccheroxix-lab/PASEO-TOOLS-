package com.example.ui.screens

import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.InstalledApp
import com.example.ui.MainViewModel
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonGlow
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.PaseoBorderDark
import com.example.ui.theme.PaseoBorderGreen
import com.example.ui.theme.PaseoDarkBackground
import com.example.ui.theme.PaseoSurfaceCard
import com.example.ui.theme.PaseoSurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class LauncherFilterMode {
    GAMES,
    ALL,
    FAVORITES
}

@Composable
fun GameLauncherScreen(
    viewModel: MainViewModel,
    apps: List<InstalledApp>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isLoading: Boolean,
    onGameClick: (InstalledApp) -> Unit
) {
    var filterMode by remember { mutableStateOf(LauncherFilterMode.GAMES) }
    var showAddDialog by remember { mutableStateOf(false) }
    var addDialogSearchQuery by remember { mutableStateOf("") }
    val favoritePackages by viewModel.favoritePackages.collectAsStateWithLifecycle()

    // Real list filtering - strictly honest telemetry without simulated or fake apps
    val baseList = remember(apps, filterMode, favoritePackages) {
        when (filterMode) {
            LauncherFilterMode.GAMES -> apps.filter { it.isGame }
            LauncherFilterMode.ALL -> apps
            LauncherFilterMode.FAVORITES -> apps.filter { it.packageName in favoritePackages }
        }
    }

    val displayList = remember(baseList, searchQuery, favoritePackages) {
        val query = searchQuery.trim()
        val filtered = if (query.isBlank()) {
            baseList
        } else {
            baseList.filter {
                it.appName.contains(query, ignoreCase = true) ||
                it.packageName.contains(query, ignoreCase = true)
            }
        }
        // Prioritize favorites on top, then alphabetical
        filtered.sortedWith(
            compareByDescending<InstalledApp> { it.packageName in favoritePackages }
                .thenBy { it.appName.lowercase() }
        )
    }

    val totalDetectedGames = remember(apps) { apps.count { it.isGame } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PaseoDarkBackground)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Bar
            item(span = { GridItemSpan(3) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Game Launcher",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF221115))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${displayList.size} item",
                                    color = CrimsonGlow,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = if (totalDetectedGames > 0) {
                                "$totalDetectedGames game terdeteksi via PackageManager"
                            } else {
                                "0 game berkategori android:appCategory='game'"
                            },
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = { viewModel.loadApps() },
                        modifier = Modifier.testTag("btn_refresh_apps")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Muat Ulang Aplikasi",
                            tint = CrimsonGlow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Category Filter Tabs: Game Terdeteksi | Semua Aplikasi | Favorit
            item(span = { GridItemSpan(3) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PaseoSurfaceDark)
                        .border(1.dp, PaseoBorderGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterTabButton(
                        title = "Game ($totalDetectedGames)",
                        isSelected = filterMode == LauncherFilterMode.GAMES,
                        onClick = { filterMode = LauncherFilterMode.GAMES },
                        modifier = Modifier.weight(1f).testTag("filter_games")
                    )
                    FilterTabButton(
                        title = "Semua (${apps.size})",
                        isSelected = filterMode == LauncherFilterMode.ALL,
                        onClick = { filterMode = LauncherFilterMode.ALL },
                        modifier = Modifier.weight(1f).testTag("filter_all")
                    )
                    FilterTabButton(
                        title = "Favorit (${favoritePackages.size})",
                        isSelected = filterMode == LauncherFilterMode.FAVORITES,
                        onClick = { filterMode = LauncherFilterMode.FAVORITES },
                        modifier = Modifier.weight(1f).testTag("filter_favorites")
                    )
                }
            }

            // Search Bar
            item(span = { GridItemSpan(3) }) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .testTag("game_search_input"),
                    placeholder = {
                        Text(
                            text = when (filterMode) {
                                LauncherFilterMode.GAMES -> "Cari game terdeteksi..."
                                LauncherFilterMode.ALL -> "Cari seluruh aplikasi terpasang..."
                                LauncherFilterMode.FAVORITES -> "Cari game favorit..."
                            },
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hapus Pencarian",
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = PaseoSurfaceCard,
                        unfocusedContainerColor = PaseoSurfaceCard,
                        focusedBorderColor = CrimsonPrimary,
                        unfocusedBorderColor = PaseoBorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }

            // Loading indicator
            if (isLoading) {
                item(span = { GridItemSpan(3) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = CrimsonGlow, modifier = Modifier.size(32.dp))
                    }
                }
            }

            // Empty State Handling
            if (!isLoading && displayList.isEmpty()) {
                item(span = { GridItemSpan(3) }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = PaseoSurfaceCard),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PaseoBorderDark)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = when (filterMode) {
                                    LauncherFilterMode.GAMES -> Icons.Default.SportsEsports
                                    LauncherFilterMode.FAVORITES -> Icons.Default.StarBorder
                                    LauncherFilterMode.ALL -> Icons.Default.Apps
                                },
                                contentDescription = null,
                                tint = CrimsonGlow,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = when {
                                    searchQuery.isNotBlank() -> "Aplikasi Tidak Ditemukan"
                                    filterMode == LauncherFilterMode.GAMES -> "Belum Ada Game Terdeteksi"
                                    filterMode == LauncherFilterMode.FAVORITES -> "Belum Ada Game Favorit"
                                    else -> "Daftar Aplikasi Kosong"
                                },
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = when {
                                    searchQuery.isNotBlank() -> "Tidak ditemukan aplikasi yang cocok dengan kata kunci '$searchQuery'."
                                    filterMode == LauncherFilterMode.GAMES -> "Sistem Android tidak mendeteksi aplikasi dengan tag 'appCategory=game'. Anda dapat beralih ke tab 'Semua' untuk memilih aplikasi terpasang secara manual."
                                    filterMode == LauncherFilterMode.FAVORITES -> "Beri tanda bintang pada game atau aplikasi untuk menambahkannya ke tab Favorit agar cepat diakses."
                                    else -> "Tidak dapat membaca paket aplikasi terpasang dari sistem Android."
                                },
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            if (filterMode == LauncherFilterMode.GAMES && searchQuery.isBlank() && apps.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { filterMode = LauncherFilterMode.ALL },
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Buka Tab 'Semua Aplikasi'", fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Grid Items of Games
            items(displayList) { game ->
                val isFav = game.packageName in favoritePackages
                GameGridCard(
                    game = game,
                    isFavorite = isFav,
                    onToggleFavorite = { viewModel.toggleFavorite(game.packageName) },
                    onClick = { onGameClick(game) }
                )
            }

            // Add Custom Game Card inside Grid
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(125.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, PaseoBorderDark, RoundedCornerShape(16.dp))
                        .clickable { showAddDialog = true }
                        .testTag("add_custom_game_card"),
                    colors = CardDefaults.cardColors(containerColor = PaseoSurfaceCard)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E1E26)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Pilih Aplikasi",
                                tint = TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Pilih\nAplikasi",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Bottom Spacing for Floating/Fixed Bar
            item(span = { GridItemSpan(3) }) {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Bottom Fixed Button: "+ Tambah Game / Kelola Profil"
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .testTag("btn_tambah_game"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrimsonPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = "Pilih Aplikasi & Buat Profil",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

    // App Picker Dialog to Add App to Game List / View Profiles
    if (showAddDialog) {
        val filteredDialogApps = remember(apps, addDialogSearchQuery) {
            if (addDialogSearchQuery.isBlank()) {
                apps
            } else {
                apps.filter {
                    it.appName.contains(addDialogSearchQuery, ignoreCase = true) ||
                    it.packageName.contains(addDialogSearchQuery, ignoreCase = true)
                }
            }
        }

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                addDialogSearchQuery = ""
            },
            title = {
                Column {
                    Text(
                        text = "Pilih Aplikasi Terpasang",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Buka detail game untuk mengatur sensitivitas & optimasi",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = addDialogSearchQuery,
                        onValueChange = { addDialogSearchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Cari aplikasi...", color = TextMuted, fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = PaseoSurfaceDark,
                            unfocusedContainerColor = PaseoSurfaceDark,
                            focusedBorderColor = CrimsonPrimary,
                            unfocusedBorderColor = PaseoBorderDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredDialogApps) { app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PaseoSurfaceDark)
                                    .clickable {
                                        showAddDialog = false
                                        addDialogSearchQuery = ""
                                        onGameClick(app)
                                    }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val bmp = (app.icon as? BitmapDrawable)?.bitmap
                                if (bmp != null) {
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = app.appName,
                                        modifier = Modifier.size(28.dp).clip(RoundedCornerShape(6.dp))
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (app.isGame) Icons.Default.SportsEsports else Icons.Default.Apps,
                                        contentDescription = null,
                                        tint = CrimsonGlow,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.appName,
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = app.packageName,
                                        color = TextMuted,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (app.packageName in favoritePackages) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Favorit",
                                        tint = AmberWarning,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAddDialog = false
                        addDialogSearchQuery = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                ) {
                    Text("Tutup", color = Color.White)
                }
            },
            containerColor = PaseoSurfaceCard
        )
    }
}

@Composable
private fun FilterTabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) CrimsonPrimary else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else TextMuted,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun GameGridCard(
    game: InstalledApp,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(125.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (isFavorite) CrimsonGlow.copy(alpha = 0.6f) else PaseoBorderDark,
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .testTag("game_card_${game.packageName.replace(".", "_")}"),
        colors = CardDefaults.cardColors(containerColor = PaseoSurfaceCard)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Game Icon
                val bmp = (game.icon as? BitmapDrawable)?.bitmap
                if (bmp != null) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = game.appName,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF2A080E), Color(0xFF140306))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (game.isGame) Icons.Default.SportsEsports else Icons.Default.Apps,
                            contentDescription = game.appName,
                            tint = CrimsonGlow,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = game.appName,
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = game.versionName,
                    color = TextMuted,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Favorite star badge
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(28.dp)
                    .padding(2.dp)
                    .testTag("fav_${game.packageName.replace(".", "_")}")
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = if (isFavorite) "Hapus Favorit" else "Tambah Favorit",
                    tint = if (isFavorite) AmberWarning else TextMuted.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
