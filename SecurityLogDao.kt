package com.monzer.hares

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SecurityLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SecurityLogEntity)

    @Query("SELECT * FROM security_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<SecurityLogEntity>>

    @Query("DELETE FROM security_logs")
    suspend fun clearLogs()
}
