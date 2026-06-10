package com.example

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.ChurchDatabase
import com.example.data.ChurchRepository
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import com.example.worker.BirthdayReminderWorker
import java.util.concurrent.TimeUnit

// Simple type-safe routing destinations
sealed class Screen {
    object Dashboard : Screen()
    object Directory : Screen()
    object CreateFamily : Screen()
    data class AddEditMember(val memberId: Long?, val familyIdToJoin: Long? = null) : Screen()
    data class MemberProfile(val memberId: Long) : Screen()
    data class FamilyProfile(val familyId: Long) : Screen()
    object Backup : Screen()
}

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            scheduleBirthdayWorker()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request POST_NOTIFICATIONS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    scheduleBirthdayWorker()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            scheduleBirthdayWorker()
        }

        // 1. Core database and repository initialization
        val database = ChurchDatabase.getDatabase(applicationContext)
        val repository = ChurchRepository(database.churchDao())

        // 2. ViewModel setup
        val factory = MainViewModelFactory(repository)
        val viewModel = androidx.lifecycle.ViewModelProvider(this, factory)[MainViewModel::class.java]

        setContent {
            MyApplicationTheme {
                // In-memory UI backstack router
                val screenStack = remember { mutableStateListOf<Screen>(Screen.Dashboard) }
                val currentScreen = screenStack.lastOrNull() ?: Screen.Dashboard

                fun navigateTo(screen: Screen) {
                    // Reset to root targets to avoid accumulating excessive stacks
                    if (screen is Screen.Dashboard || screen is Screen.Directory || screen is Screen.Backup) {
                        screenStack.clear()
                    }
                    screenStack.add(screen)
                }

                fun goBack() {
                    if (screenStack.size > 1) {
                        screenStack.removeLast()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // Display bottom bar only on root tabs for clean focused editing on other screens
                        if (currentScreen is Screen.Dashboard || currentScreen is Screen.Directory || currentScreen is Screen.Backup) {
                            NavigationBar(
                                windowInsets = WindowInsets.navigationBars
                            ) {
                                NavigationBarItem(
                                    selected = currentScreen is Screen.Dashboard,
                                    onClick = { navigateTo(Screen.Dashboard) },
                                    icon = { Icon(Icons.Filled.Cake, contentDescription = "Birthdays") },
                                    label = { Text("Birthdays") },
                                    modifier = Modifier.testTag("nav_tab_dashboard")
                                )
                                NavigationBarItem(
                                    selected = currentScreen is Screen.Directory,
                                    onClick = { navigateTo(Screen.Directory) },
                                    icon = { Icon(Icons.Filled.PeopleAlt, contentDescription = "Directory") },
                                    label = { Text("Directory") },
                                    modifier = Modifier.testTag("nav_tab_directory")
                                )
                                NavigationBarItem(
                                    selected = currentScreen is Screen.Backup,
                                    onClick = { navigateTo(Screen.Backup) },
                                    icon = { Icon(Icons.Filled.Backup, contentDescription = "Backup") },
                                    label = { Text("Backup") },
                                    modifier = Modifier.testTag("nav_tab_backup")
                                )
                            }
                        }
                    },
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    // Navigation Router Controller
                    BoxWithNavigation(
                        currentScreen = currentScreen,
                        viewModel = viewModel,
                        modifier = Modifier
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding),
                        onNavigate = { navigateTo(it) },
                        onBack = { goBack() }
                    )
                }
            }
        }
    }
    private fun scheduleBirthdayWorker() {
        val workRequest = PeriodicWorkRequestBuilder<BirthdayReminderWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "birthday_reminder_work",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}

@Composable
fun BoxWithNavigation(
    currentScreen: Screen,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    onNavigate: (Screen) -> Unit,
    onBack: () -> Unit
) {
    androidx.compose.foundation.layout.Box(modifier = modifier) {
        when (currentScreen) {
            is Screen.Dashboard -> {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToFamilyProfile = { onNavigate(Screen.FamilyProfile(it)) },
                    onNavigateToCreateFamily = { onNavigate(Screen.CreateFamily) }
                )
            }
            is Screen.Directory -> {
                DirectoryScreen(
                    viewModel = viewModel,
                    onNavigateToFamilyProfile = { onNavigate(Screen.FamilyProfile(it)) },
                    onNavigateToCreateFamily = { onNavigate(Screen.CreateFamily) }
                )
            }
            is Screen.CreateFamily -> {
                CreateFamilyScreen(
                    viewModel = viewModel,
                    onComplete = { onBack() },
                    onBack = { onBack() }
                )
            }
            is Screen.AddEditMember -> {
                AddEditMemberScreen(
                    memberId = currentScreen.memberId,
                    familyIdToJoin = currentScreen.familyIdToJoin,
                    viewModel = viewModel,
                    onComplete = { onBack() },
                    onBack = { onBack() }
                )
            }
            is Screen.MemberProfile -> {
                MemberProfileScreen(
                    memberId = currentScreen.memberId,
                    viewModel = viewModel,
                    onNavigateToEdit = { onNavigate(Screen.AddEditMember(it)) },
                    onBack = { onBack() }
                )
            }
            is Screen.FamilyProfile -> {
                FamilyProfileScreen(
                    familyId = currentScreen.familyId,
                    viewModel = viewModel,
                    onNavigateToMember = { onNavigate(Screen.MemberProfile(it)) },
                    onBack = { onBack() }
                )
            }
            is Screen.Backup -> {
                BackupScreen(
                    viewModel = viewModel,
                    onBack = { onNavigate(Screen.Dashboard) }
                )
            }
        }
    }
}
