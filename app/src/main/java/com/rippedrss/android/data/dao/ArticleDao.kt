package com.rippedrss.android.data.dao

import androidx.room.*
import com.rippedrss.android.data.model.Article
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles ORDER BY pubDate DESC")
    fun getAllArticles(): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE feedId = :feedId ORDER BY pubDate DESC")
    fun getArticlesByFeed(feedId: String): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE isSaved = 1 ORDER BY pubDate DESC")
    fun getSavedArticles(): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE isRead = 0 ORDER BY pubDate DESC")
    fun getUnreadArticles(): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE uniqueId = :articleId")
    suspend fun getArticleById(articleId: String): Article?

    @Query("SELECT * FROM articles WHERE feedId = :feedId AND guid = :guid")
    suspend fun getArticleByGuid(feedId: String, guid: String): Article?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertArticles(articles: List<Article>)

    @Update
    suspend fun updateArticle(article: Article)

    @Delete
    suspend fun deleteArticle(article: Article)

    @Query("DELETE FROM articles WHERE feedId = :feedId")
    suspend fun deleteArticlesByFeed(feedId: String)

    @Query("UPDATE articles SET isRead = :isRead WHERE uniqueId = :articleId")
    suspend fun updateReadStatus(articleId: String, isRead: Boolean)

    @Query("UPDATE articles SET isSaved = :isSaved WHERE uniqueId = :articleId")
    suspend fun updateSavedStatus(articleId: String, isSaved: Boolean)

    @Query("SELECT COUNT(*) FROM articles WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>
}
