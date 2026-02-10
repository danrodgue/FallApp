package com.example.fallapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.fallapp.data.local.dao.NinotDao
import com.example.fallapp.data.local.entity.NinotEntity

@Database(
    entities = [NinotEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FallAppDatabase : RoomDatabase() {
    abstract fun ninotDao(): NinotDao
}

