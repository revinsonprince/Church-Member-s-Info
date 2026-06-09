package com.example.ui

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Family
import com.example.data.Member
import com.example.data.VisitLog
import kotlinx.coroutines.launch
import java.util.Calendar

// ==========================================
// 1. DASHBOARD SCREEN (Upcoming Birthdays)
// ==========================================
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToProfile: (Long) -> Unit,
    onNavigateToCreateFamily: () -> Unit
) {
    val eventReminders by viewModel.upcomingEvents.collectAsState()
    val familyCount by viewModel.families.collectAsState()
    val memberCount by viewModel.members.collectAsState()
    val visitLogs by viewModel.allVisitLogs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcoming Pastoral Header
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                lineHeight = 40.sp,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Statistics Cards Row (Satisfies visual guidelines)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatsCard(
                title = "Families",
                count = familyCount.size.toString(),
                icon = Icons.Outlined.People,
                modifier = Modifier.weight(1f)
            )
            StatsCard(
                title = "Members",
                count = memberCount.size.toString(),
                icon = Icons.Outlined.AccountCircle,
                modifier = Modifier.weight(1f)
            )
            StatsCard(
                title = "Total Visits",
                count = visitLogs.size.toString(),
                icon = Icons.Outlined.HomeWork,
                modifier = Modifier.weight(1f)
            )
        }

        // Quick Action Divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Upcoming Events (30 Days)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            FilledTonalButton(
                onClick = onNavigateToCreateFamily,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("onboard_family_button")
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Create Family", style = MaterialTheme.typography.labelSmall)
            }
        }

        // Event Timeline / Reminders
        if (eventReminders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        Icons.Outlined.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "No birthdays or anniversaries in the next 30 days.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                eventReminders.forEach { reminder ->
                    EventItem(reminder = reminder, onClick = { onNavigateToProfile(reminder.member.id) })
                }
            }
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    count: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = count,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun EventItem(reminder: EventReminder, onClick: () -> Unit) {
    val isBirthday = reminder.eventType == "Birthday"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("event_item_${reminder.member.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(16.dp)
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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isBirthday) Icons.Filled.Cake else Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Column {
                    Text(
                        text = if (isBirthday || reminder.pairedNames == null) reminder.member.fullName else reminder.pairedNames,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    val actionText = if (isBirthday) "Turning ${reminder.years}" else "${reminder.years}th Anniversary"
                    Text(
                        text = "$actionText on ${reminder.dateStr.substring(5)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // Days remaining chip
            val daysText = if (reminder.daysRemaining == 0) "Today!" else "In ${reminder.daysRemaining}d"
            val badgeColor = if (reminder.daysRemaining == 0) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
            val textColor = if (reminder.daysRemaining == 0) {
                MaterialTheme.colorScheme.onTertiaryContainer
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(badgeColor)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = daysText,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = textColor
                )
            }
        }
    }
}

// ==========================================
// 2. DIRECTORY SCREEN (Search & Grouped Family Blocks)
// ==========================================
@Composable
fun DirectoryScreen(
    viewModel: MainViewModel,
    onNavigateToFamilyProfile: (Long) -> Unit,
    onNavigateToCreateFamily: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val familyUnits by viewModel.filteredFamilyUnits.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Modern Outlined Search Box (Satisfies search capability)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Search by name, phone, or address...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear search")
                    }
                }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("directory_search_bar"),
            shape = CircleShape,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        // Directory Content Lists
        if (familyUnits.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.ContactMail,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "No family units found.",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Try searching for another keyword or register a new family.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onNavigateToCreateFamily) {
                        Text("Create Family Instead")
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(familyUnits) { unit ->
                    FamilyGroupCard(
                        unit = unit,
                        searchQuery = searchQuery,
                        onFamilyClick = { onNavigateToFamilyProfile(unit.family.id) },
                        onDeleteFamilyClick = { viewModel.deleteFamily(unit) }
                    )
                }
            }
        }
    }
}

@Composable
fun FamilyGroupCard(
    unit: FamilyUnit,
    searchQuery: String,
    onFamilyClick: () -> Unit,
    onDeleteFamilyClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("family_card_${unit.family.id}")
            .clickable { onFamilyClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Family Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = unit.family.familyName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.primary
                    )
                    unit.head?.let {
                        Text(
                            text = "Family Head: ${it.fullName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Unit Count: ${unit.members.size}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    IconButton(onClick = onDeleteFamilyClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete Family", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            if (searchQuery.isNotBlank()) {
                val matchingMembers = unit.members.filter { m ->
                    m.fullName.contains(searchQuery, ignoreCase = true) ||
                    m.address.contains(searchQuery, ignoreCase = true) ||
                    m.phoneNumber.contains(searchQuery, ignoreCase = true)
                }

                if (matchingMembers.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Matching Members:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        matchingMembers.forEach { m ->
                            Text("- ${m.fullName}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

// Temporary layout support icons for the Crown marker helper
private val Icons.Filled.Crown get() = Icons.Filled.Star

// ==========================================
// 2b. FAMILY PROFILE SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyProfileScreen(
    familyId: Long,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val familyUnits by viewModel.familyUnits.collectAsState()
    val familyUnit = familyUnits.find { it.family.id == familyId }

    var selectedMemberForPopup by remember { mutableStateOf<Member?>(null) }
    var showAddLogDialog by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }

    if (familyUnit == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Family not found.")
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onBack) { Text("Go Back") }
            }
        }
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            MediumTopAppBar(
                title = { Text(familyUnit.family.familyName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddLogDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.AddComment, contentDescription = "Add Visit")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "FAMILY DETAILS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        ContactFieldRow(
                            icon = Icons.Outlined.PinDrop,
                            label = "Address",
                            value = familyUnit.head?.address ?: "No address available"
                        )
                        ContactFieldRow(
                            icon = Icons.Outlined.Phone,
                            label = "Head Contact",
                            value = familyUnit.head?.phoneNumber ?: "No phone available"
                        )
                        if (!familyUnit.family.additionalInfo.isNullOrBlank()) {
                            ContactFieldRow(
                                icon = Icons.Outlined.Info,
                                label = "Additional Info",
                                value = familyUnit.family.additionalInfo
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MEMBERS",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(
                        onClick = { showAddMemberDialog = true },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Add Member")
                    }
                }
            }

            items(familyUnit.members) { member ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedMemberForPopup = member }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (member.role.lowercase() == "head") {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (member.role.lowercase() == "head") Icons.Filled.Crown else Icons.Filled.Person,
                                contentDescription = null,
                                tint = if (member.role.lowercase() == "head") Color.White else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = member.fullName,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = member.role,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = "View Profile")
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        if (showAddLogDialog && familyUnit.head != null) {
            AddVisitLogDialog(
                onDismiss = { showAddLogDialog = false },
                onConfirm = { date, reason, prayer, notes ->
                    viewModel.addVisitLog(familyUnit.head.id, date, reason, prayer, notes)
                    showAddLogDialog = false
                }
            )
        } else if (showAddLogDialog && familyUnit.head == null) {
            Toast.makeText(context, "No head found to associate visit.", Toast.LENGTH_SHORT).show()
            showAddLogDialog = false
        }

        selectedMemberForPopup?.let { mem ->
            AlertDialog(
                onDismissRequest = { selectedMemberForPopup = null },
                title = { Text(mem.fullName) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Role: ${mem.role}")
                        Text("Birth Date: ${mem.dateOfBirth}")
                        mem.weddingDate?.let { wed -> Text("Wedding Date: $wed") }
                        if (mem.phoneNumber.isNotBlank()) Text("Phone: ${mem.phoneNumber}")
                        if (mem.address.isNotBlank()) Text("Address: ${mem.address}")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedMemberForPopup = null }) {
                        Text("Close")
                    }
                }
            )
        }

        if (showAddMemberDialog) {
            AddDialogMember(
                onDismiss = { showAddMemberDialog = false },
                onAdd = { draft ->
                    viewModel.addDraftMemberToFamily(familyId, familyUnit.head, draft)
                    showAddMemberDialog = false
                }
            )
        }
    }
}

// ==========================================
// 3. MEMBER PROFILE & VISIT LOGS SCREEN
// ==========================================
@Composable
fun MemberProfileScreen(
    memberId: Long,
    viewModel: MainViewModel,
    onNavigateToEdit: (Long) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val members by viewModel.members.collectAsState()
    val families by viewModel.families.collectAsState()
    val allLogs by viewModel.allVisitLogs.collectAsState()

    val member = members.find { it.id == memberId }
    val family = families.find { it.id == member?.familyId }
    val logs = allLogs.filter { it.memberId == memberId }

    var showAddLogDialog by remember { mutableStateOf(false) }

    if (member == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Member not found.")
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onBack) { Text("Go Back") }
            }
        }
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddLogDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_visit_log_fab")
            ) {
                Icon(Icons.Filled.AddComment, contentDescription = "Add Visit Log")
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Profile Top bar header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Go back")
                }
                Text(
                    text = "Member Profile",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = { onNavigateToEdit(member.id) }, modifier = Modifier.testTag("edit_profile_button")) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit Member")
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Info Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Avatar Badge
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = member.firstName.take(1) + member.lastName.take(1),
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            Text(
                                text = member.fullName,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center
                            )

                            // Role and Family metadata tags
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(bottom = 4.dp)
                            ) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text(member.role) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Filled.CardMembership,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                                family?.let {
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(it.familyName) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Filled.PeopleAlt,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                            // Relational stats buttons
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                QuickContactButton(
                                    icon = Icons.Filled.Phone,
                                    label = "Call",
                                    onClick = {
                                        try {
                                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${member.phoneNumber}"))
                                            context.startActivity(dialIntent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Could not open dialer", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                                QuickContactButton(
                                    icon = Icons.Filled.Map,
                                    label = "Map",
                                    onClick = {
                                        try {
                                            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(member.address)}"))
                                            context.startActivity(mapIntent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Map unavailable", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Profile secondary fields (DOB, Phone, Address)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "CONTACT & PERSONAL DETAILS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary
                            )

                            ContactFieldRow(
                                icon = Icons.Outlined.Cake,
                                label = "Date of Birth",
                                value = member.dateOfBirth
                            )
                            ContactFieldRow(
                                icon = Icons.Outlined.Phone,
                                label = "Phone Number",
                                value = member.phoneNumber
                            )
                            ContactFieldRow(
                                icon = Icons.Outlined.PinDrop,
                                label = "Address",
                                value = member.address
                            )
                        }
                    }
                }

                // Visitation Logs List Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Visitation Log History",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${logs.size} recorded",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Visit logs list
                if (logs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Comment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    "No visits logged yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                } else {
                    items(logs) { log ->
                        VisitLogItem(
                            log = log,
                            onDelete = { viewModel.deleteVisitLog(log) }
                        )
                    }
                }
            }
        }
    }

    if (showAddLogDialog) {
        AddVisitLogDialog(
            onDismiss = { showAddLogDialog = false },
            onConfirm = { date, reason, prayers, notes ->
                viewModel.addVisitLog(member.id, date, reason, prayers, notes)
                showAddLogDialog = false
            }
        )
    }
}

@Composable
fun QuickContactButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun ContactFieldRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            Text(value, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
        }
    }
}

@Composable
fun VisitLogItem(log: VisitLog, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("visit_log_item_${log.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Log Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = log.visitDate,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete Log",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Reason
            Text(
                text = "Reason for Visit",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = log.reason,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Prayer requests
            if (log.prayerRequests.isNotBlank()) {
                Text(
                    text = "Prayer Requests",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = log.prayerRequests,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Notes
            if (log.notes.isNotBlank()) {
                Text(
                    text = "Pastoral Notes",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = log.notes,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// Dialog to add visit logs
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVisitLogDialog(
    onDismiss: () -> Unit,
    onConfirm: (date: String, reason: String, prayer: String, notes: String) -> Unit
) {
    val context = LocalContext.current
    var date by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var prayer by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()
    if (date.isBlank()) {
        date = String.format(
            "%d-%02d-%02d",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Household Visit", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Visit date text field with dialog calendar launcher
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Visit Date") },
                    trailingIcon = {
                        IconButton(onClick = {
                            DatePickerDialog(
                                context,
                                { _, year, monthOfYear, dayOfMonth ->
                                    date = String.format("%d-%02d-%02d", year, monthOfYear + 1, dayOfMonth)
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }) {
                            Icon(Icons.Filled.CalendarMonth, contentDescription = "Choose Date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason for Visit") },
                    placeholder = { Text("e.g. Birthday blessing, bereavement support...") },
                    modifier = Modifier.fillMaxWidth().testTag("visit_reason_input")
                )

                OutlinedTextField(
                    value = prayer,
                    onValueChange = { prayer = it },
                    label = { Text("Prayer Requests") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("visit_prayer_input")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes & Guidance") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("visit_notes_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.isBlank()) {
                        Toast.makeText(context, "Please provide a reason", Toast.LENGTH_SHORT).show()
                    } else {
                        onConfirm(date, reason, prayer, notes)
                    }
                },
                modifier = Modifier.testTag("submit_visit_log_button")
            ) {
                Text("Save Log")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ==========================================
// 4. ADD / EDIT MEMBER FORM SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMemberScreen(
    memberId: Long?,
    familyIdToJoin: Long? = null,
    viewModel: MainViewModel,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val members by viewModel.members.collectAsState()
    val families by viewModel.families.collectAsState()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(if (familyIdToJoin != null) "Other" else "Head") }
    var phone by remember { mutableStateOf("") }
    var doorNo by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var zip by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var weddingDate by remember { mutableStateOf("") }

    // Relationship matching options
    var familySelectionMode by remember { mutableStateOf(if (familyIdToJoin != null) 1 else 0) } // 0 = Create New, 1 = Join Existing
    var newFamilyName by remember { mutableStateOf("") }
    var selectedExistingFamilyId by remember { mutableStateOf(familyIdToJoin ?: -1L) }

    // On Edit initialization loader
    LaunchedEffect(memberId) {
        if (memberId != null && memberId != 0L) {
            val m = members.find { it.id == memberId }
            if (m != null) {
                firstName = m.firstName
                lastName = m.lastName
                role = m.role
                phone = m.phoneNumber
                
                val addressParts = m.address.split(", ")
                doorNo = addressParts.getOrNull(0) ?: ""
                street = addressParts.getOrNull(1) ?: ""
                city = addressParts.getOrNull(2) ?: ""
                zip = addressParts.getOrNull(3) ?: ""

                dob = m.dateOfBirth
                weddingDate = m.weddingDate ?: ""

                val fam = families.find { it.id == m.familyId }
                if (fam != null) {
                    familySelectionMode = 1
                    selectedExistingFamilyId = fam.id
                }
            }
        }
    }

    val calendar = Calendar.getInstance()
    if (dob.isBlank()) {
        dob = String.format("%d-%02d-%02d", 1980, 1, 1) // logical starting DOB
    }

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text(if (memberId == null || memberId == 0L) "Register Member" else "Edit Contact Info") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Details Section
            Text("MEMBER PARTICULARS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)

            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth().testTag("first_name_input")
            )

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name") },
                modifier = Modifier.fillMaxWidth().testTag("last_name_input")
            )

            // DOB with native calendar launcher
            OutlinedTextField(
                value = dob,
                onValueChange = { dob = it },
                label = { Text("Date of Birth") },
                trailingIcon = {
                    IconButton(onClick = {
                        val dobParts = dob.split("-")
                        val year = dobParts.getOrNull(0)?.toIntOrNull() ?: 1980
                        val month = (dobParts.getOrNull(1)?.toIntOrNull() ?: 1) - 1
                        val day = dobParts.getOrNull(2)?.toIntOrNull() ?: 1

                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                dob = String.format("%d-%02d-%02d", y, m + 1, d)
                            },
                            year,
                            month,
                            day
                        ).show()
                    }) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = "Choose DOB")
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("dob_input")
            )

            if (role == "Spouse") {
                // Wedding Date
                OutlinedTextField(
                    value = weddingDate,
                    onValueChange = { weddingDate = it },
                    label = { Text("Wedding Date (Optional)") },
                    placeholder = { Text("YYYY-MM-DD") },
                    trailingIcon = {
                        IconButton(onClick = {
                            val wedParts = weddingDate.takeIf { it.isNotBlank() }?.split("-") ?: emptyList()
                            val year = wedParts.getOrNull(0)?.toIntOrNull() ?: 2000
                            val month = (wedParts.getOrNull(1)?.toIntOrNull() ?: 1) - 1
                            val day = wedParts.getOrNull(2)?.toIntOrNull() ?: 1

                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    weddingDate = String.format("%d-%02d-%02d", y, m + 1, d)
                                },
                                year,
                                month,
                                day
                            ).show()
                        }) {
                            Icon(Icons.Filled.CalendarMonth, contentDescription = "Choose Wedding Date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("wedding_input")
                )
            }

            // Family Role Selector (Head, Spouse, Child, Parent, Sibling, Grandparent, Other)
            Text("FAMILY ROLE ASSIGNMENT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)

            val roles = listOf("Head", "Spouse", "Child", "Parent", "Sibling", "Grandparent", "Other")
            Box(modifier = Modifier.fillMaxWidth()) {
                var expandedRoleMenu by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = role,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Role in Family") },
                    trailingIcon = {
                        IconButton(onClick = { expandedRoleMenu = true }) {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Show roles")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(
                    expanded = expandedRoleMenu,
                    onDismissRequest = { expandedRoleMenu = false }
                ) {
                    roles.forEach { r ->
                        DropdownMenuItem(
                            text = { Text(r) },
                            onClick = {
                                role = r
                                expandedRoleMenu = false
                            }
                        )
                    }
                }
            }

            // Household grouping configurations (Dropdown check logic)
            Text("RELATIONAL HOUSEHOLD GROUPING", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)

            if (memberId == null || memberId == 0L) {
                // If we are strictly joining a specific family
                val selectedFamily = families.find { it.id == familyIdToJoin }
                OutlinedTextField(
                    value = selectedFamily?.familyName ?: "Unknown Family",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Joining Family") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    "Note: Associated family matching is locked during edits. Re-onboard the contact to link to another house unit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Contact / Location
            Text("COMMUNICATION DETAILS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth().testTag("phone_input")
            )

            OutlinedTextField(
                value = doorNo,
                onValueChange = { doorNo = it },
                label = { Text("Door No.") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = street,
                onValueChange = { street = it },
                label = { Text("Street Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = zip,
                onValueChange = { zip = it },
                label = { Text("Zip Code") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action triggers
            Button(
                onClick = {
                    if (firstName.isBlank() || lastName.isBlank()) {
                        Toast.makeText(context, "Please enter first and last name", Toast.LENGTH_SHORT).show()
                    } else if (memberId == null && selectedExistingFamilyId == -1L) {
                        Toast.makeText(context, "Please ensure a family unit is selected", Toast.LENGTH_SHORT).show()
                    } else {
                        val finalOption = if (memberId != null && memberId != 0L) {
                            val activeMember = members.find { it.id == memberId }
                            FamilyOption.JoinExisting(activeMember?.familyId ?: 0L)
                        } else {
                            FamilyOption.JoinExisting(selectedExistingFamilyId)
                        }

                        val combinedAddress = listOf(doorNo, street, city, zip).filter { it.isNotBlank() }.joinToString(", ")
                        viewModel.saveMember(
                            memberId = memberId ?: 0L,
                            firstName = firstName,
                            lastName = lastName,
                            role = role,
                            familyOption = finalOption,
                            phoneNumber = phone,
                            address = combinedAddress,
                            dateOfBirth = dob,
                            weddingDate = if (role == "Spouse") weddingDate else null,
                            onComplete = onComplete
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_member_button")
            ) {
                Text(if (memberId == null || memberId == 0L) "Register Member" else "Save Changes")
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ==========================================
// 5. PORTABILITY / SETTINGS SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isLoaderVisible by remember { mutableStateOf(false) }

    val families by viewModel.families.collectAsState()
    val members by viewModel.members.collectAsState()
    val logs by viewModel.allVisitLogs.collectAsState()

    // Import file picker activity contract details
    val selectJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            isLoaderVisible = true
            viewModel.performImport(context, uri) { message ->
                isLoaderVisible = false
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text("Backup & Core Portability") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Illustration
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.CloudUpload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(52.dp)
                )
            }

            Text(
                text = "Secure Your Ministry Logs",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Back up and restore your visitation histories, family relationships, head of household links, and birthdays to a single cloud-portable JSON file.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Stats summary card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "DATABASE CAPACITY STATE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Family records:")
                        Text("${families.size} units", fontWeight = FontWeight.Bold)
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Church members:")
                        Text("${members.size} records", fontWeight = FontWeight.Bold)
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Total house visit logs:")
                        Text("${logs.size} recorded", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (isLoaderVisible) {
                CircularProgressIndicator()
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Export trigger (Satisfies file export file sharing requirement)
                    Button(
                        onClick = {
                            viewModel.performExport(context) { message ->
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("export_backup_button")
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export JSON")
                    }

                    // Import trigger (provides complementary restore function)
                    OutlinedButton(
                        onClick = { selectJsonLauncher.launch("application/json") },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("import_backup_button")
                    ) {
                        Icon(Icons.Filled.CloudDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Import Backup")
                    }
                }
            }
        }
    }
}
