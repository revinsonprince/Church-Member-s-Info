package com.example.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.Family
import com.example.data.Member
import com.example.data.VisitLog
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object BackupUtils {
    private const val TAG = "BackupUtils"
    private const val FILE_NAME = "shepherd_tracker_backup.json"
    private const val AUTHORITY = "com.aistudio.shepherdvisites.ntxyrw.fileprovider"

    fun exportData(
        context: Context,
        families: List<Family>,
        members: List<Member>,
        visitLogs: List<VisitLog>
    ): Uri? {
        try {
            val root = JSONObject()
            root.put("version", 1)
            root.put("exportDate", System.currentTimeMillis())

            // Families
            val familiesArray = JSONArray()
            for (f in families) {
                val fObj = JSONObject()
                fObj.put("id", f.id)
                fObj.put("familyName", f.familyName)
                fObj.put("headMemberId", f.headMemberId)
                fObj.put("relatedFamilies", f.relatedFamilies ?: JSONObject.NULL)
                fObj.put("additionalInfo", f.additionalInfo ?: JSONObject.NULL)
                fObj.put("weddingDate", f.weddingDate ?: JSONObject.NULL)
                fObj.put("address", f.address)
                familiesArray.put(fObj)
            }
            root.put("families", familiesArray)

            // Members
            val membersArray = JSONArray()
            for (m in members) {
                val mObj = JSONObject()
                mObj.put("id", m.id)
                mObj.put("familyId", m.familyId)
                mObj.put("firstName", m.firstName)
                mObj.put("lastName", m.lastName)
                mObj.put("role", m.role)
                mObj.put("phoneNumber", m.phoneNumber)
                mObj.put("dateOfBirth", m.dateOfBirth)
                mObj.put("lastVisitedDate", m.lastVisitedDate ?: JSONObject.NULL)
                membersArray.put(mObj)
            }
            root.put("members", membersArray)

            // Visit Logs
            val logsArray = JSONArray()
            for (l in visitLogs) {
                val lObj = JSONObject()
                lObj.put("id", l.id)
                lObj.put("memberId", l.memberId)
                lObj.put("visitDate", l.visitDate)
                lObj.put("reason", l.reason)
                lObj.put("prayerRequests", l.prayerRequests)
                lObj.put("notes", l.notes)
                logsArray.put(lObj)
            }
            root.put("visitLogs", logsArray)

            // Write to local cache file
            val cacheFile = File(context.cacheDir, FILE_NAME)
            FileOutputStream(cacheFile).use { writer ->
                writer.write(root.toString(2).toByteArray())
            }

            // Generate content URI using FileProvider
            return FileProvider.getUriForFile(context, AUTHORITY, cacheFile)
        } catch (e: Exception) {
            Log.e(TAG, "Export failed", e)
            return null
        }
    }

    fun shareBackup(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share Shepherd Backup via:"))
    }

    data class BackupData(
        val families: List<Family>,
        val members: List<Member>,
        val visitLogs: List<VisitLog>
    )

    fun importData(context: Context, uri: Uri): BackupData? {
        try {
            val contentResolver = context.contentResolver
            val jsonString = contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader().use { it.readText() }
            } ?: return null

            val root = JSONObject(jsonString)
            
            val families = mutableListOf<Family>()
            val familiesArray = root.optJSONArray("families")
            if (familiesArray != null) {
                for (i in 0 until familiesArray.length()) {
                    val obj = familiesArray.getJSONObject(i)
                    families.add(
                        Family(
                            id = obj.optLong("id"),
                            familyName = obj.optString("familyName"),
                            headMemberId = obj.optLong("headMemberId"),
                            relatedFamilies = if (obj.isNull("relatedFamilies")) null else obj.optString("relatedFamilies"),
                            additionalInfo = if (obj.isNull("additionalInfo")) null else obj.optString("additionalInfo"),
                            weddingDate = if (obj.isNull("weddingDate")) null else obj.optString("weddingDate"),
                            address = obj.optString("address", "")
                        )
                    )
                }
            }

            val members = mutableListOf<Member>()
            val membersArray = root.optJSONArray("members")
            if (membersArray != null) {
                for (i in 0 until membersArray.length()) {
                    val obj = membersArray.getJSONObject(i)
                    members.add(
                        Member(
                            id = obj.optLong("id"),
                            familyId = obj.optLong("familyId"),
                            firstName = obj.optString("firstName"),
                            lastName = obj.optString("lastName"),
                            role = obj.optString("role"),
                            phoneNumber = obj.optString("phoneNumber"),
                            dateOfBirth = obj.optString("dateOfBirth"),
                            lastVisitedDate = if (obj.isNull("lastVisitedDate")) null else obj.optString("lastVisitedDate")
                        )
                    )
                }
            }

            val visitLogs = mutableListOf<VisitLog>()
            val logsArray = root.optJSONArray("visitLogs")
            if (logsArray != null) {
                for (i in 0 until logsArray.length()) {
                    val obj = logsArray.getJSONObject(i)
                    visitLogs.add(
                        VisitLog(
                            id = obj.optLong("id"),
                            memberId = obj.optLong("memberId"),
                            visitDate = obj.optString("visitDate"),
                            reason = obj.optString("reason"),
                            prayerRequests = obj.optString("prayerRequests"),
                            notes = obj.optString("notes")
                        )
                    )
                }
            }

            return BackupData(families, members, visitLogs)
        } catch (e: Exception) {
            Log.e(TAG, "Import failed", e)
            return null
        }
    }
}
