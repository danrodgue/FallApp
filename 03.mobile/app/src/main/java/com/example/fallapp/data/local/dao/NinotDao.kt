package com.example.fallapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fallapp.data.local.entity.NinotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NinotDao {

    @Query("SELECT * FROM ninots ORDER BY premiado DESC, totalVotos DESC")
    fun observeAll(): Flow<List<NinotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<NinotEntity>)

    @Query("DELETE FROM ninots")
    suspend fun clear()
}

