package com.akari.app.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Read-only Health Connect access. Entirely optional: when Health Connect is
 * absent, not granted, or errors, every call degrades to null and the app
 * paces by feel. Never writes, never simulates bpm, never touches the network.
 */
class HealthConnectRepository(private val context: Context) {

    /** All read permissions Akari can ever ask for — nothing else. */
    val permissions: Set<String> = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(RestingHeartRateRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
    )

    fun availability(): Availability = when (HealthConnectClient.getSdkStatus(context)) {
        HealthConnectClient.SDK_AVAILABLE -> Availability.AVAILABLE
        HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> Availability.UPDATE_REQUIRED
        else -> Availability.UNAVAILABLE
    }

    private fun clientOrNull(): HealthConnectClient? =
        if (availability() == Availability.AVAILABLE)
            runCatching { HealthConnectClient.getOrCreate(context) }.getOrNull()
        else null

    suspend fun grantedPermissions(): Set<String> =
        clientOrNull()?.permissionController?.let {
            runCatching { it.getGrantedPermissions() }.getOrDefault(emptySet())
        } ?: emptySet()

    suspend fun hasAllPermissions(): Boolean = grantedPermissions().containsAll(permissions)

    /** Latest heart-rate sample in the last 30 minutes, or null. */
    suspend fun latestHeartRate(): Int? {
        val client = clientOrNull() ?: return null
        return runCatching {
            val end = Instant.now()
            val start = end.minus(30, ChronoUnit.MINUTES)
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end),
                ),
            )
            response.records
                .flatMap { it.samples }
                .maxByOrNull { it.time }
                ?.beatsPerMinute
                ?.toInt()
        }.getOrNull()
    }

    /** Latest resting heart rate reading, or null. */
    suspend fun latestRestingHeartRate(): Int? {
        val client = clientOrNull() ?: return null
        return runCatching {
            val end = Instant.now()
            val start = end.minus(2, ChronoUnit.DAYS)
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = RestingHeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end),
                ),
            )
            response.records.maxByOrNull { it.time }?.beatsPerMinute?.toInt()
        }.getOrNull()
    }

    enum class Availability { AVAILABLE, UPDATE_REQUIRED, UNAVAILABLE }
}
