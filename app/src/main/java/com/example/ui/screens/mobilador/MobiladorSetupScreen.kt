package com.example.ui.screens.mobilador

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.mobilador.ConnectedInputDeviceInfo
import com.example.model.mobilador.MappingElement
import com.example.model.mobilador.MappingElementType
import com.example.model.mobilador.MobiladorProfile
import com.example.ui.MainViewModel
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonBorder
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonGlow
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.PaseoBorderDark
import com.example.ui.theme.PaseoCardBackground
import com.example.ui.theme.PaseoDarkBackground
import com.example.ui.theme.PaseoSurfaceCard
import com.example.ui.theme.PaseoSurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobiladorSetupScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val mobiladorRepo = viewModel.mobiladorRepository
    val activeProfile by mobiladorRepo.activeProfile.collectAsStateWithLifecycle()
    val allProfiles by mobiladorRepo.allProfiles.collectAsStateWithLifecycle()
    val connectedDevices by mobiladorRepo.connectedDevices.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(1) } // Default to Mapping Editor

    var showSaveDialog by remember { mutableStateOf(false) }
    var saveProfileNameInput by remember { mutableStateOf("") }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameInput by remember { mutableStateOf("") }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportJsonText by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }
    var showAddElementDialog by remember { mutableStateOf(false) }
    var selectedElementId by remember { mutableStateOf<String?>(null) }

    Surface(
        color = PaseoDarkBackground,
        modifier = Modifier.fillMaxSize().testTag("mobilador_setup_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "MOBILADOR SETUP",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Profil: ${activeProfile.name}",
                            fontSize = 11.sp,
                            color = CrimsonGlow
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("mobilador_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val undone = mobiladorRepo.undo()
                            if (undone != null) viewModel.showMessage("Undo berhasil.")
                        },
                        enabled = mobiladorRepo.canUndo(),
                        modifier = Modifier.testTag("mobilador_undo_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (mobiladorRepo.canUndo()) Color.White else Color.Gray
                        )
                    }
                    IconButton(
                        onClick = {
                            val redone = mobiladorRepo.redo()
                            if (redone != null) viewModel.showMessage("Redo berhasil.")
                        },
                        enabled = mobiladorRepo.canRedo(),
                        modifier = Modifier.testTag("mobilador_redo_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            tint = if (mobiladorRepo.canRedo()) Color.White else Color.Gray
                        )
                    }
                    IconButton(
                        onClick = {
                            exportJsonText = mobiladorRepo.exportProfileJson(activeProfile)
                            showExportDialog = true
                        },
                        modifier = Modifier.testTag("mobilador_export_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Ekspor", tint = Color.White)
                    }
                    IconButton(
                        onClick = {
                            importJsonText = ""
                            showImportDialog = true
                        },
                        modifier = Modifier.testTag("mobilador_import_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Impor", tint = CrimsonGlow)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PaseoDarkBackground)
            )

            // Segment Tabs (Setup Perangkat, Mapping Editor, Kontrol & Integrasi)
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = PaseoSurfaceDark,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = CrimsonPrimary
                    )
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Keyboard, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Setup Perangkat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SportsEsports, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Mapping Editor", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Mouse, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Kontrol & Integrasi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> DeviceSetupTab(
                    connectedDevices = connectedDevices,
                    onRefresh = { mobiladorRepo.scanConnectedDevices() },
                    viewModel = viewModel
                )
                1 -> MappingEditorTab(
                    activeProfile = activeProfile,
                    allProfiles = allProfiles,
                    selectedElementId = selectedElementId,
                    onSelectElement = { selectedElementId = it },
                    onUpdateProfile = { mobiladorRepo.updateEditorProfile(it) },
                    onSelectProfile = { mobiladorRepo.setActiveProfile(it) },
                    onSaveNewProfile = {
                        saveProfileNameInput = "${activeProfile.name} (Custom)"
                        showSaveDialog = true
                    },
                    onDuplicateProfile = {
                        val dup = mobiladorRepo.duplicateProfile(activeProfile)
                        viewModel.showMessage("Profil '${dup.name}' diduplikasi.")
                    },
                    onRenameProfile = {
                        renameInput = activeProfile.name
                        showRenameDialog = true
                    },
                    onDeleteProfile = {
                        mobiladorRepo.deleteProfile(activeProfile.id)
                        viewModel.showMessage("Profil dihapus.")
                    },
                    onResetLayout = {
                        val fallback = MobiladorProfile.DEFAULT_PROFILES.firstOrNull { it.id == activeProfile.id }
                            ?: MobiladorProfile.DEFAULT_PROFILES.first()
                        mobiladorRepo.updateEditorProfile(fallback)
                        viewModel.showMessage("Layout dikembalikan ke preset awal.")
                    },
                    onOpenAddElement = { showAddElementDialog = true },
                    viewModel = viewModel
                )
                2 -> ControlAndIntegrationTab(
                    activeProfile = activeProfile,
                    onUpdateProfile = { mobiladorRepo.updateEditorProfile(it) },
                    viewModel = viewModel
                )
            }
        }

        // Save Dialog
        if (showSaveDialog) {
            AlertDialog(
                onDismissRequest = { showSaveDialog = false },
                title = { Text("Simpan Profil Mobilador Baru", color = Color.White) },
                text = {
                    OutlinedTextField(
                        value = saveProfileNameInput,
                        onValueChange = { saveProfileNameInput = it },
                        label = { Text("Nama Profil") },
                        singleLine = true,
                        colors = outlinedTextFieldColors()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (saveProfileNameInput.isNotBlank()) {
                                val saved = mobiladorRepo.saveCustomProfile(
                                    activeProfile.copy(name = saveProfileNameInput.trim())
                                )
                                showSaveDialog = false
                                viewModel.showMessage("Profil '${saved.name}' berhasil disimpan.")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                    ) {
                        Text("Simpan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveDialog = false }) {
                        Text("Batal", color = TextMuted)
                    }
                },
                containerColor = PaseoSurfaceCard
            )
        }

        // Rename Dialog
        if (showRenameDialog) {
            AlertDialog(
                onDismissRequest = { showRenameDialog = false },
                title = { Text("Ubah Nama Profil", color = Color.White) },
                text = {
                    OutlinedTextField(
                        value = renameInput,
                        onValueChange = { renameInput = it },
                        label = { Text("Nama Baru") },
                        singleLine = true,
                        colors = outlinedTextFieldColors()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (renameInput.isNotBlank()) {
                                mobiladorRepo.renameProfile(activeProfile.id, renameInput.trim())
                                showRenameDialog = false
                                viewModel.showMessage("Nama profil diperbarui.")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                    ) {
                        Text("Ubah")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRenameDialog = false }) {
                        Text("Batal", color = TextMuted)
                    }
                },
                containerColor = PaseoSurfaceCard
            )
        }

        // Export Dialog
        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = { Text("Ekspor Konfigurasi JSON", color = Color.White) },
                text = {
                    Column {
                        Text("Salin format JSON profil mapping berikut:", color = Color(0xFFCCCCCC), fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = exportJsonText,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                            colors = outlinedTextFieldColors()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("MobiladorJSON", exportJsonText))
                            viewModel.showMessage("JSON profil disalin ke clipboard.")
                            showExportDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                    ) {
                        Text("Salin & Tutup")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExportDialog = false }) {
                        Text("Tutup", color = TextMuted)
                    }
                },
                containerColor = PaseoSurfaceCard
            )
        }

        // Import Dialog
        if (showImportDialog) {
            AlertDialog(
                onDismissRequest = { showImportDialog = false },
                title = { Text("Impor Konfigurasi JSON", color = Color.White) },
                text = {
                    Column {
                        Text("Tempel JSON profil mapping di bawah:", color = Color(0xFFCCCCCC), fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = importJsonText,
                            onValueChange = { importJsonText = it },
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                            colors = outlinedTextFieldColors()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val res = mobiladorRepo.importProfileJson(importJsonText)
                            res.onSuccess {
                                showImportDialog = false
                                viewModel.showMessage("Profil '${it.name}' berhasil diimpor!")
                            }.onFailure {
                                viewModel.showMessage("Gagal mengimpor: ${it.localizedMessage}")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                    ) {
                        Text("Impor")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showImportDialog = false }) {
                        Text("Batal", color = TextMuted)
                    }
                },
                containerColor = PaseoSurfaceCard
            )
        }

        // Add Element Dialog
        if (showAddElementDialog) {
            AlertDialog(
                onDismissRequest = { showAddElementDialog = false },
                title = { Text("Tambah Tombol Mapping Baru", color = Color.White) },
                text = {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().height(260.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(MappingElementType.values()) { type ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val newElem = MappingElement(
                                            id = UUID.randomUUID().toString(),
                                            name = type.displayName,
                                            type = type,
                                            keyBinding = type.defaultKey,
                                            keyCode = type.defaultKeyCode,
                                            normalizedX = 0.5f,
                                            normalizedY = 0.5f
                                        )
                                        val updatedList = activeProfile.elements + newElem
                                        mobiladorRepo.updateEditorProfile(activeProfile.copy(elements = updatedList))
                                        showAddElementDialog = false
                                        selectedElementId = newElem.id
                                        viewModel.showMessage("Tombol '${type.displayName}' ditambahkan ke tengah layar.")
                                    },
                                colors = CardDefaults.cardColors(containerColor = PaseoSurfaceDark)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(type.displayName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF221115))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(type.defaultKey, color = CrimsonGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showAddElementDialog = false }) {
                        Text("Batal", color = TextMuted)
                    }
                },
                containerColor = PaseoSurfaceCard
            )
        }
    }
}

// ==========================================
// TAB 1: DEVICE SETUP & CONNECTION
// ==========================================
@Composable
private fun DeviceSetupTab(
    connectedDevices: List<ConnectedInputDeviceInfo>,
    onRefresh: () -> Unit,
    viewModel: MainViewModel
) {
    val keyboardDetected = connectedDevices.any { it.isKeyboard }
    val mouseDetected = connectedDevices.any { it.isMouse }

    var lastCapturedKey by remember { mutableStateOf<String?>(null) }
    var lastCapturedMouse by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Status Overview Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PaseoSurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CrimsonBorder, Color(0xFF24141E))))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STATUS KONEKSI HARDWARE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonGlow,
                            letterSpacing = 1.sp
                        )
                        IconButton(onClick = onRefresh, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = "Pindai Perangkat", tint = Color.White)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Keyboard Status Box
                        DeviceStatusBox(
                            title = "Keyboard",
                            isDetected = keyboardDetected,
                            icon = Icons.Default.Keyboard,
                            modifier = Modifier.weight(1f)
                        )
                        // Mouse Status Box
                        DeviceStatusBox(
                            title = "Mouse",
                            isDetected = mouseDetected,
                            icon = Icons.Default.Mouse,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (connectedDevices.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text("Perangkat Terdeteksi:", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        connectedDevices.forEach { dev ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (dev.isBluetooth) Icons.Default.Bluetooth else Icons.Default.Usb,
                                    contentDescription = null,
                                    tint = if (dev.isKeyboard || dev.isMouse) EmeraldAccent else Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = dev.name,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.weight(1f))
                                Text(
                                    text = when {
                                        dev.isKeyboard && dev.isMouse -> "Combo KB+Mouse"
                                        dev.isKeyboard -> "Keyboard"
                                        dev.isMouse -> "Mouse"
                                        else -> "Perangkat Input"
                                    },
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Input Testing Pad
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF130E14)),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFF4A1A28), Color(0xFF221118))))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AREA UJI INPUT LIVE (KEYBOARD & MOUSE)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = CrimsonGlow
                    )
                    Text(
                        text = "Tekan tombol keyboard atau klik mouse di area ini untuk menguji respon input",
                        fontSize = 10.sp,
                        color = TextMuted
                    )

                    Spacer(Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0A070B))
                            .border(1.dp, Color(0xFF2D1822), RoundedCornerShape(10.dp))
                            .focusRequester(focusRequester)
                            .focusable()
                            .clickable { focusRequester.requestFocus() }
                            .onKeyEvent { keyEvent ->
                                val k = keyEvent.key
                                lastCapturedKey = "Key: ${k} (KeyCode: ${keyEvent.nativeKeyEvent.keyCode})"
                                true
                            }
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull()
                                        if (change != null && change.pressed) {
                                            if (change.type == PointerType.Mouse) {
                                                lastCapturedMouse = "Mouse: Tombol Mouse Terdeteksi (X: ${change.position.x.toInt()}, Y: ${change.position.y.toInt()})"
                                            } else {
                                                lastCapturedMouse = "Input Sentuhan: Terdeteksi (Tipe: ${change.type})"
                                            }
                                        }
                                    }
                                }
                            }
                            .testTag("mobilador_input_test_surface"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = lastCapturedKey ?: "Ketuk di sini lalu tekan tombol Keyboard",
                                color = if (lastCapturedKey != null) EmeraldAccent else Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = lastCapturedMouse ?: "Klik mouse untuk menguji deteksi tombol mouse",
                                color = if (lastCapturedMouse != null) CrimsonGlow else Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Connection Guide & Troubleshooting
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PaseoSurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(PaseoBorderDark, Color(0xFF1A1A22))))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PANDUAN KONEKSI & TROUBLESHOOTING",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))

                    TroubleshootStepItem(
                        number = "1",
                        title = "Koneksi USB OTG / Converter Hub",
                        desc = "Colokkan kabel OTG ke HP Android. Jika perangkat tidak menyala, aktifkan 'Koneksi OTG' di Pengaturan Sistem Android (beberapa HP seperti Oppo/Vivo mematikan OTG otomatis setelah 10 menit)."
                    )
                    TroubleshootStepItem(
                        number = "2",
                        title = "Koneksi Bluetooth",
                        desc = "Aktifkan Bluetooth pada HP dan nyalakan mode pairing pada converter/keyboard. Pasangkan perangkat dan pastikan status terhubung sebagai 'Perangkat Input'."
                    )
                    TroubleshootStepItem(
                        number = "3",
                        title = "Izin & Deteksi Game",
                        desc = "Android membutuhkan izin 'Tampilkan di atas aplikasi lain' (Overlay) agar bantuan indikator tombol dapat ditampilkan saat game berjalan."
                    )
                }
            }
        }

        item { Spacer(Modifier.height(40.dp)) }
    }
}

@Composable
private fun DeviceStatusBox(
    title: String,
    isDetected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDetected) Color(0xFF0F2B1A) else Color(0xFF221115))
            .border(1.dp, if (isDetected) EmeraldAccent.copy(alpha = 0.6f) else CrimsonBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDetected) EmeraldAccent else CrimsonGlow,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (isDetected) "Terhubung Aktif" else "Belum Terdeteksi",
                color = if (isDetected) EmeraldAccent else Color(0xFFFF7777),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TroubleshootStepItem(number: String, title: String, desc: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(CrimsonContainer)
                .border(1.dp, CrimsonGlow, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(number, color = CrimsonGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(desc, color = TextMuted, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}

// ==========================================
// TAB 2: MAPPING EDITOR
// ==========================================
@Composable
private fun MappingEditorTab(
    activeProfile: MobiladorProfile,
    allProfiles: List<MobiladorProfile>,
    selectedElementId: String?,
    onSelectElement: (String?) -> Unit,
    onUpdateProfile: (MobiladorProfile) -> Unit,
    onSelectProfile: (MobiladorProfile) -> Unit,
    onSaveNewProfile: () -> Unit,
    onDuplicateProfile: () -> Unit,
    onRenameProfile: () -> Unit,
    onDeleteProfile: () -> Unit,
    onResetLayout: () -> Unit,
    onOpenAddElement: () -> Unit,
    viewModel: MainViewModel
) {
    var editKeyBindingDialog by remember { mutableStateOf(false) }
    var editingElement by remember { mutableStateOf<MappingElement?>(null) }
    var keyBindingInput by remember { mutableStateOf("") }

    // Check duplicate key conflict
    val keyConflicts = remember(activeProfile.elements) {
        val groups = activeProfile.elements.groupBy { it.keyBinding.uppercase() }
        groups.filter { it.key.isNotBlank() && it.key != "MOUSE" && it.value.size > 1 }.keys
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Profiles Bar
        item {
            Text("PILIHAN PROFIL MAPPING", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CrimsonGlow)
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(allProfiles, key = { it.id }) { p ->
                    val isSelected = p.id == activeProfile.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectProfile(p) },
                        label = {
                            Text(
                                p.name,
                                color = if (isSelected) Color.White else Color(0xFFCCCCCC),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CrimsonPrimary,
                            containerColor = PaseoSurfaceDark
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) CrimsonGlow else PaseoBorderDark,
                            selectedBorderColor = CrimsonGlow,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }
        }

        // Action Toolbar (Save New, Duplicate, Rename, Reset, Delete)
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = onSaveNewProfile,
                    modifier = Modifier.weight(1f).height(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = CrimsonGlow, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Simpan", fontSize = 11.sp, color = Color.White)
                }
                OutlinedButton(
                    onClick = onDuplicateProfile,
                    modifier = Modifier.weight(1f).height(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Duplikasi", fontSize = 11.sp, color = Color.White)
                }
                OutlinedButton(
                    onClick = onResetLayout,
                    modifier = Modifier.weight(1f).height(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Reset", fontSize = 11.sp, color = Color.White)
                }
                if (activeProfile.isCustom) {
                    OutlinedButton(
                        onClick = onRenameProfile,
                        modifier = Modifier.size(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Ubah Nama", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                    OutlinedButton(
                        onClick = onDeleteProfile,
                        modifier = Modifier.size(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFFF5555), modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        // Conflict Warning Banner
        if (keyConflicts.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF381414)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Konflik Tombol Duplikat: Tombol [${keyConflicts.joinToString()}] terpasang pada lebih dari 1 aksi!",
                            color = Color(0xFFFF9999),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Interactive Screen Canvas (Aspect Ratio 20:9 or 16:9)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0B10)),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CrimsonBorder, Color(0xFF221118))))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("PREVIEW LAYAR GAME", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Rasio: ${activeProfile.aspectRatio}", fontSize = 10.sp, color = TextMuted)
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = onOpenAddElement,
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                                modifier = Modifier.height(30.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Tambah Tombol", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Screen Canvas
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(if (activeProfile.aspectRatio == "16:9") 16f / 9f else 20f / 9f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF070508))
                            .border(1.dp, Color(0xFF331C28), RoundedCornerShape(8.dp))
                            .clickable { onSelectElement(null) }
                            .testTag("mobilador_canvas_preview")
                    ) {
                        val canvasWidth = maxWidth
                        val canvasHeight = maxHeight

                        // Crosshair / Center reference
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawLine(Color(0xFF1E1018), Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), 1f)
                            drawLine(Color(0xFF1E1018), Offset(0f, size.height / 2), Offset(size.width, size.height / 2), 1f)
                        }

                        // Render Mapping Elements
                        activeProfile.elements.forEach { elem ->
                            val isSelected = elem.id == selectedElementId
                            val isConflict = keyConflicts.contains(elem.keyBinding.uppercase())

                            val posX = (canvasWidth.value * elem.normalizedX).dp - (elem.sizeDp / 2).dp
                            val posY = (canvasHeight.value * elem.normalizedY).dp - (elem.sizeDp / 2).dp

                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(posX.roundToPx(), posY.roundToPx()) }
                                    .size(elem.sizeDp.dp)
                                    .clip(if (elem.type == MappingElementType.MOVEMENT_WASD || elem.type == MappingElementType.MOUSE_LOOK) CircleShape else RoundedCornerShape(10.dp))
                                    .background(
                                        when {
                                            isConflict -> Color(0xFF551118)
                                            isSelected -> CrimsonPrimary.copy(alpha = elem.opacity)
                                            else -> Color(0xFF1F1522).copy(alpha = elem.opacity)
                                        }
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = when {
                                            isConflict -> AmberWarning
                                            isSelected -> CrimsonGlow
                                            else -> Color(0xFF442D3D)
                                        },
                                        shape = if (elem.type == MappingElementType.MOVEMENT_WASD || elem.type == MappingElementType.MOUSE_LOOK) CircleShape else RoundedCornerShape(10.dp)
                                    )
                                    .pointerInput(elem.id) {
                                        detectDragGestures(
                                            onDragStart = { onSelectElement(elem.id) },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                val newNormX = (elem.normalizedX + (dragAmount.x / canvasWidth.toPx())).coerceIn(0.05f, 0.95f)
                                                val newNormY = (elem.normalizedY + (dragAmount.y / canvasHeight.toPx())).coerceIn(0.05f, 0.95f)
                                                val updatedList = activeProfile.elements.map {
                                                    if (it.id == elem.id) it.copy(normalizedX = newNormX, normalizedY = newNormY) else it
                                                }
                                                onUpdateProfile(activeProfile.copy(elements = updatedList))
                                            }
                                        )
                                    }
                                    .clickable { onSelectElement(elem.id) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = elem.keyBinding,
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = if (elem.type == MappingElementType.MOVEMENT_WASD) 12.sp else 10.sp
                                    )
                                    Text(
                                        text = elem.name.take(6),
                                        color = CrimsonGlow,
                                        fontSize = 8.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Element Detail Inspector & Adjustment
        val selectedElement = activeProfile.elements.find { it.id == selectedElementId }
        if (selectedElement != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PaseoSurfaceCard),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CrimsonBorder, Color(0xFF221520))))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("PENGATURAN TOMBOL: ${selectedElement.name}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CrimsonGlow)
                                Text("Binding: [${selectedElement.keyBinding}]", fontSize = 11.sp, color = Color.White)
                            }
                            Row {
                                Button(
                                    onClick = {
                                        editingElement = selectedElement
                                        keyBindingInput = selectedElement.keyBinding
                                        editKeyBindingDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                                    modifier = Modifier.height(30.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("Ganti Tombol", fontSize = 10.sp)
                                }
                                Spacer(Modifier.width(6.dp))
                                IconButton(
                                    onClick = {
                                        val filtered = activeProfile.elements.filter { it.id != selectedElement.id }
                                        onUpdateProfile(activeProfile.copy(elements = filtered))
                                        onSelectElement(null)
                                        viewModel.showMessage("Tombol dihapus.")
                                    },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFFF5555), modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Size Slider
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Ukuran Tombol", fontSize = 11.sp, color = Color.White)
                            Text("${selectedElement.sizeDp.toInt()} dp", fontSize = 11.sp, color = CrimsonGlow, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = selectedElement.sizeDp,
                            onValueChange = { newSize ->
                                val updated = activeProfile.elements.map {
                                    if (it.id == selectedElement.id) it.copy(sizeDp = newSize) else it
                                }
                                onUpdateProfile(activeProfile.copy(elements = updated))
                            },
                            valueRange = 30f..120f,
                            colors = sliderColors()
                        )

                        // Opacity Slider
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Transparansi Visual", fontSize = 11.sp, color = Color.White)
                            Text("${(selectedElement.opacity * 100).toInt()}%", fontSize = 11.sp, color = CrimsonGlow, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = selectedElement.opacity,
                            onValueChange = { newOp ->
                                val updated = activeProfile.elements.map {
                                    if (it.id == selectedElement.id) it.copy(opacity = newOp) else it
                                }
                                onUpdateProfile(activeProfile.copy(elements = updated))
                            },
                            valueRange = 0.2f..1.0f,
                            colors = sliderColors()
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(40.dp)) }
    }

    // Edit Key Binding Dialog
    if (editKeyBindingDialog && editingElement != null) {
        AlertDialog(
            onDismissRequest = { editKeyBindingDialog = false },
            title = { Text("Atur Tombol / KeyBinding", color = Color.White) },
            text = {
                Column {
                    Text("Ketik tombol keyboard atau pilih tombol cepat:", color = TextMuted, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = keyBindingInput,
                        onValueChange = { keyBindingInput = it.uppercase() },
                        singleLine = true,
                        colors = outlinedTextFieldColors()
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("Pilihan Cepat:", fontSize = 10.sp, color = TextMuted)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("M_LEFT", "M_RIGHT", "SPACE", "C", "R", "F", "TAB").forEach { k ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF221115))
                                    .border(1.dp, CrimsonBorder, RoundedCornerShape(6.dp))
                                    .clickable { keyBindingInput = k }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(k, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (keyBindingInput.isNotBlank()) {
                            val updated = activeProfile.elements.map {
                                if (it.id == editingElement!!.id) it.copy(keyBinding = keyBindingInput.trim()) else it
                            }
                            onUpdateProfile(activeProfile.copy(elements = updated))
                            editKeyBindingDialog = false
                            viewModel.showMessage("Tombol diperbarui ke $keyBindingInput")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { editKeyBindingDialog = false }) {
                    Text("Batal", color = TextMuted)
                }
            },
            containerColor = PaseoSurfaceCard
        )
    }
}

// ==========================================
// TAB 3: CONTROLS & INTEGRATION
// ==========================================
@Composable
private fun ControlAndIntegrationTab(
    activeProfile: MobiladorProfile,
    onUpdateProfile: (MobiladorProfile) -> Unit,
    viewModel: MainViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // In-Game Mouse Settings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PaseoSurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CrimsonBorder, Color(0xFF22121B))))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SETELAN POINTER & MOUSE IN-GAME",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = CrimsonGlow
                    )
                    Spacer(Modifier.height(12.dp))

                    // Hide Cursor Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sembunyikan Kursor Mouse Saat Main", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Mengunci pointer ke tengah layar untuk simulasi FPS / mouse look", color = TextMuted, fontSize = 10.sp)
                        }
                        Switch(
                            checked = activeProfile.isCursorHiddenInGame,
                            onCheckedChange = {
                                onUpdateProfile(activeProfile.copy(isCursorHiddenInGame = it))
                            },
                            colors = switchColors()
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Mouse Sensitivity X
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Sensitivitas Mouse Sumbu X (Yaw)", fontSize = 11.sp, color = Color.White)
                        Text(String.format(java.util.Locale.US, "%.2fx", activeProfile.mouseSensitivityX), fontSize = 11.sp, color = CrimsonGlow, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = activeProfile.mouseSensitivityX,
                        onValueChange = { onUpdateProfile(activeProfile.copy(mouseSensitivityX = it)) },
                        valueRange = 0.5f..3.0f,
                        colors = sliderColors()
                    )

                    Spacer(Modifier.height(8.dp))

                    // Mouse Sensitivity Y
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Sensitivitas Mouse Sumbu Y (Pitch)", fontSize = 11.sp, color = Color.White)
                        Text(String.format(java.util.Locale.US, "%.2fx", activeProfile.mouseSensitivityY), fontSize = 11.sp, color = CrimsonGlow, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = activeProfile.mouseSensitivityY,
                        onValueChange = { onUpdateProfile(activeProfile.copy(mouseSensitivityY = it)) },
                        valueRange = 0.5f..3.0f,
                        colors = sliderColors()
                    )
                }
            }
        }

        // Honest Technical Disclaimer & Architecture Transparency
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF160E14)),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFF4A1A24), Color(0xFF221118))))
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = CrimsonGlow, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Transparansi Teknis Mobilador Android",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Android menerapkan isolasi keamanan (App Sandbox). Tanpa converter hardware (seperti Geekgamer, KuGamer, Flydigi) atau Accessibility Service / Shizuku berizin, aplikasi pihak ketiga tidak dapat mengirim simulasi sentuhan ke layar game lain. PASEO SENSI menyediakan editor mapping presisi, generator layout, pengujian input hardware, dan sinkronisasi profil yang aman tanpa merusak integritas sistem atau game.",
                            color = Color(0xFFB5AAB1),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(40.dp)) }
    }
}

@Composable
private fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CrimsonPrimary,
    unfocusedBorderColor = PaseoBorderDark,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = CrimsonGlow
)

@Composable
private fun sliderColors() = SliderDefaults.colors(
    thumbColor = CrimsonGlow,
    activeTrackColor = CrimsonPrimary,
    inactiveTrackColor = Color(0xFF2B1D27)
)

@Composable
private fun switchColors() = SwitchDefaults.colors(
    checkedThumbColor = Color.White,
    checkedTrackColor = CrimsonPrimary,
    uncheckedThumbColor = TextMuted,
    uncheckedTrackColor = Color(0xFF221520)
)
