package com.example.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.Calendar

data class DraftMember(
    val firstName: String,
    val lastName: String,
    val role: String,
    val dateOfBirth: String,
    val phoneNumber: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFamilyScreen(
    viewModel: MainViewModel,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var headFirstName by remember { mutableStateOf("") }
    var headLastName by remember { mutableStateOf("") }
    var headPhone by remember { mutableStateOf("") }
    var headDoorNo by remember { mutableStateOf("") }
    var headStreet by remember { mutableStateOf("") }
    var headCity by remember { mutableStateOf("") }
    var headZip by remember { mutableStateOf("") }
    var headDob by remember { mutableStateOf("") }
    var relatedFamiliesText by remember { mutableStateOf("") }
    var familyAdditionalInfo by remember { mutableStateOf("") }
    var familyWeddingDate by remember { mutableStateOf("") }

    val additionalMembers = remember { mutableStateListOf<DraftMember>() }
    
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showLinkFamilyDialog by remember { mutableStateOf(false) }

    val familyUnits by viewModel.familyUnits.collectAsState()

    if (headDob.isBlank()) {
        headDob = String.format("%d-%02d-%02d", 1980, 1, 1) // logical starting DOB
    }

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text("Create Family") },
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
            // Head Details Section
            Text("HEAD OF FAMILY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)

            OutlinedTextField(
                value = headFirstName,
                onValueChange = { headFirstName = it },
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth().testTag("head_first_name_input")
            )

            OutlinedTextField(
                value = headLastName,
                onValueChange = { headLastName = it },
                label = { Text("Last Name") },
                modifier = Modifier.fillMaxWidth().testTag("head_last_name_input")
            )

            OutlinedTextField(
                value = headPhone,
                onValueChange = { headPhone = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = headDoorNo,
                onValueChange = { headDoorNo = it },
                label = { Text("Door No.") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = headStreet,
                onValueChange = { headStreet = it },
                label = { Text("Street Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = headCity,
                onValueChange = { headCity = it },
                label = { Text("City") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = headZip,
                onValueChange = { headZip = it },
                label = { Text("Zip Code") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = familyAdditionalInfo,
                onValueChange = { familyAdditionalInfo = it },
                label = { Text("Additional Information") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = familyWeddingDate,
                onValueChange = { familyWeddingDate = it },
                label = { Text("Family Wedding Date") },
                placeholder = { Text("YYYY-MM-DD") },
                trailingIcon = {
                    IconButton(onClick = {
                        val wedParts = familyWeddingDate.split("-")
                        val year = wedParts.getOrNull(0)?.toIntOrNull() ?: 2000
                        val month = (wedParts.getOrNull(1)?.toIntOrNull() ?: 1) - 1
                        val day = wedParts.getOrNull(2)?.toIntOrNull() ?: 1

                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                familyWeddingDate = String.format("%d-%02d-%02d", y, m + 1, d)
                            },
                            year,
                            month,
                            day
                        ).show()
                    }) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = "Choose Wedding Date")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(
                onClick = { showLinkFamilyDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(Icons.Filled.Link, contentDescription = "Link Family")
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (relatedFamiliesText.isBlank()) "Link Existing Family" else "Linked: $relatedFamiliesText")
            }

            // DOB with native calendar launcher
            OutlinedTextField(
                value = headDob,
                onValueChange = { headDob = it },
                label = { Text("Date of Birth") },
                trailingIcon = {
                    IconButton(onClick = {
                        val dobParts = headDob.split("-")
                        val year = dobParts.getOrNull(0)?.toIntOrNull() ?: 1980
                        val month = (dobParts.getOrNull(1)?.toIntOrNull() ?: 1) - 1
                        val day = dobParts.getOrNull(2)?.toIntOrNull() ?: 1

                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                headDob = String.format("%d-%02d-%02d", y, m + 1, d)
                            },
                            year,
                            month,
                            day
                        ).show()
                    }) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = "Choose DOB")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("FAMILY MEMBERS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                TextButton(onClick = { showAddMemberDialog = true }) {
                    Text("Add Member")
                }
            }

            if (additionalMembers.isEmpty()) {
                Text(
                    text = "No additional members added yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                additionalMembers.forEachIndexed { index, m ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${m.firstName} ${m.lastName}", fontWeight = FontWeight.Bold)
                            Text(m.role, style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { additionalMembers.removeAt(index) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Remove")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (headFirstName.isNotBlank() && headLastName.isNotBlank()) {
                        val combinedAddress = listOf(headDoorNo, headStreet, headCity, headZip)
                            .filter { it.isNotBlank() }
                            .joinToString(", ")
                        viewModel.createFamilyWithMembers(
                            headFirstName,
                            headLastName,
                            headPhone,
                            combinedAddress,
                            headDob,
                            familyWeddingDate.takeIf { it.isNotBlank() },
                            relatedFamiliesText.takeIf { it.isNotBlank() },
                            familyAdditionalInfo.takeIf { it.isNotBlank() },
                            additionalMembers,
                            onComplete
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp).testTag("save_family_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save Family", style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    if (showLinkFamilyDialog && familyUnits.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showLinkFamilyDialog = false },
            title = { Text("Link Existing Family") },
            text = {
                LazyColumn {
                    items(familyUnits) { unit ->
                        TextButton(
                            onClick = {
                                relatedFamiliesText = unit.family.familyName
                                showLinkFamilyDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(unit.family.familyName, textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLinkFamilyDialog = false }) {
                    Text("Close")
                }
            }
        )
    } else if (showLinkFamilyDialog) {
        AlertDialog(
            onDismissRequest = { showLinkFamilyDialog = false },
            title = { Text("Link Existing Family") },
            text = { Text("There are no other families to link. Create some families first.") },
            confirmButton = {
                TextButton(onClick = { showLinkFamilyDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showAddMemberDialog) {
        AddDialogMember(
            onDismiss = { showAddMemberDialog = false },
            onAdd = { draft ->
                additionalMembers.add(draft)
                showAddMemberDialog = false
            }
        )
    }
}

@Composable
fun AddDialogMember(
    onDismiss: () -> Unit,
    onAdd: (DraftMember) -> Unit
) {
    val context = LocalContext.current
    var fmFirstName by remember { mutableStateOf("") }
    var fmLastName by remember { mutableStateOf("") }
    var fmRole by remember { mutableStateOf("Spouse") }
    var fmPhone by remember { mutableStateOf("") }
    var fmDob by remember { mutableStateOf("2000-01-01") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Family Member") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = fmFirstName,
                    onValueChange = { fmFirstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = fmLastName,
                    onValueChange = { fmLastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = fmPhone,
                    onValueChange = { fmPhone = it },
                    label = { Text("Phone Number (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                val roles = listOf("Spouse", "Child", "Parent", "Sibling", "Grandparent", "Other")
                var expandedRoleMenu by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = fmRole,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
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
                                    fmRole = r
                                    expandedRoleMenu = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = fmDob,
                    onValueChange = { fmDob = it },
                    label = { Text("DOB") },
                    trailingIcon = {
                        IconButton(onClick = {
                            val dobParts = fmDob.split("-")
                            val year = dobParts.getOrNull(0)?.toIntOrNull() ?: 1980
                            val month = (dobParts.getOrNull(1)?.toIntOrNull() ?: 1) - 1
                            val day = dobParts.getOrNull(2)?.toIntOrNull() ?: 1
                            DatePickerDialog(
                                context,
                                { _, y, m, d -> fmDob = String.format("%d-%02d-%02d", y, m + 1, d) },
                                year, month, day
                            ).show()
                        }) {
                            Icon(Icons.Filled.CalendarMonth, contentDescription = "Choose DOB")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (fmFirstName.isNotBlank()) {
                    onAdd(DraftMember(fmFirstName, fmLastName, fmRole, fmDob, fmPhone.takeIf { it.isNotBlank() }))
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
