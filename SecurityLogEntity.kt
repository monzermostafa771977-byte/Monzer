package com.monzer.hares

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "security_logs")
data class SecurityLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val riskLevel: String,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)
