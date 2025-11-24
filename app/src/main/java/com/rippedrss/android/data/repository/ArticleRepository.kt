package com.rippedrss.android.data.repository

import com.rippedrss.android.data.dao.ArticleDao
import com.rippedrss.android.data.model.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ArticleRepository(private val articleDao: ArticleDao) {

    fun getAllArticles(): Flow<List<Article>> = articleDao.getAllArticles()

    fun getArticlesByFeed(feedId: String): Flow<List<Article>> = articleDao.getArticlesByFeed(feedId)

    fun getSavedArticles(): Flow<List<Article>> = articleDao.getSavedArticles()

    fun getUnreadArticles(): Flow<List<Article>> = articleDao.getUnreadArticles()

    fun getUnreadCount(): Flow<Int> = articleDao.getUnreadCount()

    suspend fun getArticleById(articleId: String): Article? = articleDao.getArticleById(articleId)

    suspend fun markAsRead(articleId: String, isRead: Boolean = true) = withContext(Dispatchers.IO) {
        articleDao.updateReadStatus(articleId, isRead)
    }

    suspend fun toggleSaved(articleId: String, isSaved: Boolean) = withContext(Dispatchers.IO) {
        articleDao.updateSavedStatus(articleId, isSaved)
    }
}
