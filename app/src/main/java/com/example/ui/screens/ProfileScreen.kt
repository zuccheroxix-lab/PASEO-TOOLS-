package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.CrimsonGlow
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.PaseoBorderDark
import com.example.ui.theme.PaseoDarkBackground
import com.example.ui.theme.PaseoSurfaceCard
import com.example.ui.theme.PaseoSurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VerifiedBlue
import com.example.ui.theme.WhatsAppGreen

@Composable
fun ProfileScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current

    Surface(
        color = PaseoDarkBackground,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item {
                Text(
                    text = "Developer & Partner",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // Developer Card (ZUCCHERO XANN)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, PaseoBorderDark, RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = PaseoSurfaceCard)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(CrimsonPrimary.copy(alpha = 0.5f), Color(0xFF1E0509))
                                        )
                                    )
                                    .border(1.5.dp, CrimsonGlow.copy(alpha = 0.8f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "ZUCCHERO XANN",
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = VerifiedBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Text(
                                    text = "Developer / Owner",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF0F0F14))
                                        .clickable {
                                            copyToClipboard(context, "ZX-OWNER-001")
                                            viewModel.showMessage("UID tersalin: ZX-OWNER-001")
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "UID: ZX-OWNER-001",
                                        color = TextMuted,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy UID",
                                        tint = TextMuted,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Partner Section Header
            item {
                Text(
                    text = "PARTNER",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                )
            }

            // Partner 1: LALZ
            item {
                PartnerCard(
                    number = "1",
                    name = "LALZ",
                    role = "Partner 1",
                    id = "best",
                    onCopy = {
                        copyToClipboard(context, "best")
                        viewModel.showMessage("ID LALZ tersalin: best")
                    }
                )
            }

            // Partner 2: VAXXY
            item {
                PartnerCard(
                    number = "2",
                    name = "VAXXY",
                    role = "Partner 2",
                    id = "VAXXY",
                    onCopy = {
                        copyToClipboard(context, "VAXXY")
                        viewModel.showMessage("ID VAXXY tersalin: VAXXY")
                    }
                )
            }

            // Social & Community Links Section
            item {
                Text(
                    text = "KOMUNITAS & MEDIA",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
                )
            }

            item {
                SocialLinkCard(
                    title = "WhatsApp Developer",
                    subtitle = "Chat Sekarang",
                    icon = Icons.AutoMirrored.Filled.Send,
                    iconColor = WhatsAppGreen,
                    onClick = {
                        openUrl(context, "https://wa.me/?text=Halo%20Developer%20PASEO%20SENSI")
                    }
                )
            }

            item {
                SocialLinkCard(
                    title = "Channel",
                    subtitle = "Join Channel Informasi & Update",
                    icon = Icons.Default.Campaign,
                    iconColor = Color(0xFF2979FF),
                    onClick = {
                        openUrl(context, "https://github.com/zuccheroxix-lab/PASEO-TOOLS-")
                    }
                )
            }

            item {
                SocialLinkCard(
                    title = "Sociabuzz",
                    subtitle = "Dukung Developer ZUCCHERO XANN",
                    icon = Icons.Default.Favorite,
                    iconColor = Color(0xFFFF9100),
                    onClick = {
                        openUrl(context, "https://sociabuzz.com")
                    }
                )
            }

            item {
                SocialLinkCard(
                    title = "TikTok",
                    subtitle = "@zuccheroxann",
                    icon = Icons.Default.Videocam,
                    iconColor = CrimsonGlow,
                    onClick = {
                        openUrl(context, "https://www.tiktok.com/@zuccheroxann")
                    }
                )
            }

            // Security & App Lock Section
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "KEAMANAN & PENGUNCIAN INTERNAL",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, PaseoBorderDark, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = PaseoSurfaceCard)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = CrimsonGlow,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Keamanan Aplikasi PASEO SENSI",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = "PIN dan Pola dienkripsi dengan salted PBKDF2 hash lokal. Sistem ini murni keamanan aplikasi dan tidak mengubah lockscreen Android Anda.",
                            color = TextMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            androidx.compose.material3.Button(
                                onClick = { viewModel.lockApp() },
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Kunci Aplikasi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            androidx.compose.material3.Button(
                                onClick = { viewModel.logout() },
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = PaseoSurfaceDark),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Ganti Akun", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun PartnerCard(
    number: String,
    name: String,
    role: String,
    id: String,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, PaseoBorderDark, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = PaseoSurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF22222E)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.take(1),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "$number. $name",
                        color = TextPrimary,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$role • ID: $id",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy ID",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun SocialLinkCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, PaseoBorderDark, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = PaseoSurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PaseoSurfaceDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("ID", text))
}

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (_: Exception) {}
}
