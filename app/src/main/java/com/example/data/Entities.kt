package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "families")
data class Family(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val familyName: String,
    val headMemberId: Long = 0,
    val relatedFamilies: String? = null,
    val additionalInfo: String? = null,
    val weddingDate: String? = null // Format: YYYY-MM-DD
)

@Entity(tableName = "members")
data class Member(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val familyId: Long,
    val firstName: String,
    val lastName: String,
    val role: String, // "Head", "Spouse", "Child", "Parent", "Sibling", "Grandparent", "Other"
    val phoneNumber: String,
    val address: String,
    val dateOfBirth: String, // Format: YYYY-MM-DD
    val lastVisitedDate: String? = null // Cache of latest visit log date
) {
    val fullName: String get() = "$firstName $lastName"
}

@Entity(tableName = "visit_logs")
data class VisitLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memberId: Long,
    val visitDate: String, // Format: YYYY-MM-DD
    val reason: String,
    val prayerRequests: String,
    val notes: String
)
