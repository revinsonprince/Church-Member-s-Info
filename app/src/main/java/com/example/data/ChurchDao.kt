package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChurchDao {

    // Families
    @Query("SELECT * FROM families ORDER BY familyName ASC")
    fun getAllFamilies(): Flow<List<Family>>

    @Query("SELECT * FROM families WHERE id = :familyId")
    suspend fun getFamilyById(familyId: Long): Family?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamily(family: Family): Long

    @Update
    suspend fun updateFamily(family: Family)

    @Delete
    suspend fun deleteFamily(family: Family)

    // Members
    @Query("SELECT * FROM members ORDER BY lastName ASC, firstName ASC")
    fun getAllMembers(): Flow<List<Member>>

    @Query("SELECT * FROM members ORDER BY lastName ASC, firstName ASC")
    suspend fun getAllMembersSync(): List<Member>

    @Query("SELECT * FROM members WHERE familyId = :familyId")
    suspend fun getMembersByFamilyId(familyId: Long): List<Member>

    @Query("SELECT * FROM members WHERE id = :memberId")
    suspend fun getMemberById(memberId: Long): Member?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Member): Long

    @Update
    suspend fun updateMember(member: Member)

    @Delete
    suspend fun deleteMember(member: Member)

    // Visit Logs
    @Query("SELECT * FROM visit_logs ORDER BY visitDate DESC")
    fun getAllVisitLogs(): Flow<List<VisitLog>>

    @Query("SELECT * FROM visit_logs WHERE memberId = :memberId ORDER BY visitDate DESC")
    fun getVisitLogsForMember(memberId: Long): Flow<List<VisitLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisitLog(visitLog: VisitLog): Long

    @Update
    suspend fun updateVisitLog(visitLog: VisitLog)

    @Delete
    suspend fun deleteVisitLog(visitLog: VisitLog)
    
    // Joint Transactions
    @Transaction
    suspend fun saveMemberVisit(visitLog: VisitLog, visitDate: String) {
        insertVisitLog(visitLog)
        val member = getMemberById(visitLog.memberId)
        if (member != null) {
            // Update last visited date cached on member
            val updatedMember = member.copy(lastVisitedDate = visitDate)
            updateMember(updatedMember)
        }
    }
}
