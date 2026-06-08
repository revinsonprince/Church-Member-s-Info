package com.example.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.utils.BackupUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

// Domain model combining Family with its Members
data class FamilyUnit(
    val family: Family,
    val head: Member?,
    val members: List<Member>
)

// UI state for Upcoming Events
data class EventReminder(
    val member: Member,
    val eventType: String,
    val years: Int,
    val daysRemaining: Int,
    val dateStr: String
)

class MainViewModel(private val repository: ChurchRepository) : ViewModel() {

    private val TAG = "MainViewModel"

    // Raw sources from repository Flow
    val families: StateFlow<List<Family>> = repository.allFamilies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val members: StateFlow<List<Member>> = repository.allMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVisitLogs: StateFlow<List<VisitLog>> = repository.allVisitLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search query backing state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Combined Relational Units
    val familyUnits: StateFlow<List<FamilyUnit>> = combine(families, members) { fams, mems ->
        fams.map { fam ->
            val familyMems = mems.filter { m -> m.familyId == fam.id }
            val head = familyMems.find { m -> m.id == fam.headMemberId || m.role.lowercase() == "head" }
            FamilyUnit(
                family = fam,
                head = head,
                members = familyMems
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Relational Units based on search query
    val filteredFamilyUnits: StateFlow<List<FamilyUnit>> = combine(familyUnits, searchQuery) { units, query ->
        if (query.isBlank()) {
            units
        } else {
            units.filter { unit ->
                val matchFamilyName = unit.family.familyName.contains(query, ignoreCase = true)
                val matchHead = unit.head?.fullName?.contains(query, ignoreCase = true) == true
                val matchMember = unit.members.any { m ->
                    m.fullName.contains(query, ignoreCase = true) ||
                    m.phoneNumber.contains(query, ignoreCase = true) ||
                    m.address.contains(query, ignoreCase = true)
                }
                matchFamilyName || matchHead || matchMember
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Events calculations for next 30 days
    val upcomingEvents: StateFlow<List<EventReminder>> = members.map { mems ->
        val reminders = mutableListOf<EventReminder>()
        mems.forEach { m ->
            val bday = calculateEventReminder(m, m.dateOfBirth, "Birthday")
            if (bday != null && bday.daysRemaining in 0..30) reminders.add(bday)
            
            val wed = calculateEventReminder(m, m.weddingDate, "Anniversary")
            if (wed != null && wed.daysRemaining in 0..30) reminders.add(wed)
        }
        reminders.sortedBy { it.daysRemaining }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Insert or edit family
    fun saveFamily(family: Family, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            if (family.id == 0L) {
                val newId = repository.insertFamily(family)
                onComplete(newId)
            } else {
                repository.updateFamily(family)
                onComplete(family.id)
            }
        }
    }

    fun createFamilyWithMembers(
        headFirstName: String,
        headLastName: String,
        headPhone: String,
        headAddress: String,
        headDob: String,
        headWeddingDate: String?,
        relatedFamilies: String?,
        additionalMembers: List<DraftMember>,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val familyName = "$headFirstName's Family"
            val newFamily = Family(familyName = familyName, relatedFamilies = relatedFamilies)
            val familyId = repository.insertFamily(newFamily)

            val headMember = Member(
                familyId = familyId,
                firstName = headFirstName,
                lastName = headLastName,
                role = "Head",
                phoneNumber = headPhone,
                address = headAddress,
                dateOfBirth = headDob.ifBlank { "2000-01-01" },
                weddingDate = headWeddingDate,
                lastVisitedDate = null
            )
            val headId = repository.insertMember(headMember)
            
            // Update family with head ID
            repository.updateFamily(newFamily.copy(id = familyId, headMemberId = headId))

            additionalMembers.forEach { m ->
                val newMember = Member(
                    familyId = familyId,
                    firstName = m.firstName,
                    lastName = m.lastName,
                    role = m.role,
                    phoneNumber = m.phoneNumber ?: headPhone, // Use provided phone or inherit
                    address = headAddress,   // Inherits address
                    dateOfBirth = m.dateOfBirth.ifBlank { "2000-01-01" },
                    weddingDate = m.weddingDate,
                    lastVisitedDate = null
                )
                repository.insertMember(newMember)
            }
            onComplete()
        }
    }

    // Complete pipeline to save contact member
    fun saveMember(
        memberId: Long,
        firstName: String,
        lastName: String,
        role: String,
        familyOption: FamilyOption,
        phoneNumber: String,
        address: String,
        dateOfBirth: String,
        weddingDate: String?,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            var finalFamilyId = 0L

            when (familyOption) {
                is FamilyOption.JoinExisting -> {
                    finalFamilyId = familyOption.familyId
                }
                is FamilyOption.CreateNewFamily -> {
                    // Create Family unit first
                    val newFamilyName = familyOption.familyName.ifBlank { "$firstName's Family" }
                    val newFamily = Family(familyName = newFamilyName)
                    finalFamilyId = repository.insertFamily(newFamily)
                }
            }

            val savedMember = Member(
                id = memberId,
                familyId = finalFamilyId,
                firstName = firstName,
                lastName = lastName,
                role = role,
                phoneNumber = phoneNumber,
                address = address,
                dateOfBirth = dateOfBirth.ifBlank { "2000-01-01" },
                weddingDate = weddingDate,
                lastVisitedDate = if (memberId != 0L) repository.getMemberById(memberId)?.lastVisitedDate else null
            )

            if (memberId == 0L) {
                val newMemberId = repository.insertMember(savedMember)
                // If this is the Head role, update family head pointer
                if (role.lowercase() == "head" || role.lowercase() == "head of family") {
                    val family = repository.getFamilyById(finalFamilyId)
                    if (family != null) {
                        repository.updateFamily(family.copy(headMemberId = newMemberId))
                    }
                }
            } else {
                repository.updateMember(savedMember)
                if (role.lowercase() == "head" || role.lowercase() == "head of family") {
                    val family = repository.getFamilyById(finalFamilyId)
                    if (family != null) {
                        repository.updateFamily(family.copy(headMemberId = memberId))
                    }
                }
            }
            onComplete()
        }
    }

    fun deleteMember(member: Member, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteMember(member)
            // If they were the family head, clear head pointers
            val family = repository.getFamilyById(member.familyId)
            if (family != null && family.headMemberId == member.id) {
                // Find next member or clear
                val remainingMems = repository.getMembersByFamilyId(family.id)
                val newHead = remainingMems.find { it.role.lowercase() == "head" } ?: remainingMems.firstOrNull()
                if (newHead != null) {
                    repository.updateFamily(family.copy(headMemberId = newHead.id))
                    repository.updateMember(newHead.copy(role = "Head"))
                } else {
                    repository.deleteFamily(family)
                }
            }
            onComplete()
        }
    }

    fun deleteFamily(familyUnit: FamilyUnit) {
        viewModelScope.launch {
            familyUnit.members.forEach { m -> repository.deleteMember(m) }
            repository.deleteFamily(familyUnit.family)
        }
    }

    fun addDraftMemberToFamily(familyId: Long, headMember: Member?, draft: DraftMember) {
        viewModelScope.launch {
            val phone = draft.phoneNumber?.takeIf { it.isNotBlank() } ?: headMember?.phoneNumber ?: ""
            val address = headMember?.address ?: ""
            val newMember = Member(
                familyId = familyId,
                firstName = draft.firstName,
                lastName = draft.lastName,
                role = draft.role,
                phoneNumber = phone,
                address = address,
                dateOfBirth = draft.dateOfBirth.ifBlank { "2000-01-01" },
                weddingDate = draft.weddingDate,
                lastVisitedDate = null
            )
            repository.insertMember(newMember)
        }
    }

    // Visit logs operations
    fun addVisitLog(memberId: Long, visitDate: String, reason: String, prayerRequests: String, notes: String) {
        viewModelScope.launch {
            val log = VisitLog(
                memberId = memberId,
                visitDate = visitDate.ifBlank { getTodayString() },
                reason = reason,
                prayerRequests = prayerRequests,
                notes = notes
            )
            repository.saveMemberVisit(log, log.visitDate)
        }
    }

    fun deleteVisitLog(log: VisitLog) {
        viewModelScope.launch {
            repository.deleteVisitLog(log)
        }
    }

    // Export Trigger
    fun performExport(context: Context, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val fams = families.value
            val mems = members.value
            val logs = allVisitLogs.value
            val uri = BackupUtils.exportData(context, fams, mems, logs)
            if (uri != null) {
                BackupUtils.shareBackup(context, uri)
                onComplete("Backup generated and ready for sharing!")
            } else {
                onComplete("Failed to export backup.")
            }
        }
    }

    // Import Trigger
    fun performImport(context: Context, uri: Uri, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val backup = BackupUtils.importData(context, uri)
            if (backup != null) {
                // Wipe DB tables sequentially & insert
                try {
                    // For safety, let's insert or replace. First we can clear local tables
                    // To do a complete restore:
                    for (m in members.value) repository.deleteMember(m)
                    for (f in families.value) repository.deleteFamily(f)
                    for (l in allVisitLogs.value) repository.deleteVisitLog(l)

                    // Insert backups
                    for (f in backup.families) repository.insertFamily(f)
                    for (m in backup.members) repository.insertMember(m)
                    for (l in backup.visitLogs) repository.insertVisitLog(l)

                    onComplete("Import completed successfully! ${backup.members.size} members restored.")
                } catch (e: Exception) {
                    Log.e(TAG, "Database restore failed", e)
                    onComplete("Failed to restore backup: ${e.localizedMessage}")
                }
            } else {
                onComplete("Invalid backup file or parse error.")
            }
        }
    }

    // Internal date utility functions
    private fun getTodayString(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = String.format("%02d", calendar.get(Calendar.MONTH) + 1)
        val day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))
        return "$year-$month-$day"
    }

    private fun calculateEventReminder(member: Member, dateStr: String?, eventType: String): EventReminder? {
        if (dateStr.isNullOrBlank()) return null
        val parts = dateStr.split("-")
        if (parts.size < 3) return null

        val eventYear = parts[0].toIntOrNull() ?: return null
        val month = parts[1].toIntOrNull() ?: return null
        val day = parts[2].toIntOrNull() ?: return null

        val today = Calendar.getInstance()
        val currentYear = today.get(Calendar.YEAR)

        val eventThisYear = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        var nextEventYear = currentYear
        if (eventThisYear.before(today) && eventThisYear.get(Calendar.DAY_OF_MONTH) != today.get(Calendar.DAY_OF_MONTH)) {
            eventThisYear.add(Calendar.YEAR, 1)
            nextEventYear++
        }

        val diffMillis = eventThisYear.timeInMillis - today.timeInMillis
        val days = (diffMillis / (1000 * 60 * 60 * 24)).toInt()

        val yearsPassed = nextEventYear - eventYear

        return EventReminder(
            member = member,
            eventType = eventType,
            years = yearsPassed,
            daysRemaining = if (days < 0) 0 else days,
            dateStr = dateStr
        )
    }
}

// Support types for creating/editing member linkages
sealed class FamilyOption {
    data class JoinExisting(val familyId: Long) : FamilyOption()
    data class CreateNewFamily(val familyName: String) : FamilyOption()
}

// Custom simple provider factory
class MainViewModelFactory(private val repository: ChurchRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
