package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.VeloraTransaction
import com.example.ui.theme.*
import com.example.ui.viewmodel.VeloraViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VeloraDashboard(viewModel: VeloraViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    var showOcrDialog by remember { mutableStateOf(false) }

    // Speech recognition launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (spokenText != null) {
                parseAndAddVoiceTransaction(spokenText, viewModel, context)
            }
        }
    }

    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = ObsidianBackground,
        bottomBar = {
            VeloraBottomNavigation(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
            // Bagian Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("velora_header"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "VELORA",
                        style = MaterialTheme.typography.displayLarge.copy(
                            color = ChampagneGold,
                            letterSpacing = 4.sp,
                            fontSize = 32.sp
                        )
                    )
                    Text(
                        text = "PRIVATE WEALTH ENGINE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TextSecondary,
                            letterSpacing = 2.sp
                        )
                    )
                }

                // Avatar Profil
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, ChampagneGold, CircleShape)
                        .background(CharcoalCardAlt)
                        .clickable {
                            Toast.makeText(context, "Velora Elite Member Account", Toast.LENGTH_SHORT).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "VL",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = ChampagneGold,
                            fontSize = 16.sp
                        )
                    )
                    // Ikon ornamen bintang
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = "Elite Status Badge",
                        tint = ChampagneGold,
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 1.dp, y = (-2).dp)
                    )
                }
            }

            // Card info kekayaan / saldo utama
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(CharcoalSurface, Color(0xFF222222)),
                        )
                    )
                    .border(
                        BorderStroke(
                            1.dp,
                            Brush.linearGradient(
                                colors = listOf(GoldGradientStart, GoldGradientEnd)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp)
                    .testTag("wealth_card")
            ) {
                // Efek glow di background
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(ChampagneGold.copy(alpha = 0.08f), Color.Transparent)
                        ),
                        radius = size.width / 1.5f,
                        center = Offset(size.width, 0f)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Asset Class Symbol",
                                tint = ChampagneGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "TOTAL LIQUID BALANCE",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = TextSecondary,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CharcoalCardAlt),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = EmeraldMint,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Total saldo terupdate
                    Text(
                        text = formatCurrency(uiState.totalBalance),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("total_balance_text")
                    )

                    Divider(color = Color(0xFF2B2B2B), thickness = 1.dp)

                    // Rincian Pemasukan / Pengeluaran
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Info Pemasukan
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldMint)
                                )
                                Text(
                                    text = "INFLOW",
                                    style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary, letterSpacing = 0.5.sp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = formatCurrency(uiState.totalIncome),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = EmeraldMint,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.testTag("total_income_text")
                            )
                        }

                        // Info Pengeluaran
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(CrimsonRose)
                                )
                                Text(
                                    text = "OUTFLOW",
                                    style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary, letterSpacing = 0.5.sp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = formatCurrency(uiState.totalExpense),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = CrimsonRose,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.testTag("total_expense_text")
                            )
                        }
                    }
                }
            }

            // Visualisasi alokasi aset (Portofolio)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CharcoalSurface)
                    .border(1.dp, Color(0xFF222222), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Asset Portfolio",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = ChampagneGold,
                            fontSize = 18.sp
                        )
                    )
                    
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Market Allocation Trend",
                        tint = ChampagneGold,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bar persentase alokasi masing-masing aset
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CharcoalCardAlt)
                ) {
                    val totalClass = uiState.stocksValue + uiState.cryptoValue + uiState.goldValue
                    val stocksW = if (totalClass == 0.0) 0.5f else (uiState.stocksValue / totalClass).toFloat()
                    val cryptoW = if (totalClass == 0.0) 0.3f else (uiState.cryptoValue / totalClass).toFloat()
                    val goldW = if (totalClass == 0.0) 0.2f else (uiState.goldValue / totalClass).toFloat()

                    if (stocksW > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(stocksW)
                                .background(Brush.horizontalGradient(listOf(Color(0xFF8A640F), ChampagneGold)))
                        )
                    }
                    if (cryptoW > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(cryptoW)
                                .background(Brush.horizontalGradient(listOf(Color(0xFF00B0FF), EmeraldMint)))
                        )
                    }
                    if (goldW > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(goldW)
                                .background(Brush.horizontalGradient(listOf(Color(0xFFFF9100), Color(0xFFFFD600))))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // List detail pembagian aset
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    val totalClass = uiState.stocksValue + uiState.cryptoValue + uiState.goldValue
                    val sP = if(totalClass == 0.0) 50 else (uiState.stocksValue / totalClass * 100).toInt()
                    val cP = if(totalClass == 0.0) 30 else (uiState.cryptoValue / totalClass * 100).toInt()
                    val gP = if(totalClass == 0.0) 20 else (uiState.goldValue / totalClass * 100).toInt()

                    AssetAllocationRow(
                        name = "Stocks (Core Equities)",
                        pct = "$sP%",
                        amount = formatCurrency(uiState.stocksValue),
                        indicatorColor = ChampagneGold
                    )

                    AssetAllocationRow(
                        name = "Crypto (Digital Assets)",
                        pct = "$cP%",
                        amount = formatCurrency(uiState.cryptoValue),
                        indicatorColor = EmeraldMint
                    )

                    AssetAllocationRow(
                        name = "Gold (Bullion Reserves)",
                        pct = "$gP%",
                        amount = formatCurrency(uiState.goldValue),
                        indicatorColor = Color(0xFFFFD600)
                    )
                }
            }

            // Tombol-tombol aksi cepat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Input suara
                Button(
                    onClick = {
                        // Buka dialog simulasi / speech intent
                        showVoiceDialog = true
                        startSpeechToTextIntent(context, speechLauncher)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("voice_input_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CharcoalSurface,
                        contentColor = ChampagneGold
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF2C2C2C))
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Speech Recognition Link",
                        tint = ChampagneGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Voice",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                }

                // Scan struk/nota belanjaan
                Button(
                    onClick = { showOcrDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("scan_ocr_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CharcoalSurface,
                        contentColor = ChampagneGold
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF2C2C2C))
                ) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "Scan Document Receiver Link",
                        tint = ChampagneGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Scan",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                }

                // Tambah keuangan manual
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("add_transaction_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ChampagneGold,
                        contentColor = ObsidianBackground
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add New Record Link",
                        tint = ObsidianBackground,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+ Add",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ObsidianBackground
                        )
                    )
                }
            }

            // Riwayat transaksi keuangan berjalan
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Wealth Activity Logs",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = ChampagneGold,
                            fontSize = 18.sp
                        )
                    )

                    Text(
                        text = "Reset DB",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = CrimsonRose,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .clickable {
                                viewModel.clearAllData()
                                Toast.makeText(context, "Database Reset", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.transactions.isEmpty()) {
                    // Tampilan jika data kosong
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = "Safe Deposit Box",
                            tint = Color(0xFF444444),
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = "No records logged yet",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        uiState.transactions.forEach { trans ->
                            TransactionRowItem(
                                transaction = trans,
                                onDelete = { viewModel.deleteTransaction(trans) }
                            )
                        }
                    }
                }
            }
        }
    } // Akhir tab HOME

    1 -> {
        // Tab Aset: List rincian kepemilikan portfolio
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Bagian Akun Portfolio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PORTFOLIO",
                        style = MaterialTheme.typography.displayLarge.copy(
                            color = ChampagneGold,
                            letterSpacing = 4.sp,
                            fontSize = 28.sp
                        )
                    )
                    Text(
                        text = "ASSET CLASS DETAILED HOLDINGS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TextSecondary,
                            letterSpacing = 2.sp
                        )
                    )
                }
            }

            // Card akumulasi nilai wallet keseluruhan
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(CharcoalSurface)
                    .border(BorderStroke(1.dp, Color(0xFF222222)), RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "CONSOLIDATED WALLET VALUES",
                        style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary, letterSpacing = 1.sp)
                    )
                    Text(
                        text = formatCurrency(uiState.stocksValue + uiState.cryptoValue + uiState.goldValue),
                        style = MaterialTheme.typography.headlineLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                    )
                    Divider(color = Color(0xFF2B2B2B))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Portfolio Integrity", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
                        Text("Secured Offline Vault", style = MaterialTheme.typography.bodyMedium.copy(color = EmeraldMint, fontWeight = FontWeight.Bold))
                    }
                }
            }

            // Detail kepemilikan Saham/Equities
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(0.5.dp, Color(0xFF2C2C2C))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically, 
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShowChart, 
                                contentDescription = "Stocks Allocation", 
                                tint = ChampagneGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "Global Equities (Stocks)", 
                                style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            formatCurrency(uiState.stocksValue), 
                            style = MaterialTheme.typography.titleMedium.copy(color = ChampagneGold, fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text("Premium international indexes including bluechips tech and growth funds.", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                    
                    Button(
                        onClick = {
                            viewModel.addTransaction("Core Equities Auto-Rebalance Inflow", 5000000.0, "INCOME", "Stocks")
                            Toast.makeText(context, "Equities rebalanced! Rp 5.000.000 added.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalCardAlt, contentColor = ChampagneGold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Simulate Allocation Inflow (+Rp 5M)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            // Detail kepemilikan Crypto
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(0.5.dp, Color(0xFF2C2C2C))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically, 
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CurrencyBitcoin, 
                                contentDescription = "Crypto Allocation", 
                                tint = EmeraldMint,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "Digital Currencies (Crypto)", 
                                style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            formatCurrency(uiState.cryptoValue), 
                            style = MaterialTheme.typography.titleMedium.copy(color = EmeraldMint, fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text("Secured sovereign digital coins and tokens managed in cold vault ledgers.", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                    
                    Button(
                        onClick = {
                            viewModel.addTransaction("Bitcoin Smart-Accumulation Outflow", 2500000.0, "EXPENSE", "Crypto")
                            Toast.makeText(context, "Deposited to Crypto cold vault! Rp 2.500.000 deducted.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalCardAlt, contentColor = EmeraldMint),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Simulate Cold Vault Acquisition (-Rp 2.5M)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            // Detail kepemilikan Emas/Bullion
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(0.5.dp, Color(0xFF2C2C2C))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically, 
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stars, 
                                contentDescription = "Gold Allocation", 
                                tint = Color(0xFFFFD600),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "Fine Physical Gold (Bullion)", 
                                style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            formatCurrency(uiState.goldValue), 
                            style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFFFFD600), fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text("Certified fine physical 99.9% gold bullion minted bars guarded in private depositories.", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                    
                    Button(
                        onClick = {
                            viewModel.addTransaction("Fine Gold Bullion Purchase Outflow", 1500000.0, "EXPENSE", "Gold")
                            Toast.makeText(context, "Acquired minted gold! Rp 1.500.000 deducted.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalCardAlt, contentColor = Color(0xFFFFD600)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Simulate Gold Depository Purchase (-Rp 1.5M)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    } // Akhir tab ASET

    2 -> {
        // Tab Notifikasi / Alerts System
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Bagian Notifikasi Sistem
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MONITORS",
                        style = MaterialTheme.typography.displayLarge.copy(
                            color = ChampagneGold,
                            letterSpacing = 4.sp,
                            fontSize = 28.sp
                        )
                    )
                    Text(
                        text = "SYSTEM INTEGRITY & DEVIATION AUDITS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TextSecondary,
                            letterSpacing = 2.sp
                        )
                    )
                }
            }

            // Status sistem & enkripsi
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(0.5.dp, Color(0xFF2C2C2C))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Telemetry Monitor", style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(EmeraldMint.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(EmeraldMint))
                                Text("ONLINE", style = MaterialTheme.typography.labelSmall.copy(color = EmeraldMint, fontWeight = FontWeight.Bold))
                            }
                        }
                    }

                    Divider(color = Color(0xFF2B2B2B))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Deviation Guard", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                            Text("Active (Within Limits)", style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("API Encryption Status", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                            Text("E2EE AES-256", style = MaterialTheme.typography.bodySmall.copy(color = ChampagneGold, fontWeight = FontWeight.Bold))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Last Heartbeat Sync", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                            Text("Just Now", style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary))
                        }
                    }
                }
            }

            Text(
                text = "RECENT MARKET PHENOMENA LOGS",
                style = MaterialTheme.typography.labelMedium.copy(color = ChampagneGold, letterSpacing = 2.sp)
            )

            // Log fenomena market terbaru
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val alertLogs = listOf(
                    Pair("⚠️ High Ripple Deviation", "Bitcoin price shifted +4.2% in last 10 minutes. Cold storage locks temporarily bolstered."),
                    Pair("📈 Physical Bullion Record", "Sovereign Gold reached an all-time local high of Rp 1.472.000/gram."),
                    Pair("💼 Dividend Dispersal Alert", "Quarterly index dividends processed successfully. Capital reinvested in active core holdings.")
                )

                alertLogs.forEach { (title, desc) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(0.5.dp, Color(0xFF222222))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = title, style = MaterialTheme.typography.titleSmall.copy(color = ChampagneGold, fontWeight = FontWeight.Bold))
                            Text(text = desc, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                        }
                    }
                }
            }
        }
    } // Akhir tab ALERTS

    3 -> {
        // Tab Setelan Akun & Keamanan
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Bagian Setelan Akun
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SETTINGS",
                        style = MaterialTheme.typography.displayLarge.copy(
                            color = ChampagneGold,
                            letterSpacing = 4.sp,
                            fontSize = 28.sp
                        )
                    )
                    Text(
                        text = "VELORA SECURITY & PREFERENCES",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TextSecondary,
                            letterSpacing = 2.sp
                        )
                    )
                }
            }

            // Info Akun VIP Klien
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(0.5.dp, Color(0xFF2C2C2C))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(CharcoalCardAlt)
                            .border(1.5.dp, ChampagneGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("MC", style = MaterialTheme.typography.titleLarge.copy(color = ChampagneGold, fontWeight = FontWeight.Bold))
                    }

                    Column {
                        Text("MingCapital's", style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                        Text("VIP Private Wealth Client", style = MaterialTheme.typography.bodySmall.copy(color = ChampagneGold))
                        Text("ID: VL-983-ALPHA", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                    }
                }
            }

            Text(
                text = "PREMIUM AUTHORIZATION PREFERENCES",
                style = MaterialTheme.typography.labelMedium.copy(color = ChampagneGold, letterSpacing = 2.sp)
            )

            // Setelan Preferences (Biometrik dan Enkripsi)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(0.5.dp, Color(0xFF2C2C2C))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Verifikasi sidik jari/FaceID
                    var biometricEnabled by remember { mutableStateOf(true) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Biometric Verification", style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                            Text("Request Fingerprint/FaceID prior to high amount transfers.", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                        }
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = { biometricEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ObsidianBackground,
                                checkedTrackColor = ChampagneGold,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = CharcoalCardAlt
                            )
                        )
                    }

                    Divider(color = Color(0xFF2B2B2B), modifier = Modifier.padding(horizontal = 12.dp))

                    // Opsi enkripsi SQLite db local
                    var ledgerEncryption by remember { mutableStateOf(true) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Local Ledger Encryption", style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                            Text("Encrypt on-device Room SQLite database via SQLCipher keys.", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                        }
                        Switch(
                            checked = ledgerEncryption,
                            onCheckedChange = { ledgerEncryption = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ObsidianBackground,
                                checkedTrackColor = ChampagneGold,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = CharcoalCardAlt
                            )
                        )
                    }
                }
            }

            Text(
                text = "MAINTENANCE & SECURITY CONTROL",
                style = MaterialTheme.typography.labelMedium.copy(color = CrimsonRose, letterSpacing = 2.sp)
            )

            // Pilihan pembersihan data db permanen
            Button(
                onClick = {
                    viewModel.clearAllData()
                    Toast.makeText(context, "All Vault records purged securely!", Toast.LENGTH_LONG).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurface, contentColor = CrimsonRose),
                border = BorderStroke(1.dp, CrimsonRose.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Purge Database", tint = CrimsonRose)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Purge Database Records Permanently", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = CrimsonRose))
            }
        }
    } // Akhir tab SETTINGS
        }
    }

    // Tampilan dialog aksi modal
    if (showAddDialog) {
        VeloraAddDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, amt, type, cat ->
                viewModel.addTransaction(title, amt, type, cat)
                showAddDialog = false
            }
        )
    }

    if (showVoiceDialog) {
        SimulatedVoiceSheet(
            onDismiss = { showVoiceDialog = false },
            onSimulateCommand = { simulatedPhrase ->
                parseAndAddVoiceTransaction(simulatedPhrase, viewModel, context)
                showVoiceDialog = false
            }
        )
    }

    if (showOcrDialog) {
        SimulatedOcrScanSheet(
            onDismiss = { showOcrDialog = false },
            onMockScan = { name, amt, category ->
                viewModel.addTransaction(name, amt, "EXPENSE", category)
                showOcrDialog = false
                Toast.makeText(context, "OCR Scanned: $name extracted!", Toast.LENGTH_LONG).show()
            }
        )
    }
}

// ==========================================
// SUB-KOMPONEN TAMPILAN VISUAL INDIVIDUAL
// ==========================================

@Composable
fun AssetAllocationRow(
    name: String,
    pct: String,
    amount: String,
    indicatorColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(indicatorColor)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = pct,
            style = MaterialTheme.typography.labelMedium.copy(
                color = TextSecondary
            ),
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Text(
            text = amount,
            style = MaterialTheme.typography.labelLarge.copy(
                color = TextPrimary
            )
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionRowItem(
    transaction: VeloraTransaction,
    onDelete: () -> Unit
) {
    val isIncome = transaction.type == "INCOME"
    val colorAccent = if (isIncome) EmeraldMint else CrimsonRose
    val prefix = if (isIncome) "+" else "-"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CharcoalSurface)
            .border(0.5.dp, Color(0xFF2A2A2A), RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = {},
                onLongClick = { onDelete() }
            )
            .padding(14.dp)
            .testTag("transaction_row_${transaction.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Kontainer Pembungkus Ikon
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(CharcoalCardAlt),
            contentAlignment = Alignment.Center
        ) {
            val icon = getCategoryIcon(transaction.category, isIncome)
            Icon(
                imageVector = icon,
                contentDescription = transaction.category,
                tint = colorAccent,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Judul serta tanggal transaksi
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = transaction.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${transaction.category} • ${transaction.formattedDate}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Dana & tombol aksi hapus
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "$prefix ${formatCurrency(transaction.amount)}",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = colorAccent,
                    fontSize = 14.sp
                )
            )
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete item",
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

// Popup Dialog buat input atau tambah transaksi manual
@Composable
fun VeloraAddDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("EXPENSE") } // INCOME atau EXPENSE
    var category by remember { mutableStateOf("Stocks") }

    val categories = if (transactionType == "INCOME") {
        listOf("Salary", "Freelance", "Dividends", "Gifts", "Other")
    } else {
        listOf("Stocks", "Crypto", "Gold", "Lifestyle", "Rent", "Other")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, ChampagneGold, RoundedCornerShape(24.dp)),
            color = CharcoalSurface,
            contentColor = TextPrimary
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NEW FINANCIAL ENTRY",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = ChampagneGold,
                            letterSpacing = 2.sp,
                            fontSize = 18.sp
                        )
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Outlined.Close, contentDescription = "Close prompt", tint = ChampagneGold)
                    }
                }

                Divider(color = Color(0xFF2A2A2A))

                // Pemilih Tipe Transaksi (Income / Expense)
                Text(
                    text = "TRANSACTION TYPE",
                    style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary, letterSpacing = 1.sp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CharcoalCardAlt)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (transactionType == "INCOME") EmeraldMint.copy(alpha = 0.15f) else Color.Transparent
                            )
                            .clickable {
                                transactionType = "INCOME"
                                category = "Salary"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "INFLOW / INCOME",
                            color = if (transactionType == "INCOME") EmeraldMint else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (transactionType == "EXPENSE") CrimsonRose.copy(alpha = 0.15f) else Color.Transparent
                            )
                            .clickable {
                                transactionType = "EXPENSE"
                                category = "Stocks"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "OUTFLOW / EXPENSE",
                            color = if (transactionType == "EXPENSE") CrimsonRose else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                // Input Keterangan Deskripsi
                Text(
                    text = "DESCRIPTION",
                    style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary, letterSpacing = 1.sp)
                )
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("e.g. Bvlgari Diamond Ring", color = TextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_title"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = CharcoalCardAlt,
                        unfocusedContainerColor = CharcoalCardAlt,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedIndicatorColor = ChampagneGold,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                // Input Nominal Jumlah Uang
                Text(
                    text = "VALUE (IDR)",
                    style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary, letterSpacing = 1.sp)
                )
                TextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    placeholder = { Text("e.g. 5000000", color = TextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_amount"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = CharcoalCardAlt,
                        unfocusedContainerColor = CharcoalCardAlt,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedIndicatorColor = ChampagneGold,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )

                // Pemilih Kategori Transaksi
                Text(
                    text = "CATEGORY",
                    style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary, letterSpacing = 1.sp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val selected = category == cat
                        val selectedColor = if (transactionType == "INCOME") EmeraldMint else ChampagneGold
                        Card(
                            modifier = Modifier.clickable { category = cat },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) selectedColor.copy(alpha = 0.15f) else CharcoalCardAlt
                            ),
                            border = BorderStroke(1.dp, if (selected) selectedColor else Color.Transparent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (selected) selectedColor else TextSecondary,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tombol Konfirmasi Menyimpan Data
                Button(
                    onClick = {
                        val parsedAmt = amountText.toDoubleOrNull()
                        if (title.isNotBlank() && parsedAmt != null && parsedAmt > 0) {
                            onConfirm(title, parsedAmt, transactionType, category)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("dialog_submit_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ChampagneGold,
                        contentColor = ObsidianBackground
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "AUTHORIZE TRANSACTION",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.5.sp,
                            color = ObsidianBackground
                        )
                    )
                }
            }
        }
    }
}

// Dialog simulasi input suara / Microphone
@Composable
fun SimulatedVoiceSheet(
    onDismiss: () -> Unit,
    onSimulateCommand: (String) -> Unit
) {
    // Animasi gelombang hologram mic
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val waveScale1 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "w1"
    )

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, ChampagneGold, RoundedCornerShape(24.dp)),
            color = CharcoalSurface,
            contentColor = TextPrimary
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VOICE REQUISITION",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = ChampagneGold,
                            letterSpacing = 2.sp,
                            fontSize = 16.sp
                        )
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = ChampagneGold)
                    }
                }

                Divider(color = Color(0xFF2C2C2C))

                Text(
                    text = "Speak naturally to Velora Private Engine",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Efek visual lingkaran suara
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Lingkaran luar berdenyut
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = waveScale1
                                scaleY = waveScale1
                                alpha = (1.5f - waveScale1).coerceIn(0f, 1f)
                            }
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(ChampagneGold.copy(alpha = 0.2f))
                            .border(1.dp, ChampagneGold, CircleShape)
                    )

                    // Lingkaran inti tombol mic
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(ChampagneGold)
                            .clickable {
                                // Aksi tutup dialog atau panggil mic sistem
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice listening",
                            tint = ObsidianBackground,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Text(
                    text = "System is listening...",
                    style = MaterialTheme.typography.labelMedium.copy(color = ChampagneGold, letterSpacing = 1.sp)
                )

                Divider(color = Color(0xFF2C2C2C))

                Text(
                    text = "MOCK VOICE SAMPLES (TAP TO SIMULATE)",
                    style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary, letterSpacing = 1.sp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val samples = listOf(
                        "Received salary bonus 45000000",
                        "Bought tech stocks 12000000",
                        "Deposited crypto reserve 8000000",
                        "Fine luxury dinner expense 3500000"
                    )

                    samples.forEach { sample ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSimulateCommand(sample) },
                            colors = CardDefaults.cardColors(containerColor = CharcoalCardAlt),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "\"$sample\"",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = ChampagneGold,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Dialog kamera simulasi scan nota (OCR)
@Composable
fun SimulatedOcrScanSheet(
    onDismiss: () -> Unit,
    onMockScan: (String, Double, String) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ocr_laser")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ly"
    )

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, ChampagneGold, RoundedCornerShape(24.dp)),
            color = CharcoalSurface,
            contentColor = TextPrimary
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "OCR RECEIPTS SCANNING",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = ChampagneGold,
                            letterSpacing = 2.sp,
                            fontSize = 16.sp
                        )
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, tint = ChampagneGold, contentDescription = "Close ocr")
                    }
                }

                Divider(color = Color(0xFF2C2C2C))

                // Bingkai frame kamera hp simulasi perekaman
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                        .border(1.5.dp, Color(0xFF333333), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Teks pemberitahuan simulasi autofocus
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterCenterFocus,
                            contentDescription = "Ocr frame focus",
                            tint = ChampagneGold.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "RECEIPT SCANNING VIEWPORT",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = TextSecondary,
                                letterSpacing = 2.sp
                            )
                        )
                    }

                    // Efek laser pemindaian nota scan
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .offset(y = laserY.dp - 100.dp)
                            .background(Brush.horizontalGradient(listOf(Color.Transparent, ChampagneGold, Color.Transparent)))
                    )
                }

                Text(
                    text = "Position invoice within frame to verify entry",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                    textAlign = TextAlign.Center
                )

                Divider(color = Color(0xFF2C2C2C))

                Text(
                    text = "SELECT MOCK INVOICE TO SCAN",
                    style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary, letterSpacing = 1.sp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val invoices = listOf(
                        Triple("Bvlgari Jewelry Boutique Receipt", 24000000.0, "Lifestyle"),
                        Triple("Grand Hyatt Executive Suite", 15000000.0, "Lifestyle"),
                        Triple("Starbucks Reserve Premium Coffee", 450000.0, "Lifestyle")
                    )

                    invoices.forEach { (name, amount, category) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onMockScan(name, amount, category) },
                            colors = CardDefaults.cardColors(containerColor = CharcoalCardAlt),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ChampagneGold
                                        )
                                    )
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 10.sp,
                                            color = TextSecondary
                                        )
                                    )
                                }
                                Text(
                                    text = formatCurrency(amount),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// KUMPULAN FUNGSI PENDUKUNG (UTILITIES)
// ==========================================

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    return format.format(amount)
}

// Pemicu intent dialog perekam suara asli android
fun startSpeechToTextIntent(context: Context, launcher: androidx.activity.result.ActivityResultLauncher<Intent>) {
    try {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Authorize financial request (e.g. salary 5000000)")
        }
        launcher.launch(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Speech recognizer not supported on this device. Using simulation instead.", Toast.LENGTH_LONG).show()
    }
}

// Parser teks kalimat input suara
fun parseAndAddVoiceTransaction(phrase: String, viewModel: VeloraViewModel, context: Context) {
    val lower = phrase.lowercase()
    var amount = 1000000.0 // Cadangan nominal default
    var type = "EXPENSE"
    var category = "Other"
    var title = phrase

    // Ambil deretan angka nominal
    val numberRegex = "\\d+".toRegex()
    val match = numberRegex.find(lower)
    if (match != null) {
        amount = match.value.toDoubleOrNull() ?: 1000000.0
    }

    // Pencocokan kategori (Income vs Expense)
    if (lower.contains("salary") || lower.contains("income") || lower.contains("dividends") || lower.contains("bonus") || lower.contains("received") || lower.contains("gaji")) {
        type = "INCOME"
        category = "Salary"
        title = "Voice Requisition Inflow: " + phrase.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    } else {
        type = "EXPENSE"
        title = "Voice Requisition Outflow: " + phrase.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        when {
            lower.contains("stock") || lower.contains("stocks") || lower.contains("equity") -> {
                category = "Stocks"
            }
            lower.contains("crypto") || lower.contains("bitcoin") || lower.contains("digital") -> {
                category = "Crypto"
            }
            lower.contains("gold") || lower.contains("bullion") -> {
                category = "Gold"
            }
            lower.contains("food") || lower.contains("dinner") || lower.contains("boutique") || lower.contains("luxury") -> {
                category = "Lifestyle"
            }
        }
    }

    viewModel.addTransaction(title, amount, type, category)
    Toast.makeText(context, "Authorized via Voice Requisition!", Toast.LENGTH_LONG).show()
}

// Mapping ikon berdasarkan kategori pengeluaran/pendapatan
@Composable
fun getCategoryIcon(category: String, isIncome: Boolean): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        "Salary" -> Icons.Default.Work
        "Freelance" -> Icons.Default.WorkOutline
        "Dividends" -> Icons.Default.TrendingUp
        "Gifts" -> Icons.Default.CardGiftcard
        "Stocks" -> Icons.Default.ShowChart
        "Crypto" -> Icons.Default.CurrencyBitcoin
        "Gold" -> Icons.Default.Stars
        "Lifestyle" -> Icons.Default.Restaurant
        "Rent" -> Icons.Default.Home
        else -> if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
    }
}

// Komponen bar menu navigasi bawah halaman utama
@Composable
fun VeloraBottomNavigation(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = CharcoalSurface,
        border = BorderStroke(0.5.dp, Color(0xFF222222))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pilihan tab Home
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTabSelected(0) }
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (selectedTab == 0) ChampagneGold else Color.Transparent)
                )
                Text(
                    text = "HOME",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (selectedTab == 0) ChampagneGold else TextPrimary.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }

            // Pilihan tab Assets (Portofolio)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTabSelected(1) }
            ) {
                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = "Assets",
                    tint = if (selectedTab == 1) ChampagneGold else TextPrimary.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "ASSETS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (selectedTab == 1) ChampagneGold else TextPrimary.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }

            // Pilihan tab Alerts (Notifikasi)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTabSelected(2) }
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Alerts",
                    tint = if (selectedTab == 2) ChampagneGold else TextPrimary.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "ALERTS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (selectedTab == 2) ChampagneGold else TextPrimary.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }

            // Pilihan tab Settings (Pengaturan)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTabSelected(3) }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = if (selectedTab == 3) ChampagneGold else TextPrimary.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "SETTINGS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (selectedTab == 3) ChampagneGold else TextPrimary.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}
