package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        StudentProfileEntity::class,
        CertificateEntity::class,
        QuizHistoryEntity::class,
        ForumPostEntity::class,
        SystemMasteryEntity::class,
        BookmarkEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AnatomyDatabase : RoomDatabase() {

    abstract fun anatomyDao(): AnatomyDao

    companion object {
        @Volatile
        private var INSTANCE: AnatomyDatabase? = null

        fun getInstance(context: Context): AnatomyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AnatomyDatabase::class.java,
                    "anatomy_learning_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
