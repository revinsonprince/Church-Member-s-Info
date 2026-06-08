package com.example.data

import kotlinx.coroutines.flow.Flow

class ChurchRepository(private val churchDao: ChurchDao) {

    val allFamilies: Flow<List<Family>> = churchDao.getAllFamilies()
    val allMembers: Flow<List<Member>> = churchDao.getAllMembers()
    val allVisitLogs: Flow<List<VisitLog>> = churchDao.getAllVisitLogs()

    suspend fun getFamilyById(familyId: Long): Family? {
        return churchDao.getFamilyById(familyId)
    }

    suspend fun getMembersByFamilyId(familyId: Long): List<Member> {
        return churchDao.getMembersByFamilyId(familyId)
    }

    suspend fun getMemberById(memberId: Long): Member? {
        return churchDao.getMemberById(memberId)
    }

    suspend fun insertFamily(family: Family): Long {
        return churchDao.insertFamily(family)
    }

    suspend fun updateFamily(family: Family) {
        churchDao.updateFamily(family)
    }

    suspend fun deleteFamily(family: Family) {
        churchDao.deleteFamily(family)
    }

    suspend fun insertMember(member: Member): Long {
        return churchDao.insertMember(member)
    }

    suspend fun updateMember(member: Member) {
        churchDao.updateMember(member)
    }

    suspend fun deleteMember(member: Member) {
        churchDao.deleteMember(member)
    }

    fun getVisitLogsForMember(memberId: Long): Flow<List<VisitLog>> {
        return churchDao.getVisitLogsForMember(memberId)
    }

    suspend fun insertVisitLog(visitLog: VisitLog): Long {
        return churchDao.insertVisitLog(visitLog)
    }

    suspend fun updateVisitLog(visitLog: VisitLog) {
        churchDao.updateVisitLog(visitLog)
    }

    suspend fun deleteVisitLog(visitLog: VisitLog) {
        churchDao.deleteVisitLog(visitLog)
    }

    suspend fun saveMemberVisit(visitLog: VisitLog, visitDate: String) {
        churchDao.saveMemberVisit(visitLog, visitDate)
    }
}
