package com.rippedrss.android.data.dao

import androidx.room.*
import com.rippedrss.android.data.model.Feed
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {
    @Query("SELECT * FROM feeds ORDER BY isFavorite DESC, title ASC")
    fun getAllFeeds(): Flow<List<Feed>>

    @Query("SELECT * FROM feeds WHERE id = :feedId")
    suspend fun getFeedById(feedId: String): Feed?

    @Query("SELECT * FROM feeds WHERE feedUrl = :feedUrl")
    suspend fun getFeedByUrl(feedUrl: String): Feed?

    @Query("SELECT * FROM feeds WHERE isFavorite = 1 ORDER BY lastUpdated ASC")
    suspend fun getFavoriteFeedsForRefresh(): List<Feed>

    @Query("SELECT * FROM feeds WHERE isFavorite = 0 ORDER BY lastUpdated ASC NULLS FIRST LIMIT :limit")
    suspend fun getOldestNonFavoriteFeeds(limit: Int): List<Feed>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeed(feed: Feed)

    @Update
    suspend fun updateFeed(feed: Feed)

    @Delete
    suspend fun deleteFeed(feed: Feed)

    @Query("UPDATE feeds SET lastUpdated = :timestamp, lastRefreshError = NULL WHERE id = :feedId")
    suspend fun updateLastRefreshSuccess(feedId: String, timestamp: Long)

    @Query("UPDATE feeds SET lastRefreshError = :error WHERE id = :feedId")
    suspend fun updateLastRefreshError(feedId: String, error: String)

    @Query("UPDATE feeds SET isFavorite = :isFavorite WHERE id = :feedId")
    suspend fun updateFavorite(feedId: String, isFavorite: Boolean)
}
