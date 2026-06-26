package com.example.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.Family

// Robust address parser to split existing concatenated address back to separate fields
fun parseAddress(address: String): Map<String, String> {
    val parts = address.split(",").map { it.trim() }
    val result = mutableMapOf(
        "doorNo" to "",
        "street" to "",
        "locality" to "",
        "city" to "",
        "state" to "",
        "zip" to ""
    )
    if (parts.isEmpty() || address.isBlank()) return result

    if (parts.size == 1) {
        result["street"] = parts[0]
        return result
    }

    val remainingParts = parts.toMutableList()
    val lastPart = remainingParts.lastOrNull() ?: ""
    if (lastPart.any { it.isDigit() } && lastPart.length >= 3) {
        result["zip"] = lastPart
        remainingParts.removeAt(remainingParts.lastIndex)
    }

    if (remainingParts.isNotEmpty()) {
        result["state"] = remainingParts.last()
        remainingParts.removeAt(remainingParts.lastIndex)
    }

    if (remainingParts.isNotEmpty()) {
        result["city"] = remainingParts.last()
        remainingParts.removeAt(remainingParts.lastIndex)
    }

    if (remainingParts.isNotEmpty()) {
        result["locality"] = remainingParts.last()
        remainingParts.removeAt(remainingParts.lastIndex)
    }

    if (remainingParts.isNotEmpty()) {
        val first = remainingParts.first()
        if (first.length < 10 && (first.any { it.isDigit() } || first.contains("/") || first.contains("-"))) {
            result["doorNo"] = first
            remainingParts.removeAt(0)
        }
    }

    if (remainingParts.isNotEmpty()) {
        result["street"] = remainingParts.joinToString(", ")
    }

    return result
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFamilyScreen(
    familyId: Long,
    viewModel: MainViewModel,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val familyUnits by viewModel.familyUnits.collectAsState()
    val familyUnit = familyUnits.find { it.family.id == familyId }

    var familyName by remember { mutableStateOf("") }
    var doorNo by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var locality by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var zip by remember { mutableStateOf("") }
    var weddingDate by remember { mutableStateOf("") }
    var additionalInfo by remember { mutableStateOf("") }
    var relatedFamiliesText by remember { mutableStateOf("") }

    var showLinkFamilyDialog by remember { mutableStateOf(false) }

    // Initialize states when the family data loads
    LaunchedEffect(familyUnit) {
        familyUnit?.let { unit ->
            val fam = unit.family
            familyName = fam.familyName
            additionalInfo = fam.additionalInfo ?: ""
            weddingDate = fam.weddingDate ?: ""
            relatedFamiliesText = fam.relatedFamilies ?: ""

            val addrParts = parseAddress(fam.address)
            doorNo = addrParts["doorNo"] ?: ""
            street = addrParts["street"] ?: ""
            locality = addrParts["locality"] ?: ""
            city = addrParts["city"] ?: ""
            state = addrParts["state"] ?: ""
            zip = addrParts["zip"] ?: ""
        }
    }

    if (familyUnit == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text("Edit Family Info") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
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
            Text("FAMILY GENERAL INFORMATION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)

            OutlinedTextField(
                value = familyName,
                onValueChange = { familyName = it },
                label = { Text("Family Name") },
                modifier = Modifier.fillMaxWidth().testTag("edit_family_name_input")
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Text("ADDRESS DETAILS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)

            OutlinedTextField(
                value = doorNo,
                onValueChange = { doorNo = it },
                label = { Text("House/Door Number") },
                modifier = Modifier.fillMaxWidth().testTag("edit_door_no_input")
            )

            OutlinedTextField(
                value = street,
                onValueChange = { street = it },
                label = { Text("Street Name") },
                modifier = Modifier.fillMaxWidth().testTag("edit_street_input")
            )

            OutlinedTextField(
                value = locality,
                onValueChange = { locality = it },
                label = { Text("Locality") },
                modifier = Modifier.fillMaxWidth().testTag("edit_locality_input")
            )

            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City") },
                modifier = Modifier.fillMaxWidth().testTag("edit_city_input")
            )

            OutlinedTextField(
                value = state,
                onValueChange = { state = it },
                label = { Text("State") },
                modifier = Modifier.fillMaxWidth().testTag("edit_state_input")
            )

            OutlinedTextField(
                value = zip,
                onValueChange = { zip = it },
                label = { Text("PIN Code / ZIP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("edit_zip_input")
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Text("ADDITIONAL DETAILS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)

            OutlinedTextField(
                value = additionalInfo,
                onValueChange = { additionalInfo = it },
                label = { Text("Additional Information") },
                modifier = Modifier.fillMaxWidth().testTag("edit_additional_info_input")
            )

            OutlinedTextField(
                value = weddingDate,
                onValueChange = { weddingDate = it },
                label = { Text("Family Wedding Date") },
                placeholder = { Text("YYYY-MM-DD") },
                trailingIcon = {
                    IconButton(onClick = {
                        val wedParts = weddingDate.split("-")
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
                modifier = Modifier.fillMaxWidth().testTag("edit_wedding_date_input")
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

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (familyName.isNotBlank()) {
                        val combinedAddress = listOf(doorNo, street, locality, city, state, zip)
                            .filter { it.isNotBlank() }
                            .joinToString(", ")
                        
                        val updatedFamily = familyUnit.family.copy(
                            familyName = familyName,
                            address = combinedAddress,
                            additionalInfo = additionalInfo.takeIf { it.isNotBlank() },
                            weddingDate = weddingDate.takeIf { it.isNotBlank() },
                            relatedFamilies = relatedFamiliesText.takeIf { it.isNotBlank() }
                        )

                        viewModel.saveFamily(updatedFamily) {
                            onComplete()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_edit_family_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save Changes", style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    if (showLinkFamilyDialog && familyUnits.isNotEmpty()) {
        val otherFamilies = familyUnits.filter { it.family.id != familyId }
        AlertDialog(
            onDismissRequest = { showLinkFamilyDialog = false },
            title = { Text("Link Existing Family") },
            text = {
                if (otherFamilies.isEmpty()) {
                    Text("No other families available to link.")
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        otherFamilies.forEach { unit ->
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
            text = { Text("There are no other families to link.") },
            confirmButton = {
                TextButton(onClick = { showLinkFamilyDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}
