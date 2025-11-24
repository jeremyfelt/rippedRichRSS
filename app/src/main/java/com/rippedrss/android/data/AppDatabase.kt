package com.rippedrss.android.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rippedrss.android.data.dao.ArticleDao
import com.rippedrss.android.data.dao.FeedDao
import com.rippedrss.android.data.model.Article
import com.rippedrss.android.data.model.Feed

@Database(
    entities = [Feed::class, Article::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun feedDao(): FeedDao
    abstract fun articleDao(): ArticleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from version 1 to 2: Add unique index for article deduplication
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create unique index on feedId + guid for article deduplication
                // First, remove duplicates keeping the oldest entry
                db.execSQL("""
                    DELETE FROM articles
                    WHERE uniqueId NOT IN (
                        SELECT MIN(uniqueId)
                        FROM articles
                        WHERE guid IS NOT NULL
                        GROUP BY feedId, guid
                    )
                    AND guid IS NOT NULL
                """)

                // Create the unique index
                db.execSQL("""
                    CREATE UNIQUE INDEX IF NOT EXISTS index_articles_feedId_guid
                    ON articles (feedId, guid)
                    WHERE guid IS NOT NULL
                """)
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ripped_rss_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    // Only use destructive migration as last resort for major schema changes
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Clears the database instance. Used for testing or data reset.
         */
        fun clearInstance() {
            INSTANCE = null
        }
    }
}
