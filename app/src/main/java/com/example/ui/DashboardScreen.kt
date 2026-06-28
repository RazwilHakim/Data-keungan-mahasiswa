package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BillEntity
import com.example.data.TransactionEntity
import com.example.ui.theme.MoneyGreen
import com.example.ui.theme.NeutralSlate
import com.example.ui.theme.SpendRed
import com.example.ui.theme.InfoBlue
import com.example.viewmodel.FinanceViewModel
import com.example.viewmodel.FinancialHealthState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Rupiah Helper Formatter
fun formatRupiah(amount: Double): String {
    return "Rp " + String.format("%,d", amount.toLong()).replace(',', '.')
}

// Format date timestamp to standard string
fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    return sdf.format(Date(timestamp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(0) }
    
    // Modal states
    var showAddTxDialog by remember { mutableStateOf(false) }
    var showAddBillDialog by remember { mutableStateOf(false) }
    var showSetBudgetDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    // State bindings
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val userInitials by viewModel.userInitials.collectAsStateWithLifecycle()
    val userAvatarUri by viewModel.userAvatarUri.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateAvatarUri(uri.toString())
        }
    }

    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val bills by viewModel.bills.collectAsStateWithLifecycle()
    val monthlyBudget by viewModel.monthlyBudget.collectAsStateWithLifecycle()
    val totalIncome by viewModel.totalIncome.collectAsStateWithLifecycle()
    val totalExpense by viewModel.totalExpense.collectAsStateWithLifecycle()
    val balance by viewModel.balance.collectAsStateWithLifecycle()
    val categoryExpenses by viewModel.categoryExpenses.collectAsStateWithLifecycle()
    val remainingBudget by viewModel.remainingBudget.collectAsStateWithLifecycle()
    val dailySpendingAllowance by viewModel.dailySpendingAllowance.collectAsStateWithLifecycle()
    val financialHealth by viewModel.financialHealth.collectAsStateWithLifecycle()

    if (!isLoggedIn) {
        LoginScreen(
            onLoginSuccess = { name, email, initials ->
                viewModel.updateProfile(name, email, initials)
                viewModel.setLoggedIn(true)
            },
            modifier = modifier
        )
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
                            in 0..11 -> "Selamat Pagi"
                            in 12..14 -> "Selamat Siang"
                            in 15..17 -> "Selamat Sore"
                            else -> "Selamat Malam"
                        }
                        Text(
                            text = "$greeting,",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = NeutralSlate,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .clickable { showEditProfileDialog = true }
                            .testTag("top_right_profile_avatar"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (userAvatarUri.isNotEmpty()) {
                            AsyncImage(
                                model = userAvatarUri,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = userInitials,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            val dividerColor = Color(0xFFF1F5F9) // Slate-100 equivalent
            NavigationBar(
                windowInsets = WindowInsets.navigationBars,
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                modifier = Modifier.drawBehind {
                    drawLine(
                        color = dividerColor,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
                    label = { Text("Beranda", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_home")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.PieChart, contentDescription = "Analisis") },
                    label = { Text("Analisis", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_analysis")
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Tagihan") },
                    label = { Text("Tagihan", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_bills")
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Profil") },
                    label = { Text("Profil", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_settings")
                )
            }
        },
        floatingActionButton = {
            if (currentTab == 0 || currentTab == 2) {
                FloatingActionButton(
                    onClick = {
                        if (currentTab == 0) showAddTxDialog = true
                        else showAddBillDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_add")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = if (currentTab == 0) "Catat Transaksi" else "Tambah Tagihan",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentTab) {
                0 -> SummaryTab(
                    transactions = transactions,
                    monthlyBudget = monthlyBudget,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    balance = balance,
                    dailySpendingAllowance = dailySpendingAllowance,
                    onDeleteTransaction = { viewModel.deleteTransaction(it) },
                    onSetBudgetClick = { showSetBudgetDialog = true },
                    onAddTxClick = { showAddTxDialog = true }
                )
                1 -> AnalysisTab(
                    categoryExpenses = categoryExpenses,
                    totalExpense = totalExpense,
                    monthlyBudget = monthlyBudget,
                    remainingBudget = remainingBudget,
                    financialHealth = financialHealth
                )
                2 -> BillsTab(
                    bills = bills,
                    onTogglePaid = { viewModel.toggleBillPaid(it) },
                    onDeleteBill = { viewModel.deleteBill(it) },
                    onAddBillClick = { showAddBillDialog = true }
                )
                3 -> SettingsTab(
                    monthlyBudget = monthlyBudget,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { viewModel.setDarkMode(it) },
                    userName = userName,
                    userEmail = userEmail,
                    userInitials = userInitials,
                    userAvatarUri = userAvatarUri,
                    onEditProfileClick = { showEditProfileDialog = true },
                    onAvatarClick = { galleryLauncher.launch("image/*") },
                    onUpdateProfile = { name, email, initials -> viewModel.updateProfile(name, email, initials) },
                    onLogout = { viewModel.setLoggedIn(false) },
                    onUpdateBudget = { viewModel.setMonthlyBudget(it) },
                    onClearAllData = { viewModel.clearAllData() }
                )
            }
        }
    }

    // Modal Dialogs
    if (showAddTxDialog) {
        AddTransactionDialog(
            onDismiss = { showAddTxDialog = false },
            onConfirm = { title, amount, type, category, note ->
                viewModel.addTransaction(title, amount, type, category, note)
                showAddTxDialog = false
            }
        )
    }

    if (showAddBillDialog) {
        AddBillDialog(
            onDismiss = { showAddBillDialog = false },
            onConfirm = { title, amount, daysOffset, category, note ->
                viewModel.addBill(title, amount, daysOffset, category, note)
                showAddBillDialog = false
            }
        )
    }

    if (showSetBudgetDialog) {
        SetBudgetDialog(
            currentBudget = monthlyBudget,
            onDismiss = { showSetBudgetDialog = false },
            onConfirm = { budget ->
                viewModel.setMonthlyBudget(budget)
                showSetBudgetDialog = false
            }
        )
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            currentName = userName,
            currentEmail = userEmail,
            currentInitials = userInitials,
            currentAvatarUri = userAvatarUri,
            onDismiss = { showEditProfileDialog = false },
            onAvatarClick = { galleryLauncher.launch("image/*") },
            onConfirm = { name, email, initials ->
                viewModel.updateProfile(name, email, initials)
                showEditProfileDialog = false
            }
        )
    }
}

// TAB 1: SUMMARY TAB
@Composable
fun SummaryTab(
    transactions: List<TransactionEntity>,
    monthlyBudget: Double,
    totalIncome: Double,
    totalExpense: Double,
    balance: Double,
    dailySpendingAllowance: Double,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onSetBudgetClick: () -> Unit,
    onAddTxClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("summary_tab_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Balance Card (Solid Indigo with 32dp Rounded Corners and Soft Glow)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("balance_hero_card"),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF4F46E5), // Indigo 600
                                    Color(0xFF4338CA)  // Indigo 700
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "SALDO AKTIF",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formatRupiah(balance),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.White.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowUpward,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Pemasukan",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formatRupiah(totalIncome),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.White.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowDownward,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Pengeluaran",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formatRupiah(totalExpense),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Student Budget Indicator & Recommended Daily Allowance (Clean Minimal White Card with Light Borders)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        // Soft elegant light outline for minimal feel
                        drawRoundRect(
                            color = Color(0xFFF1F5F9), // Slate 100
                            size = size,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
                        )
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "BATAS ANGGARAN BULANAN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeutralSlate,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatRupiah(monthlyBudget),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = onSetBudgetClick,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFEEF2FF), RoundedCornerShape(50)) // Light Indigo-50
                                .minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Anggaran",
                                tint = Color(0xFF4F46E5), // Indigo-600
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    // Dynamic Progress Indicator for budget control as in HTML
                    Spacer(modifier = Modifier.height(14.dp))
                    val progressRatio = if (monthlyBudget > 0) (totalExpense / monthlyBudget).toFloat() else 0f
                    val clampedProgress = progressRatio.coerceIn(0f, 1f)
                    val progressPercent = (clampedProgress * 100).toInt()
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pemakaian Anggaran",
                            fontSize = 11.sp,
                            color = NeutralSlate
                        )
                        Text(
                            text = "$progressPercent%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (progressRatio > 0.9f) SpendRed else Color(0xFF4F46E5)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { clampedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)),
                        color = if (progressRatio > 0.9f) SpendRed else Color(0xFF4F46E5),
                        trackColor = Color(0xFFF1F5F9)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFEF3C7)), // Warm Amber 100
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TipsAndUpdates,
                                contentDescription = null,
                                tint = Color(0xFFD97706), // Amber 600
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "REKOMENDASI BELANJA HARIAN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeutralSlate,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "${formatRupiah(dailySpendingAllowance)} / hari",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4F46E5) // Indigo-600
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "*Saran: Kurangi jajan di minggu terakhir agar aman.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = NeutralSlate,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Transactions List Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Catatan Transaksi",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(
                    onClick = onAddTxClick,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text("+ Catat", fontSize = 13.sp)
                }
            }
        }

        // Empty State or Transactions List
        if (transactions.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = NeutralSlate.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Belum Ada Transaksi",
                        fontWeight = FontWeight.Medium,
                        color = NeutralSlate
                    )
                    Text(
                        text = "Silakan catat pengeluaran/pemasukan harian Anda.",
                        fontSize = 12.sp,
                        color = NeutralSlate.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(transactions, key = { it.id }) { tx ->
                TransactionRow(
                    transaction = tx,
                    onDelete = { onDeleteTransaction(tx) }
                )
            }
        }
    }
}

@Composable
fun TransactionRow(
    transaction: TransactionEntity,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus Transaksi?") },
            text = { Text("Apakah Anda yakin ingin menghapus catatan '${transaction.title}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    }
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDeleteConfirm = true }
            .drawBehind {
                drawRoundRect(
                    color = Color(0xFFF1F5F9), // Slate 100
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
                )
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Category Icon Badge with 12dp Rounded corners
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (transaction.type == "INCOME") MoneyGreen.copy(alpha = 0.1f)
                            else SpendRed.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(transaction.category),
                        contentDescription = null,
                        tint = if (transaction.type == "INCOME") MoneyGreen else SpendRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                
                Column {
                    Text(
                        text = transaction.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = transaction.category,
                            fontSize = 11.sp,
                            color = Color(0xFF4F46E5), // Indigo 600
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "•",
                            fontSize = 11.sp,
                            color = NeutralSlate
                        )
                        Text(
                            text = formatDate(transaction.timestamp),
                            fontSize = 11.sp,
                            color = NeutralSlate
                        )
                    }
                    if (transaction.note.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = transaction.note,
                            fontSize = 11.sp,
                            color = NeutralSlate.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            Text(
                text = (if (transaction.type == "INCOME") "+" else "-") + formatRupiah(transaction.amount),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = if (transaction.type == "INCOME") MoneyGreen else SpendRed,
                letterSpacing = (-0.3).sp
            )
        }
    }
}

// TAB 2: ANALYSIS TAB
@Composable
fun AnalysisTab(
    categoryExpenses: Map<String, Double>,
    totalExpense: Double,
    monthlyBudget: Double,
    remainingBudget: Double,
    financialHealth: FinancialHealthState
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("analysis_tab_container"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Health Check Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRoundRect(
                        color = Color(0xFFF1F5F9), // Slate 100
                        size = size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
                    )
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(financialHealth.colorHex))
                    )
                    Text(
                        text = "Status Dompet: ${financialHealth.status}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.2).sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = financialHealth.advice,
                    fontSize = 12.sp,
                    color = NeutralSlate,
                    lineHeight = 17.sp
                )
            }
        }

        // Section: Budget Utilization Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRoundRect(
                        color = Color(0xFFF1F5F9), // Slate 100
                        size = size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
                    )
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Pemakaian Anggaran Bulanan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.1).sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                
                val progressRatio = if (monthlyBudget > 0) (totalExpense / monthlyBudget).toFloat() else 0f
                val clampedProgress = progressRatio.coerceIn(0f, 1f)
                val progressColor = when {
                    progressRatio > 0.95f -> SpendRed
                    progressRatio > 0.75f -> Color(0xFFF59E0B) // Amber 500
                    else -> MoneyGreen
                }

                LinearProgressIndicator(
                    progress = { clampedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(50)),
                    color = progressColor,
                    trackColor = Color(0xFFF1F5F9)
                )
                Spacer(modifier = Modifier.height(10.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Terpakai: ${String.format("%.1f", progressRatio * 100)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeutralSlate
                    )
                    Text(
                        text = "Sisa: ${formatRupiah(remainingBudget)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MoneyGreen
                    )
                }
            }
        }

        // Section: Category Analysis
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRoundRect(
                        color = Color(0xFFF1F5F9), // Slate 100
                        size = size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
                    )
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Analisis Pengeluaran per Kategori",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Evaluasi pos jajan Anda untuk mencari penghematan potensial.",
                    fontSize = 11.sp,
                    color = NeutralSlate
                )
                Spacer(modifier = Modifier.height(20.dp))

                if (categoryExpenses.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.InsertChartOutlined,
                            contentDescription = null,
                            tint = NeutralSlate.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Belum Ada Data Pengeluaran",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = NeutralSlate
                        )
                    }
                } else {
                    val sortedExpenses = categoryExpenses.toList().sortedByDescending { it.second }
                    val maxAmount = sortedExpenses.firstOrNull()?.second ?: 1.0

                    sortedExpenses.forEach { (category, amount) ->
                        val ratio = (amount / totalExpense).toFloat()
                        val barScale = (amount / maxAmount).toFloat()
                        
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFEEF2FF)), // Light Indigo 50
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getCategoryIcon(category),
                                            contentDescription = null,
                                            tint = Color(0xFF4F46E5), // Indigo 600
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Text(
                                        text = category,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "${formatRupiah(amount)} (${String.format("%.1f", ratio * 100)}%)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { barScale },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = Color(0xFF4F46E5), // Indigo 600
                                trackColor = Color(0xFFF1F5F9)
                            )
                        }
                    }
                }
            }
        }

        // Section: Curated Tips specifically for Indonesian College Students
        Text(
            text = "Rekomendasi Hemat Mahasiswa",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            letterSpacing = 0.2.sp
        )

        StudentTipCard(
            icon = Icons.Default.Restaurant,
            title = "Strategi Makan Warteg & Masak Nasi",
            description = "Masaklah nasi sendiri di kosan Anda. Cukup beli lauk pauk di Warteg terdekat. Kebiasaan kecil ini bisa menghemat budget makan hingga 40% setiap bulannya!"
        )

        StudentTipCard(
            icon = Icons.Default.School,
            title = "Persiapan Tabungan UKT",
            description = "UKT semesteran terasa berat jika dibayar sekaligus. Sisihkan uang saku Rp 10.000 s/d Rp 15.000 per hari di celengan/rekening terpisah agar saat pendaftaran ulang saldo sudah siap."
        )

        StudentTipCard(
            icon = Icons.Default.DirectionsCar,
            title = "Transportasi Hemat & Tebengan",
            description = "Bagi Anda yang membawa motor, gunakan peta tercepat untuk hemat bensin atau atur tebengan bergiliran bersama teman kos yang sekelas."
        )
    }
}

@Composable
fun StudentTipCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = Color(0xFFF1F5F9), // Slate 100
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
                )
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFEEF2FF)), // Indigo 50
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF4F46E5), // Indigo 600
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = NeutralSlate,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

// TAB 3: BILLS TAB
@Composable
fun BillsTab(
    bills: List<BillEntity>,
    onTogglePaid: (BillEntity) -> Unit,
    onDeleteBill: (BillEntity) -> Unit,
    onAddBillClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("bills_tab_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Explanatory info banner (Clean Indigo accent border)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawRoundRect(
                            color = Color(0xFFE0E7FF), // Indigo 100
                            size = size,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
                        )
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F3FF) // Soft indigo/violet hint background
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEEF2FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationImportant,
                            contentDescription = null,
                            tint = Color(0xFF4F46E5), // Indigo 600
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pengingat Tagihan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF312E81) // Deep Indigo
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Pantau tagihan kost, iuran WiFi, hingga UKT. Menandai tagihan lunas akan mencatat pengeluaran secara otomatis ke laporan.",
                            fontSize = 11.sp,
                            color = Color(0xFF4F46E5),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Title Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daftar Tagihan Mahasiswa",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 0.2.sp
                )
                TextButton(
                    onClick = onAddBillClick,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text("+ Tambah", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4F46E5))
                }
            }
        }

        if (bills.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = null,
                        tint = NeutralSlate.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Semua Tagihan Beres!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = NeutralSlate
                    )
                    Text(
                        text = "Belum ada pengingat tagihan yang ditambahkan.",
                        fontSize = 11.sp,
                        color = NeutralSlate.copy(alpha = 0.8f)
                    )
                }
            }
        } else {
            items(bills, key = { it.id }) { bill ->
                BillRow(
                    bill = bill,
                    onTogglePaid = { onTogglePaid(bill) },
                    onDelete = { onDeleteBill(bill) }
                )
            }
        }
    }
}

@Composable
fun BillRow(
    bill: BillEntity,
    onTogglePaid: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus Tagihan?") },
            text = { Text("Yakin ingin menghapus pengingat '${bill.title}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    }
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }

    val daysLeft = ((bill.dueDate - System.currentTimeMillis()) / (24 * 3600 * 1000)).toInt()
    val isOverdue = daysLeft < 0 && !bill.isPaid
    
    val indicatorColor = when {
        bill.isPaid -> MoneyGreen
        isOverdue -> SpendRed
        daysLeft <= 3 -> Color(0xFFF59E0B) // Amber/Orange Warning
        else -> InfoBlue
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = Color(0xFFF1F5F9), // Slate 100
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
                )
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Status indicator line with clean rounded edge
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(44.dp)
                        .clip(RoundedCornerShape(50))
                        .background(indicatorColor)
                )
                Spacer(modifier = Modifier.width(14.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bill.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (bill.isPaid) NeutralSlate else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Jatuh Tempo: ${formatDate(bill.dueDate)}",
                        fontSize = 11.sp,
                        color = NeutralSlate
                    )
                    
                    // Days left / status info
                    val statusText = when {
                        bill.isPaid -> "Sudah Lunas"
                        isOverdue -> "Terlewat ${-daysLeft} Hari"
                        daysLeft == 0 -> "Hari Ini!"
                        else -> "$daysLeft Hari Lagi"
                    }
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = indicatorColor
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = formatRupiah(bill.amount),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = if (bill.isPaid) NeutralSlate else MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Paid button toggle
                    Button(
                        onClick = onTogglePaid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (bill.isPaid) Color(0xFFF1F5F9) else Color(0xFFEEF2FF),
                            contentColor = if (bill.isPaid) NeutralSlate else Color(0xFF4F46E5)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .minimumInteractiveComponentSize()
                    ) {
                        if (bill.isPaid) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Lunas", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text("Bayar", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Trash icon with customized padding
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFFFEF2F2), RoundedCornerShape(50)) // Light red background hint
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = SpendRed,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

// TAB 4: SETTINGS & TARGET TAB
@Composable
fun SettingsTab(
    monthlyBudget: Double,
    totalIncome: Double,
    totalExpense: Double,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    userName: String,
    userEmail: String,
    userInitials: String,
    userAvatarUri: String,
    onEditProfileClick: () -> Unit,
    onAvatarClick: () -> Unit,
    onUpdateProfile: (String, String, String) -> Unit,
    onLogout: () -> Unit,
    onUpdateBudget: (Double) -> Unit,
    onClearAllData: () -> Unit
) {
    var budgetText by remember { mutableStateOf(monthlyBudget.toLong().toString()) }
    var showSavedMessage by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("settings_tab_container"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App / User Profile Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRoundRect(
                        color = Color(0xFFF1F5F9), // Slate 100
                        size = size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
                    )
                }
                .clickable { onEditProfileClick() }
                .testTag("settings_profile_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFEEF2FF)) // Indigo 50
                        .clickable { onAvatarClick() }
                        .testTag("settings_avatar_box"),
                    contentAlignment = Alignment.Center
                ) {
                    if (userAvatarUri.isNotEmpty()) {
                        AsyncImage(
                            model = userAvatarUri,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = userInitials,
                            color = Color(0xFF4F46E5),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = userName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = userEmail,
                    fontSize = 13.sp,
                    color = NeutralSlate
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFEEF2FF))
                            .clickable { onEditProfileClick() }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color(0xFF4F46E5),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Ubah Profil",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4F46E5)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFEEF2FF))
                            .clickable { onAvatarClick() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("change_photo_row_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = Color(0xFF4F46E5),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Ganti Foto",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4F46E5)
                        )
                    }
                }
            }
        }

        // Mode Gelap Terang Switch Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRoundRect(
                        color = Color(0xFFF1F5F9), // Slate 100
                        size = size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
                    )
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDarkMode) Color(0xFF312E81) else Color(0xFFFEF3C7)), // Indigo-900 / Amber-100
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            tint = if (isDarkMode) Color(0xFF818CF8) else Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Mode Gelap",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isDarkMode) "Aktif" else "Nonaktif",
                            fontSize = 11.sp,
                            color = NeutralSlate
                        )
                    }
                }
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { onToggleDarkMode(it) },
                    modifier = Modifier.testTag("settings_dark_mode_switch")
                )
            }
        }

        // Edit Profile Form Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRoundRect(
                        color = Color(0xFFF1F5F9), // Slate 100
                        size = size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
                    )
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            var localName by remember(userName) { mutableStateOf(userName) }
            var localEmail by remember(userEmail) { mutableStateOf(userEmail) }
            var localInitials by remember(userInitials) { mutableStateOf(userInitials) }
            var showProfileSavedMessage by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Edit Profil Pengguna",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Sesuaikan identitas profil Anda sebagai mahasiswa untuk dipersonalisasi di dashboard.",
                    fontSize = 11.sp,
                    color = NeutralSlate
                )

                OutlinedTextField(
                    value = localName,
                    onValueChange = { localName = it },
                    label = { Text("Nama Lengkap") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("settings_profile_name"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4F46E5),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                OutlinedTextField(
                    value = localEmail,
                    onValueChange = { localEmail = it },
                    label = { Text("Alamat Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("settings_profile_email"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4F46E5),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                OutlinedTextField(
                    value = localInitials,
                    onValueChange = { if (it.length <= 3) localInitials = it.uppercase() },
                    label = { Text("Inisial Avatar") },
                    placeholder = { Text("Maksimal 3 karakter, cth: BR") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("settings_profile_initials"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4F46E5),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                Button(
                    onClick = {
                        if (localName.isNotBlank() && localEmail.isNotBlank()) {
                            onUpdateProfile(localName.trim(), localEmail.trim(), localInitials.trim().ifBlank { "U" })
                            showProfileSavedMessage = true
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4F46E5),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("settings_profile_save_button")
                ) {
                    Text("Perbarui Data Profil", fontWeight = FontWeight.Bold)
                }

                AnimatedVisibility(visible = showProfileSavedMessage) {
                    Text(
                        text = "✓ Profil berhasil diperbarui!",
                        color = MoneyGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    LaunchedEffect(showProfileSavedMessage) {
                        kotlinx.coroutines.delay(2000)
                        showProfileSavedMessage = false
                    }
                }
            }
        }

        // Adjust Budget Form
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRoundRect(
                        color = Color(0xFFF1F5F9), // Slate 100
                        size = size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
                    )
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Atur Limit Anggaran Bulanan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Target pengeluaran bulanan yang ingin Anda capai agar tabungan tetap terjaga.",
                    fontSize = 11.sp,
                    color = NeutralSlate
                )
                
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { input -> 
                        if (input.all { it.isDigit() }) budgetText = input 
                    },
                    label = { Text("Anggaran Bulanan (Rp)") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("settings_budget_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4F46E5),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                Button(
                    onClick = {
                        val amt = budgetText.toDoubleOrNull() ?: 0.0
                        onUpdateBudget(amt)
                        showSavedMessage = true
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4F46E5), // Indigo 600
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("settings_save_budget_button")
                ) {
                    Text("Simpan Konfigurasi Anggaran", fontWeight = FontWeight.Bold)
                }

                AnimatedVisibility(visible = showSavedMessage) {
                    Text(
                        text = "✓ Anggaran bulanan berhasil diperbarui!",
                        color = MoneyGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Reset Data Utility Card (Minimalist clean look with soft red hint)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRoundRect(
                        color = Color(0xFFFEE2E2), // Red 100 border
                        size = size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
                    )
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFF5F5) // Very light red hint background
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Kosongkan Semua Catatan & Saldo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SpendRed
                )
                Text(
                    text = "Aksi ini akan menghapus semua catatan pemasukan, pengeluaran, dan tagihan Anda untuk memulai ulang saldo aktif dari Rp 0 secara bersih.",
                    fontSize = 11.sp,
                    color = NeutralSlate,
                    lineHeight = 15.sp
                )
                
                Button(
                    onClick = { showResetConfirm = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpendRed,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("settings_reset_all_button")
                ) {
                    Text("Atur Ulang ke Rp 0 Sekarang", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Elegant M3 confirmation dialog
        if (showResetConfirm) {
            AlertDialog(
                onDismissRequest = { showResetConfirm = false },
                title = {
                    Text(
                        text = "Atur Ulang Data?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Text(
                        text = "Apakah Anda yakin ingin menghapus semua riwayat catatan keuangan dan memulai dari Rp 0 kembali? Tindakan ini tidak dapat dibatalkan.",
                        fontSize = 13.sp,
                        color = NeutralSlate,
                        lineHeight = 18.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onClearAllData()
                            showResetConfirm = false
                        }
                    ) {
                        Text(
                            text = "Ya, Atur Ulang",
                            fontWeight = FontWeight.Bold,
                            color = SpendRed
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetConfirm = false }) {
                        Text(text = "Batal", color = NeutralSlate)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = MaterialTheme.colorScheme.surface
            )
        }

        // Logout Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRoundRect(
                        color = Color(0xFFE2E8F0), // Slate 200 border
                        size = size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
                    )
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Akses Keamanan Akun",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Keluar dari sesi aktif ini untuk mengamankan data Anda di perangkat.",
                    fontSize = 11.sp,
                    color = NeutralSlate,
                    lineHeight = 15.sp
                )
                
                Button(
                    onClick = { onLogout() },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF64748B), // Slate-500
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("settings_logout_button")
                ) {
                    Text("Keluar dari Akun Saku", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Brief FAQ / Information section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRoundRect(
                        color = Color(0xFFF1F5F9), // Slate 100
                        size = size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
                    )
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Panduan Fitur & Manfaat",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))
                
                FaqRow(
                    q = "Bagaimana rumus Rekomendasi Jajan Harian?",
                    a = "Sisa anggaran bulanan Anda dibagi dengan sisa hari kalender di bulan ini secara real-time. Jika Anda berhemat hari ini, budget jajan hari esok akan bertambah otomatis!"
                )
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(12.dp))
                FaqRow(
                    q = "Mengapa membayar tagihan mencatat transaksi?",
                    a = "Agar keuangan konsisten. Saat menandai tagihan 'Lunas', dana otomatis dihitung sebagai pengeluaran di dashboard utama, menghemat waktu Anda dari mencatat ulang."
                )
            }
        }
    }
}

@Composable
fun FaqRow(q: String, a: String) {
    Column {
        Text(
            text = "Q: $q",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color(0xFF4F46E5) // Indigo 600
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(text = "A: $a", fontSize = 11.sp, color = NeutralSlate, lineHeight = 15.sp)
    }
}

// Category helper maps
fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "Makan & Minum" -> Icons.Default.ShoppingCart
        "Kost/Sewa", "Kost" -> Icons.Default.Home
        "Transportasi" -> Icons.Default.DirectionsCar
        "Alat Tulis & Buku" -> Icons.Default.Book
        "Pendidikan/UKT", "UKT" -> Icons.Default.School
        "Hiburan" -> Icons.Default.Movie
        "WiFi" -> Icons.Default.Wifi
        "Langganan" -> Icons.Default.Star
        else -> Icons.Default.MoreHoriz
    }
}

// Dialog: Add Transaction
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("EXPENSE") } // "INCOME" or "EXPENSE"
    var category by remember { mutableStateOf("Makan & Minum") }
    var note by remember { mutableStateOf("") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val categories = listOf(
        "Makan & Minum",
        "Kost/Sewa",
        "Transportasi",
        "Alat Tulis & Buku",
        "Pendidikan/UKT",
        "Hiburan",
        "Lainnya"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_tx_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Catat Transaksi Baru",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (isError) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            color = SpendRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                
                // Transaction Type Segmented Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { type = "EXPENSE" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "EXPENSE") SpendRed else MaterialTheme.colorScheme.outlineVariant,
                            contentColor = if (type == "EXPENSE") Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Pengeluaran", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { type = "INCOME" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "INCOME") MoneyGreen else MaterialTheme.colorScheme.outlineVariant,
                            contentColor = if (type == "INCOME") Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Pemasukan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { 
                        title = it
                        isError = false
                    },
                    label = { Text("Nama Transaksi (e.g. Beli Nasi)") },
                    modifier = Modifier.fillMaxWidth().testTag("tx_input_title"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { input -> 
                        if (input.all { it.isDigit() }) {
                            amount = input
                            isError = false
                        }
                    },
                    label = { Text("Jumlah Uang (Rp)") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("tx_input_amount"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Category selection dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("tx_input_category"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan Opsional") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: 0.0
                            if (title.isBlank()) {
                                isError = true
                                errorMessage = "Nama transaksi tidak boleh kosong!"
                            } else if (amount.isBlank() || amt <= 0.0) {
                                isError = true
                                errorMessage = "Masukkan jumlah uang yang valid (lebih dari Rp 0)!"
                            } else {
                                onConfirm(title.trim(), amt, type, category, note.trim())
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

// Dialog: Add Bill Reminder
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBillDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Int, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Kost") }
    var daysOffset by remember { mutableStateOf("5") }
    var note by remember { mutableStateOf("") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val categories = listOf("Kost", "UKT", "WiFi", "Langganan", "Lainnya")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_bill_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Tambah Pengingat Tagihan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (isError) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            color = SpendRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { 
                        title = it
                        isError = false
                    },
                    label = { Text("Nama Tagihan (e.g. WiFi Kamar)") },
                    modifier = Modifier.fillMaxWidth().testTag("bill_input_title"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { input -> 
                        if (input.all { it.isDigit() }) {
                            amount = input
                            isError = false
                        }
                    },
                    label = { Text("Jumlah Tagihan (Rp)") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("bill_input_amount"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = daysOffset,
                    onValueChange = { input -> 
                        if (input.all { it.isDigit() }) {
                            daysOffset = input
                            isError = false
                        }
                    },
                    label = { Text("Tenggat Waktu (Berapa hari lagi?)") },
                    suffix = { Text("Hari") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("bill_input_offset"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Category selection dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori Tagihan") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan Pendukung (e.g. Rek Transfer)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: 0.0
                            val offset = daysOffset.toIntOrNull() ?: 0
                            if (title.isBlank()) {
                                isError = true
                                errorMessage = "Nama tagihan tidak boleh kosong!"
                            } else if (amount.isBlank() || amt <= 0.0) {
                                isError = true
                                errorMessage = "Masukkan jumlah tagihan yang valid (lebih dari Rp 0)!"
                            } else if (daysOffset.isBlank() || offset < 0) {
                                isError = true
                                errorMessage = "Masukkan tenggat waktu hari yang valid (minimal 0 hari)!"
                            } else {
                                onConfirm(title.trim(), amt, offset, category, note.trim())
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

// Dialog: Set Budget Quick Dialog
@Composable
fun SetBudgetDialog(
    currentBudget: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var budgetText by remember { mutableStateOf(currentBudget.toLong().toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Perbarui Anggaran Bulanan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { input -> 
                        if (input.all { it.isDigit() }) budgetText = input 
                    },
                    label = { Text("Batas Anggaran Bulanan (Rp)") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = budgetText.toDoubleOrNull() ?: 0.0
                            if (amt >= 0) {
                                onConfirm(amt)
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    currentName: String,
    currentEmail: String,
    currentInitials: String,
    currentAvatarUri: String,
    onDismiss: () -> Unit,
    onAvatarClick: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var email by remember { mutableStateOf(currentEmail) }
    var initials by remember { mutableStateOf(currentInitials) }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Edit Profil Pengguna",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Avatar preview and picker inside dialog
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFEEF2FF)) // Indigo 50
                        .clickable { onAvatarClick() }
                        .testTag("dialog_avatar_box"),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentAvatarUri.isNotEmpty()) {
                        AsyncImage(
                            model = currentAvatarUri,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = initials.ifBlank { "BR" },
                            color = Color(0xFF4F46E5),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                    }
                    // Overlay camera icon
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change photo",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = "Ketuk foto untuk ganti dari galeri",
                    fontSize = 11.sp,
                    color = NeutralSlate,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isError) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            color = SpendRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it 
                        isError = false
                    },
                    label = { Text("Nama Lengkap") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_profile_name"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4F46E5),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it 
                        isError = false
                    },
                    label = { Text("Alamat Email") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_profile_email"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4F46E5),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                OutlinedTextField(
                    value = initials,
                    onValueChange = { 
                        if (it.length <= 3) {
                            initials = it.uppercase()
                            isError = false
                        }
                    },
                    label = { Text("Inisial Avatar") },
                    placeholder = { Text("Maksimal 3 karakter, cth: BR") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_profile_initials"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4F46E5),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = NeutralSlate)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                isError = true
                                errorMessage = "Nama lengkap tidak boleh kosong!"
                            } else if (email.isBlank() || !email.contains("@")) {
                                isError = true
                                errorMessage = "Harap masukkan alamat email yang valid!"
                            } else {
                                onConfirm(
                                    name.trim(),
                                    email.trim(),
                                    initials.trim().ifBlank {
                                        val words = name.trim().split("\\s+".toRegex())
                                        if (words.size == 1) words[0].take(2).uppercase()
                                        else (words[0].take(1) + words[1].take(1)).uppercase()
                                    }
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4F46E5),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Simpan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
