package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val viewModel: FinanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingState by viewModel.setting.collectAsStateWithLifecycle()
            val languageCode = settingState?.languageCode ?: "ar"
            val themeMode = settingState?.themeMode ?: "dark"

            SmartFinanceTheme(themeMode = themeMode) {
                val layoutDirection = if (languageCode == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr
                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    AppNavigationWrapper(viewModel = viewModel)
                }
            }
        }
    }
}

enum class Screen {
    SPLASH,
    ONBOARDING,
    MAIN_APP
}

enum class ActiveTab {
    DASHBOARD,
    EXPENSES,
    INCOME,
    GOALS,
    ANALYTICS,
    REPORTS,
    ACHIEVEMENTS,
    SETTINGS,
    ABOUT,
    PROFILE
}

@Composable
fun AppNavigationWrapper(viewModel: FinanceViewModel) {
    var currentScreen by remember { mutableStateOf(Screen.SPLASH) }

    // Read onboarding status from SharedPref
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("smart_finance_prefs", Context.MODE_PRIVATE) }
    val onboardingCompleted = remember { sharedPrefs.getBoolean("onboarding_completed", false) }

    when (currentScreen) {
        Screen.SPLASH -> {
            SplashScreen(onSplashFinished = {
                currentScreen = if (onboardingCompleted) Screen.MAIN_APP else Screen.ONBOARDING
            })
        }
        Screen.ONBOARDING -> {
            OnboardingScreen(onOnboardingFinished = {
                sharedPrefs.edit().putBoolean("onboarding_completed", true).apply()
                currentScreen = Screen.MAIN_APP
            }, lang = viewModel.setting.collectAsStateWithLifecycle().value?.languageCode ?: "ar")
        }
        Screen.MAIN_APP -> {
            MainAppLayout(viewModel = viewModel)
        }
    }
}

// ==================== SPLASH SCREEN ====================
@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1500)
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2500)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CosmicBlack, SlateNavy)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Animated Glowing Logo
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFF6366F1).copy(alpha = 0.1f * alphaAnim))
                    .border(2.dp, Brush.radialGradient(listOf(Color(0xFF6366F1), Color(0xFF3B82F6))), RoundedCornerShape(32.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Wallet,
                    contentDescription = "Wallet Logo",
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "المستشار المالي",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.sp
            )

            Text(
                text = "Smart Finance Tracker Pro",
                fontSize = 14.sp,
                color = Color(0xFF94A3B8),
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(120.dp))

            // Developer Credit
            Text(
                text = "تطوير م/ محمد صبري",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF818CF8)
            )

            Text(
                text = "Professional Challenger App",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}

// ==================== ONBOARDING SCREEN ====================
@Composable
fun OnboardingScreen(onOnboardingFinished: () -> Unit, lang: String) {
    var currentPage by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    val pages = listOf(
        OnboardingData(
            title = Localization.get("onboarding_1_title", lang),
            description = Localization.get("onboarding_1_desc", lang),
            icon = Icons.Default.TrendingDown
        ),
        OnboardingData(
            title = Localization.get("onboarding_2_title", lang),
            description = Localization.get("onboarding_2_desc", lang),
            icon = Icons.Default.TrackChanges
        ),
        OnboardingData(
            title = Localization.get("onboarding_3_title", lang),
            description = Localization.get("onboarding_3_desc", lang),
            icon = Icons.Default.Savings
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Upper Brand watermark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Localization.get("developer_credit", lang),
                    fontSize = 12.sp,
                    color = Color(0xFF6366F1),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${currentPage + 1}/${pages.size}",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            // Central illustration card
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(36.dp))
                        .background(SlateNavy),
                    contentAlignment = Alignment.Center
                ) {
                    // Load the custom generated hero illustration
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Illustration Icon",
                        modifier = Modifier.size(120.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = pages[currentPage].title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = pages[currentPage].description,
                    fontSize = 15.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Bottom controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip Button
                TextButton(onClick = onOnboardingFinished) {
                    Text(
                        text = if (lang == "ar") "تخطي" else "Skip",
                        color = Color(0xFF94A3B8)
                    )
                }

                // Page indicator pills
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    pages.forEachIndexed { idx, _ ->
                        Box(
                            modifier = Modifier
                                .size(width = if (idx == currentPage) 24.dp else 8.dp, height = 8.dp)
                                .clip(CircleShape)
                                .background(if (idx == currentPage) Color(0xFF6366F1) else Color(0x33FFFFFF))
                        )
                    }
                }

                // Next Button
                Button(
                    onClick = {
                        if (currentPage < pages.size - 1) {
                            currentPage++
                        } else {
                            onOnboardingFinished()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                ) {
                    Text(
                        text = if (currentPage == pages.size - 1) {
                            if (lang == "ar") "ابدأ الآن" else "Get Started"
                        } else {
                            if (lang == "ar") "التالي" else "Next"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

data class OnboardingData(
    val title: String,
    val description: String,
    val icon: ImageVector
)

// ==================== MAIN LAYOUT WITH DRAWER ====================
@Composable
fun MainAppLayout(viewModel: FinanceViewModel) {
    val settingState by viewModel.setting.collectAsStateWithLifecycle()
    val languageCode = settingState?.languageCode ?: "ar"

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var activeTab by remember { mutableStateOf(ActiveTab.DASHBOARD) }

    // Security screen check
    val isLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()
    if (isLocked) {
        PinLockScreen(viewModel = viewModel, lang = languageCode)
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = CosmicBlack,
                modifier = Modifier.width(300.dp)
            ) {
                DrawerContent(
                    viewModel = viewModel,
                    activeTab = activeTab,
                    onTabSelected = { tab ->
                        activeTab = tab
                        scope.launch { drawerState.close() }
                    },
                    lang = languageCode
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Top Credit Bar from Sophisticated Dark style
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SlateNavy)
                            .border(width = 1.dp, color = Color.White.copy(alpha = 0.05f))
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (languageCode == "ar") "تطوير م/ محمد صبري" else "Developed by Eng. Mohammed Sabry",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                    }
                    AppHeaderBar(
                        lang = languageCode,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        viewModel = viewModel,
                        onProfileClicked = { activeTab = ActiveTab.PROFILE }
                    )
                }
            },
            bottomBar = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Premium footer credit bar from Sophisticated Dark
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CosmicBlack)
                            .border(width = 1.dp, color = Color.White.copy(alpha = 0.05f))
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (languageCode == "ar") "© جميع الحقوق محفوظة - تطوير م/ محمد صبري" else "© All rights reserved - Developed by Eng. Mohammed Sabry",
                            fontSize = 8.sp,
                            color = TextSecondaryMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    label = "TabContent"
                ) { tab ->
                    when (tab) {
                        ActiveTab.DASHBOARD -> DashboardScreen(viewModel = viewModel, lang = languageCode)
                        ActiveTab.EXPENSES -> ExpensesScreen(viewModel = viewModel, lang = languageCode)
                        ActiveTab.INCOME -> IncomeScreen(viewModel = viewModel, lang = languageCode)
                        ActiveTab.GOALS -> GoalsScreen(viewModel = viewModel, lang = languageCode)
                        ActiveTab.ANALYTICS -> AnalyticsScreen(viewModel = viewModel, lang = languageCode)
                        ActiveTab.REPORTS -> ReportsScreen(viewModel = viewModel, lang = languageCode)
                        ActiveTab.ACHIEVEMENTS -> AchievementsScreen(viewModel = viewModel, lang = languageCode)
                        ActiveTab.SETTINGS -> SettingsScreen(viewModel = viewModel, lang = languageCode)
                        ActiveTab.ABOUT -> AboutScreen(viewModel = viewModel, lang = languageCode)
                        ActiveTab.PROFILE -> ProfileScreen(viewModel = viewModel, lang = languageCode)
                    }
                }
            }
        }
    }
}

// ==================== APP HEADER BAR ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeaderBar(
    lang: String,
    onOpenDrawer: () -> Unit,
    viewModel: FinanceViewModel,
    onProfileClicked: () -> Unit
) {
    val userState by viewModel.user.collectAsStateWithLifecycle()
    val notificationState by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadCount = notificationState.filter { !it.isRead }.size

    var showNotifDialog by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Column {
                Text(
                    text = Localization.get("app_title", lang),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = Localization.get("developer_credit", lang),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu Icon",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        actions = {
            // Unread Notifications Badge
            IconButton(onClick = {
                showNotifDialog = true
                viewModel.clearAllNotifications() // clear read states
            }) {
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge {
                                Text("$unreadCount", color = Color.White)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Small Profile Avatar Button
            IconButton(onClick = onProfileClicked) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF6366F1), // Indigo-500
                                    Color(0xFFA855F7)  // Purple-500
                                )
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userState?.name?.take(2)?.uppercase() ?: "MS",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )

    if (showNotifDialog) {
        AlertDialog(
            onDismissRequest = { showNotifDialog = false },
            title = { Text(Localization.get("notifications", lang)) },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                    if (notificationState.isEmpty()) {
                        item {
                            Text(
                                text = if (lang == "ar") "لا توجد إشعارات حالية" else "No recent notifications",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        items(notificationState) { notif ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = if (lang == "ar") notif.titleAr else notif.titleEn,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (lang == "ar") notif.bodyAr else notif.bodyEn,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Divider(modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotifDialog = false }) {
                    Text(if (lang == "ar") "حسناً" else "Dismiss")
                }
            }
        )
    }
}

// ==================== DRAWER CONTENT ====================
@Composable
fun DrawerContent(
    viewModel: FinanceViewModel,
    activeTab: ActiveTab,
    onTabSelected: (ActiveTab) -> Unit,
    lang: String
) {
    val userState by viewModel.user.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.bindRtlPadding(lang))
    ) {
        // Upper Profile Status Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF6366F1), // Indigo-500
                                Color(0xFFA855F7)  // Purple-500
                            )
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userState?.name?.take(2)?.uppercase() ?: "MS",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = userState?.name ?: "م/ محمد صبري",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )

            Text(
                text = "${Localization.get("level", lang)} ${userState?.level ?: 1} • ${userState?.xp ?: 0} XP",
                fontSize = 12.sp,
                color = Color(0xFF818CF8)
            )

            // Mini Watermark Banner
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF6366F1).copy(alpha = 0.15f))
                    .padding(6.dp)
            ) {
                Text(
                    text = Localization.get("developer_credit_sub", lang),
                    fontSize = 10.sp,
                    color = Color(0xFF818CF8),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Divider(color = BorderNavy, modifier = Modifier.padding(bottom = 16.dp))

        // Navigation Items
        val items = listOf(
            DrawerItemData(ActiveTab.DASHBOARD, Localization.get("dashboard", lang), Icons.Default.Dashboard),
            DrawerItemData(ActiveTab.EXPENSES, Localization.get("expenses", lang), Icons.Default.TrendingDown),
            DrawerItemData(ActiveTab.INCOME, Localization.get("income", lang), Icons.Default.TrendingUp),
            DrawerItemData(ActiveTab.GOALS, Localization.get("goals", lang), Icons.Default.TrackChanges),
            DrawerItemData(ActiveTab.ANALYTICS, Localization.get("analytics", lang), Icons.Default.BarChart),
            DrawerItemData(ActiveTab.REPORTS, Localization.get("reports", lang), Icons.Default.Description),
            DrawerItemData(ActiveTab.ACHIEVEMENTS, Localization.get("achievements", lang), Icons.Default.EmojiEvents),
            DrawerItemData(ActiveTab.SETTINGS, Localization.get("settings", lang), Icons.Default.Settings),
            DrawerItemData(ActiveTab.ABOUT, Localization.get("about", lang), Icons.Default.Info)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(items) { item ->
                val selected = activeTab == item.tab
                NavigationDrawerItem(
                    icon = { Icon(item.icon, contentDescription = null, tint = if (selected) Color.White else Color.White.copy(alpha = 0.7f)) },
                    label = { Text(item.title, fontWeight = FontWeight.SemiBold) },
                    selected = selected,
                    onClick = { onTabSelected(item.tab) },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color(0xFF6366F1),
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.White.copy(alpha = 0.7f),
                        unselectedTextColor = Color.White.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            // Developer Telegram Action in Drawer list
            item {
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF818CF8)) },
                    label = { Text(Localization.get("telegram_label", lang), color = Color(0xFF818CF8), fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Localization.get("telegram_link", lang)))
                        context.startActivity(intent)
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = Color(0xFF818CF8),
                        unselectedTextColor = Color(0xFF818CF8)
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        // Drawer Footer
        Divider(color = BorderNavy, modifier = Modifier.padding(vertical = 12.dp))
        Text(
            text = Localization.get("copyright", lang),
            fontSize = 10.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

data class DrawerItemData(
    val tab: ActiveTab,
    val title: String,
    val icon: ImageVector
)

// ==================== PIN SECURITY OVERLAY ====================
@Composable
fun PinLockScreen(viewModel: FinanceViewModel, lang: String) {
    val enteredPin by viewModel.enteredPin.collectAsStateWithLifecycle()
    val pinError by viewModel.pinError.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicBlack)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Lock Icon",
                tint = Color(0xFF10B981),
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = Localization.get("app_title", lang),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = Localization.get("pin_prompt", lang),
                fontSize = 14.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Pin indicator dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    val filled = index < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (filled) Color(0xFF10B981) else Color(0xFF1E294B))
                            .border(1.dp, Color(0xFF10B981), CircleShape)
                    )
                }
            }

            if (pinError != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = pinError!!,
                    color = Color.Red,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // PIN keypad (Grid)
            val digits = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "⌫")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                digits.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { char ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.5f),
                                contentAlignment = Alignment.Center
                            ) {
                                if (char.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            if (char == "⌫") viewModel.deletePinDigit()
                                            else viewModel.enterPinDigit(char)
                                        },
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape)
                                            .background(SlateNavy)
                                    ) {
                                        Text(
                                            text = char,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
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

// ==================== DASHBOARD SCREEN ====================
@Composable
fun DashboardScreen(viewModel: FinanceViewModel, lang: String) {
    val metrics by viewModel.financialMetrics.collectAsStateWithLifecycle()
    val recentExpenses by viewModel.filteredExpenses.collectAsStateWithLifecycle()
    val recentIncome by viewModel.filteredIncome.collectAsStateWithLifecycle()
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()

    var showExpenseDialog by remember { mutableStateOf(false) }
    var showIncomeDialog by remember { mutableStateOf(false) }

    var showGoalDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Floating credit line
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateNavy),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "💎 ${Localization.get("developer_credit_sub", lang)}",
                        fontSize = 11.sp,
                        color = Color(0xFF818CF8),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "النسخة الذهبية",
                        fontSize = 11.sp,
                        color = AmberPremium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Glassmorphic Balance Dashboard Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                IndigoGradientStart,
                                BlueGradientEnd
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(32.dp)
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = Localization.get("current_balance", lang),
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${"%,.2f".format(metrics.balance)} ${metrics.currencyCode}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldBullet)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = Localization.get("total_income", lang),
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                            Text(
                                text = "${"%,.1f".format(metrics.totalIncome)} ${metrics.currencyCode}",
                                fontWeight = FontWeight.Bold,
                                color = EmeraldAccent
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(RoseBullet)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = Localization.get("total_expenses", lang),
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                            Text(
                                text = "${"%,.1f".format(metrics.totalExpenses)} ${metrics.currencyCode}",
                                fontWeight = FontWeight.Bold,
                                color = RoseAccent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Savings rate bar
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = Localization.get("savings_rate", lang),
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "${metrics.savingsRate.toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = (metrics.savingsRate / 100f).toFloat(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }

        // Daily / Monthly metrics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SlateNavy),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = Localization.get("daily_spending", lang),
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${"%,.1f".format(metrics.dailySpending)} ${metrics.currencyCode}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SlateNavy),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = Localization.get("monthly_spending", lang),
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${"%,.1f".format(metrics.monthlySpending)} ${metrics.currencyCode}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Quick Actions Row (HTML-identical 3-column layout)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Add Income Button (إضافة دخل)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(84.dp)
                        .clickable { showIncomeDialog = true },
                    colors = CardDefaults.cardColors(containerColor = DarkCardSecondary),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF6366F1).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", color = Color(0xFF818CF8), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (lang == "ar") "إضافة دخل" else "Add Income",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Add Expense Button (إضافة مصروف)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(84.dp)
                        .clickable { showExpenseDialog = true },
                    colors = CardDefaults.cardColors(containerColor = DarkCardSecondary),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF87171).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("−", color = Color(0xFFFCA5A5), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (lang == "ar") "إضافة مصروف" else "Add Expense",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Goals Button (الأهداف)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(84.dp)
                        .clickable { showGoalDialog = true },
                    colors = CardDefaults.cardColors(containerColor = DarkCardSecondary),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("★", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (lang == "ar") "الأهداف" else "Goals",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Footer Info Banner (Silver Savings Hero / XP Level)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF6366F1).copy(alpha = 0.1f))
                    .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF6366F1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏆", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (lang == "ar") "مستوى الادخار" else "Savings Level",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC7D2FE)
                            )
                            Text(
                                text = if (lang == "ar") "بطل التوفير الفضي" else "Silver Savings Hero",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF6366F1).copy(alpha = 0.3f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "+450 XP",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC7D2FE)
                        )
                    }
                }
            }
        }

        // Recent Operations Title
        item {
            Text(
                text = Localization.get("recent_transactions", lang),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Recent Transactions combined
        val totalTransactions = (recentExpenses.take(5) + recentIncome.take(5))
            .sortedByDescending { if (it is Expense) it.timestamp else (it as Income).timestamp }
            .take(5)

        if (totalTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = Localization.get("empty_transactions", lang),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(totalTransactions) { item ->
                val isExpense = item is Expense
                val name = if (isExpense) (item as Expense).note.ifEmpty { Localization.get("food", lang) } else (item as Income).note.ifEmpty { Localization.get("salary", lang) }
                val category = if (isExpense) (item as Expense).category else (item as Income).category
                val amount = if (isExpense) (item as Expense).amount else (item as Income).amount
                val curr = if (isExpense) (item as Expense).currencyCode else (item as Income).currencyCode
                val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(
                    Date(if (isExpense) (item as Expense).timestamp else (item as Income).timestamp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateNavy),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isExpense) Color(0xFFF87171).copy(alpha = 0.15f) else Color(0xFF34D399).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isExpense) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = if (isExpense) RoseAccent else EmeraldAccent
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = name,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "$category • $date",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Text(
                            text = "${if (isExpense) "-" else "+"}${"%,.1f".format(amount)} $curr",
                            fontWeight = FontWeight.Bold,
                            color = if (isExpense) RoseAccent else EmeraldAccent
                        )
                    }
                }
            }
        }
    }

    // Add Expense Dialog
    if (showExpenseDialog) {
        AddExpenseDialog(
            lang = lang,
            viewModel = viewModel,
            onDismiss = { showExpenseDialog = false }
        )
    }

    // Add Income Dialog
    if (showIncomeDialog) {
        AddIncomeDialog(
            lang = lang,
            viewModel = viewModel,
            onDismiss = { showIncomeDialog = false }
        )
    }

    // Add Goal Dialog
    if (showGoalDialog) {
        AddGoalDialog(
            lang = lang,
            viewModel = viewModel,
            onDismiss = { showGoalDialog = false }
        )
    }
}

// ==================== EXPENSES SCREEN ====================
@Composable
fun ExpensesScreen(viewModel: FinanceViewModel, lang: String) {
    val expensesList by viewModel.filteredExpenses.collectAsStateWithLifecycle()
    val searchQuery by viewModel.expenseSearchQuery.collectAsStateWithLifecycle()
    val activeFilter by viewModel.expenseCategoryFilter.collectAsStateWithLifecycle()
    val currencies by viewModel.currencies.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }

    val categories = listOf(
        "Food" to Localization.get("food", lang),
        "Transportation" to Localization.get("transport", lang),
        "Education" to Localization.get("education", lang),
        "Shopping" to Localization.get("shopping", lang),
        "Bills" to Localization.get("bills", lang),
        "Health" to Localization.get("health", lang),
        "Entertainment" to Localization.get("entertainment", lang),
        "Family" to Localization.get("family", lang),
        "Work" to Localization.get("work", lang),
        "Other" to Localization.get("other", lang)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setExpenseSearchQuery(it) },
            placeholder = { Text(Localization.get("search", lang)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = activeFilter == null,
                    onClick = { viewModel.setExpenseCategoryFilter(null) },
                    label = { Text(Localization.get("all", lang)) }
                )
            }
            items(categories) { cat ->
                FilterChip(
                    selected = activeFilter == cat.first,
                    onClick = { viewModel.setExpenseCategoryFilter(cat.first) },
                    label = { Text(cat.second) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Expenses List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (expensesList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (lang == "ar") "لا توجد مصروفات تطابق البحث" else "No matching expenses",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                items(expensesList) { expense ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.TrendingDown, contentDescription = null, tint = Color(0xFFEF4444))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = expense.note.ifEmpty { Localization.get("food", lang) },
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${categories.find { it.first == expense.category }?.second ?: expense.category} • ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(expense.timestamp))}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "-${"%,.1f".format(expense.amount)} ${expense.currencyCode}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF4444)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { viewModel.deleteExpense(expense) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = Color(0xFFEF4444),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.End)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Expense")
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            lang = lang,
            viewModel = viewModel,
            onDismiss = { showAddDialog = false }
        )
    }
}

// ==================== INCOME SCREEN ====================
@Composable
fun IncomeScreen(viewModel: FinanceViewModel, lang: String) {
    val incomeList by viewModel.filteredIncome.collectAsStateWithLifecycle()
    val searchQuery by viewModel.incomeSearchQuery.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }

    val categories = listOf(
        "Salary" to Localization.get("salary", lang),
        "Freelance" to Localization.get("freelance", lang),
        "Investment" to Localization.get("investment", lang),
        "Bonus" to Localization.get("bonus", lang),
        "Project" to Localization.get("project", lang),
        "Other" to Localization.get("other", lang)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setIncomeSearchQuery(it) },
            placeholder = { Text(Localization.get("search", lang)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (incomeList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (lang == "ar") "لا توجد مدخولات مطابقة للبحث" else "No matching income",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                items(incomeList) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF10B981))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = item.note.ifEmpty { Localization.get("salary", lang) },
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${categories.find { it.first == item.category }?.second ?: item.category} • ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(item.timestamp))}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "+${"%,.1f".format(item.amount)} ${item.currencyCode}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { viewModel.deleteIncome(item) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = Color(0xFF10B981),
            contentColor = CosmicBlack,
            modifier = Modifier
                .align(Alignment.End)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Income")
        }
    }

    if (showAddDialog) {
        AddIncomeDialog(
            lang = lang,
            viewModel = viewModel,
            onDismiss = { showAddDialog = false }
        )
    }
}

// ==================== GOALS SCREEN ====================
@Composable
fun GoalsScreen(viewModel: FinanceViewModel, lang: String) {
    val goalsList by viewModel.goals.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedGoalForProgress by remember { mutableStateOf<Goal?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = Localization.get("saving_goals", lang),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (goalsList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = Localization.get("empty_goals", lang),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(goalsList) { goal ->
                    val progress = if (goal.targetAmount > 0) goal.currentAmount / goal.targetAmount else 0.0
                    val isFinished = goal.isCompleted || progress >= 1.0

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, if (isFinished) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isFinished) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isFinished) Icons.Default.EmojiEvents else Icons.Default.TrackChanges,
                                            contentDescription = null,
                                            tint = if (isFinished) Color(0xFF10B981) else Color(0xFFF59E0B)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = goal.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }

                                IconButton(onClick = { viewModel.deleteGoal(goal) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${Localization.get("target_amount", lang)}: ${"%,.0f".format(goal.targetAmount)} EGP",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "${Localization.get("current_amount", lang)}: ${"%,.0f".format(goal.currentAmount)} EGP",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = progress.toFloat().coerceIn(0f, 1f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(CircleShape),
                                color = if (isFinished) Color(0xFF10B981) else Color(0xFFF59E0B),
                                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${Localization.get("target_date", lang)}: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(goal.targetDate))}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )

                                if (!isFinished) {
                                    Button(
                                        onClick = { selectedGoalForProgress = goal },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text(Localization.get("add_progress", lang), fontSize = 12.sp, color = CosmicBlack, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF10B981).copy(alpha = 0.1f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(Localization.get("unlocked", lang), color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = CosmicBlack,
            modifier = Modifier
                .align(Alignment.End)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Goal")
        }
    }

    if (showAddDialog) {
        AddGoalDialog(
            lang = lang,
            viewModel = viewModel,
            onDismiss = { showAddDialog = false }
        )
    }

    if (selectedGoalForProgress != null) {
        AddMoneyToGoalDialog(
            goal = selectedGoalForProgress!!,
            lang = lang,
            viewModel = viewModel,
            onDismiss = { selectedGoalForProgress = null }
        )
    }
}

// ==================== ANALYTICS SCREEN ====================
@Composable
fun AnalyticsScreen(viewModel: FinanceViewModel, lang: String) {
    val breakdown by viewModel.expenseCategoryBreakdown.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = Localization.get("spending_analytics", lang),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        // Custom Donut breakdown
        val translatedBreakdown = breakdown.mapKeys { entry ->
            when (entry.key) {
                "Food" -> Localization.get("food", lang)
                "Transportation" -> Localization.get("transport", lang)
                "Education" -> Localization.get("education", lang)
                "Shopping" -> Localization.get("shopping", lang)
                "Bills" -> Localization.get("bills", lang)
                "Health" -> Localization.get("health", lang)
                "Entertainment" -> Localization.get("entertainment", lang)
                "Family" -> Localization.get("family", lang)
                "Work" -> Localization.get("work", lang)
                else -> Localization.get("other", lang)
            }
        }

        CustomDonutChart(data = translatedBreakdown, lang = lang)

        Text(
            text = Localization.get("saving_trend", lang),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        // Generating values for Line Chart over time
        val sortedList = expenses.sortedBy { it.timestamp }
        var accum = 0.0
        val timelinePoints = sortedList.map {
            accum += it.amount
            accum
        }

        CustomLineChart(data = timelinePoints)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ==================== REPORTS SCREEN ====================
@Composable
fun ReportsScreen(viewModel: FinanceViewModel, lang: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }
    var selectedReportPeriod by remember { mutableStateOf("monthly") }

    if (isExporting) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(if (lang == "ar") "جاري إعداد التقرير..." else "Preparing Report...") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(if (lang == "ar") "يتم الآن تجميع العمليات المالية وتحليلها..." else "Compiling operations and analyzing...")
                }
            },
            confirmButton = {}
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = Localization.get("reports", lang),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = if (lang == "ar") "اختر نوع التقرير المطلوب" else "Select Report Period",
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val periods = listOf(
                        "daily" to Localization.get("daily_report", lang),
                        "weekly" to Localization.get("weekly_report", lang),
                        "monthly" to Localization.get("monthly_report", lang),
                        "yearly" to Localization.get("yearly_report", lang)
                    )

                    periods.forEach { period ->
                        val selected = selectedReportPeriod == period.first
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                .clickable { selectedReportPeriod = period.first }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = period.second,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) CosmicBlack else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Large illustration representation
        Icon(
            imageVector = Icons.Default.Description,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            modifier = Modifier.size(140.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Export Buttons
        Button(
            onClick = {
                isExporting = true
                coroutineScope.launch {
                    delay(2000)
                    isExporting = false
                    Toast.makeText(context, Localization.get("export_success", lang), Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = CosmicBlack)
            Spacer(modifier = Modifier.width(8.dp))
            Text(Localization.get("export_pdf", lang), fontWeight = FontWeight.Bold, color = CosmicBlack)
        }

        OutlinedButton(
            onClick = {
                isExporting = true
                coroutineScope.launch {
                    delay(2000)
                    isExporting = false
                    Toast.makeText(context, Localization.get("export_success", lang), Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.GridOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(Localization.get("export_excel", lang), fontWeight = FontWeight.Bold)
        }
    }
}

// ==================== ACHIEVEMENTS SCREEN ====================
@Composable
fun AchievementsScreen(viewModel: FinanceViewModel, lang: String) {
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = Localization.get("achievements", lang),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(achievements) { ach ->
                val badgeColor = when (ach.badgeType) {
                    "gold" -> Color(0xFFF59E0B)
                    "silver" -> Color(0xFF94A3B8)
                    else -> Color(0xFFCD7F32) // Bronze
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, if (ach.isUnlocked) badgeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (ach.isUnlocked) badgeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
                                .border(1.5.dp, if (ach.isUnlocked) badgeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = if (ach.isUnlocked) badgeColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (lang == "ar") ach.titleAr else ach.titleEn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (ach.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = if (lang == "ar") ach.descAr else ach.descEn,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            if (ach.isUnlocked && ach.unlockedAt != null) {
                                Text(
                                    text = "${Localization.get("unlocked", lang)} • ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(ach.unlockedAt))}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (!ach.isUnlocked) {
                            Icon(Icons.Default.Lock, contentDescription = "Locked", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }
    }
}

// ==================== PROFILE SCREEN ====================
@Composable
fun ProfileScreen(viewModel: FinanceViewModel, lang: String) {
    val userState by viewModel.user.collectAsStateWithLifecycle()
    val achievementsState by viewModel.achievements.collectAsStateWithLifecycle()
    val expensesState by viewModel.expenses.collectAsStateWithLifecycle()
    val incomeState by viewModel.income.collectAsStateWithLifecycle()

    val totalOps = expensesState.size + incomeState.size
    val finishedGoalsCount = achievementsState.find { it.code == "first_goal_completed" && it.isUnlocked } != null

    var isEditingName by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(userState?.name ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper Profile Card
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userState?.name?.take(2) ?: "محمد",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 28.sp
            )
        }

        if (isEditingName) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    viewModel.updateProfileName(editedName)
                    isEditingName = false
                }) {
                    Icon(Icons.Default.Check, contentDescription = "Save", tint = MaterialTheme.colorScheme.primary)
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = userState?.name ?: "م/ محمد صبري",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = {
                    editedName = userState?.name ?: ""
                    isEditingName = true
                }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Name", modifier = Modifier.size(18.dp))
                }
            }
        }

        Text(
            text = "${Localization.get("level", lang)} ${userState?.level ?: 1}",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        // XP Progress Indicator
        val currentLevelXp = (userState?.xp ?: 0) % 100
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "XP Progress", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text(text = "$currentLevelXp / 100 XP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = currentLevelXp / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        }

        Divider(modifier = Modifier.padding(vertical = 12.dp))

        // Quick Stats Columns
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "${Localization.get("total_transactions", lang)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text(text = "$totalOps", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "${Localization.get("completed_goals", lang)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text(text = if (finishedGoalsCount) "1" else "0", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

// ==================== SETTINGS SCREEN ====================
@Composable
fun SettingsScreen(viewModel: FinanceViewModel, lang: String) {
    val settingState by viewModel.setting.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showPinDialog by remember { mutableStateOf(false) }
    var inputPin by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = Localization.get("settings", lang),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Change Language
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(Localization.get("change_language", lang), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { viewModel.updateLanguage("ar") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (lang == "ar") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("العربية", color = if (lang == "ar") CosmicBlack else MaterialTheme.colorScheme.onSurface)
                    }

                    Button(
                        onClick = { viewModel.updateLanguage("en") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (lang == "en") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("English", color = if (lang == "en") CosmicBlack else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        // Change Theme
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(Localization.get("change_theme", lang), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val activeTheme = settingState?.themeMode ?: "dark"
                    Button(
                        onClick = { viewModel.updateTheme("dark") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeTheme == "dark") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (lang == "ar") "داكن" else "Dark", color = if (activeTheme == "dark") CosmicBlack else MaterialTheme.colorScheme.onSurface)
                    }

                    Button(
                        onClick = { viewModel.updateTheme("light") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeTheme == "light") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (lang == "ar") "فاتح" else "Light", color = if (activeTheme == "light") CosmicBlack else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        // Change Default Currency
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(Localization.get("currency", lang), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val activeCurr = settingState?.defaultCurrency ?: "EGP"
                    val currenciesList = listOf("EGP", "USD", "EUR", "SAR", "AED")

                    currenciesList.forEach { cur ->
                        val isSel = activeCurr == cur
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                .clickable { viewModel.updateDefaultCurrency(cur) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cur,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) CosmicBlack else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // PIN Security Lock
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(Localization.get("pin_security", lang), fontWeight = FontWeight.Bold)
                    Switch(
                        checked = settingState?.isSecurityEnabled ?: false,
                        onCheckedChange = { isEnabled ->
                            if (isEnabled) {
                                showPinDialog = true
                            } else {
                                viewModel.configureSecurity(false, null)
                            }
                        }
                    )
                }
                if (settingState?.isSecurityEnabled == true) {
                    Text(
                        text = "PIN lock active • قفل الحماية مفعّل",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Local backup simulation
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(Localization.get("backup", lang), fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { Toast.makeText(context, Localization.get("backup_success", lang), Toast.LENGTH_SHORT).show() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(Localization.get("backup", lang), color = CosmicBlack)
                    }

                    Button(
                        onClick = { Toast.makeText(context, Localization.get("restore_success", lang), Toast.LENGTH_SHORT).show() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(Localization.get("restore", lang), color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text(if (lang == "ar") "تعيين رمز PIN جديد" else "Set New PIN Code") },
            text = {
                OutlinedTextField(
                    value = inputPin,
                    onValueChange = { if (it.length <= 4) inputPin = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("e.g., 1234") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (inputPin.length == 4) {
                        viewModel.configureSecurity(true, inputPin)
                        showPinDialog = false
                        inputPin = ""
                    }
                }) {
                    Text(Localization.get("save", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text(Localization.get("cancel", lang))
                }
            }
        )
    }
}

// ==================== ABOUT SCREEN ====================
@Composable
fun AboutScreen(viewModel: FinanceViewModel, lang: String) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Wallet,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
        }

        Text(
            text = Localization.get("app_title", lang),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = Localization.get("app_version", lang),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )

        Text(
            text = Localization.get("about_content", lang),
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Developer Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = Localization.get("developer_credit", lang),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp
                )
                Text(
                    text = Localization.get("developer_credit_sub", lang),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        // Telegram Visit Button
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Localization.get("telegram_link", lang)))
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Send, contentDescription = null, tint = CosmicBlack)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = Localization.get("visit_telegram", lang),
                color = CosmicBlack,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ==================== ADD TRANSACTION DIALOGS ====================
@Composable
fun AddExpenseDialog(
    lang: String,
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Food") }
    var selectedCurrency by remember { mutableStateOf("EGP") }

    val categories = listOf(
        "Food" to Localization.get("food", lang),
        "Transportation" to Localization.get("transport", lang),
        "Education" to Localization.get("education", lang),
        "Shopping" to Localization.get("shopping", lang),
        "Bills" to Localization.get("bills", lang),
        "Health" to Localization.get("health", lang),
        "Entertainment" to Localization.get("entertainment", lang),
        "Family" to Localization.get("family", lang),
        "Work" to Localization.get("work", lang),
        "Other" to Localization.get("other", lang)
    )

    val currencies = listOf("EGP", "USD", "EUR", "SAR", "AED")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Localization.get("add_expense", lang)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(Localization.get("amount", lang)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Selection Spinner representation
                Column {
                    Text(Localization.get("category", lang), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { cat ->
                            val sel = selectedCategory == cat.first
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                    .clickable { selectedCategory = cat.first }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(cat.second, color = if (sel) CosmicBlack else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }

                // Currency Selection Rows
                Column {
                    Text(Localization.get("currency", lang), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        currencies.forEach { cur ->
                            val sel = selectedCurrency == cur
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                    .clickable { selectedCurrency = cur }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(cur, fontWeight = FontWeight.Bold, color = if (sel) CosmicBlack else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(Localization.get("note", lang)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amount.toDoubleOrNull()
                if (amt != null && amt > 0) {
                    viewModel.addExpense(amt, selectedCategory, note, selectedCurrency)
                    onDismiss()
                }
            }) {
                Text(Localization.get("save", lang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Localization.get("cancel", lang))
            }
        }
    )
}

@Composable
fun AddIncomeDialog(
    lang: String,
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Salary") }
    var selectedCurrency by remember { mutableStateOf("EGP") }

    val categories = listOf(
        "Salary" to Localization.get("salary", lang),
        "Freelance" to Localization.get("freelance", lang),
        "Investment" to Localization.get("investment", lang),
        "Bonus" to Localization.get("bonus", lang),
        "Project" to Localization.get("project", lang),
        "Other" to Localization.get("other", lang)
    )

    val currencies = listOf("EGP", "USD", "EUR", "SAR", "AED")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Localization.get("add_income", lang)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(Localization.get("amount", lang)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text(Localization.get("category", lang), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { cat ->
                            val sel = selectedCategory == cat.first
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                    .clickable { selectedCategory = cat.first }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(cat.second, color = if (sel) CosmicBlack else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }

                Column {
                    Text(Localization.get("currency", lang), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        currencies.forEach { cur ->
                            val sel = selectedCurrency == cur
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                    .clickable { selectedCurrency = cur }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(cur, fontWeight = FontWeight.Bold, color = if (sel) CosmicBlack else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(Localization.get("note", lang)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amount.toDoubleOrNull()
                if (amt != null && amt > 0) {
                    viewModel.addIncome(amt, selectedCategory, note, selectedCurrency)
                    onDismiss()
                }
            }) {
                Text(Localization.get("save", lang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Localization.get("cancel", lang))
            }
        }
    )
}

// ==================== SAVE GOAL DIALOGS ====================
@Composable
fun AddGoalDialog(
    lang: String,
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Localization.get("add_goal", lang)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(Localization.get("goal_name", lang)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = targetAmount,
                    onValueChange = { targetAmount = it },
                    label = { Text(Localization.get("target_amount", lang)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val target = targetAmount.toDoubleOrNull()
                if (name.isNotEmpty() && target != null && target > 0) {
                    val oneYearFromNow = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)
                    viewModel.addGoal(name, target, oneYearFromNow)
                    onDismiss()
                }
            }) {
                Text(Localization.get("save", lang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Localization.get("cancel", lang))
            }
        }
    )
}

@Composable
fun AddMoneyToGoalDialog(
    goal: Goal,
    lang: String,
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Localization.get("add_progress", lang)) },
        text = {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text(Localization.get("amount", lang)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = {
                val amt = amount.toDoubleOrNull()
                if (amt != null && amt > 0) {
                    viewModel.updateGoalProgress(goal, amt)
                    onDismiss()
                }
            }) {
                Text(Localization.get("save", lang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Localization.get("cancel", lang))
            }
        }
    )
}

// ==================== RTL PADDING EXTENSION ====================
fun Modifier.bindRtlPadding(lang: String): Modifier {
    return this.padding(horizontal = 16.dp)
}

// Padding RTL converter helper
fun Int.bindRtlPadding(lang: String): PaddingValues {
    return PaddingValues(all = this.dp)
}
