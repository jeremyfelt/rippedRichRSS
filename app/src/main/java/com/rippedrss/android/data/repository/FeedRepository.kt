package com.rippedrss.android.data.repository

import com.rippedrss.android.data.dao.ArticleDao
import com.rippedrss.android.data.dao.FeedDao
import com.rippedrss.android.data.model.Feed
import com.rippedrss.android.data.rss.DiscoveredFeed
import com.rippedrss.android.data.rss.FaviconFetcher
import com.rippedrss.android.data.rss.FeedDiscoverer
import com.rippedrss.android.data.rss.FeedFetcher
import com.rippedrss.android.data.rss.FeedRefreshResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FeedRepository(
    private val feedDao: FeedDao,
    private val articleDao: ArticleDao,
    private val feedFetcher: FeedFetcher,
    private val feedDiscoverer: FeedDiscoverer,
    private val faviconFetcher: FaviconFetcher
) {

    fun getAllFeeds(): Flow<List<Feed>> = feedDao.getAllFeeds()

    suspend fun getFeedById(feedId: String): Feed? = feedDao.getFeedById(feedId)

    suspend fun addFeed(url: String): Result<Feed> = withContext(Dispatchers.IO) {
        try {
            // Check if feed already exists
            val existing = feedDao.getFeedByUrl(url)
            if (existing != null) {
                return@withContext Result.failure(Exception("Feed already exists"))
            }

            // Discover the feed
            val discovered = feedDiscoverer.discoverFeed(url)
                ?: return@withContext Result.failure(Exception("Could not discover feed at URL"))

            // Get favicon
            val faviconUrl = faviconFetcher.getFaviconUrl(discovered.siteUrl ?: url)

            // Create and insert feed
            val feed = Feed(
                title = discovered.title,
                feedUrl = discovered.feedUrl,
                siteUrl = discovered.siteUrl,
                faviconUrl = faviconUrl
            )

            feedDao.insertFeed(feed)

            // Fetch initial articles
            feedFetcher.refreshFeed(feed)

            Result.success(feed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFeed(feed: Feed) = withContext(Dispatchers.IO) {
        articleDao.deleteArticlesByFeed(feed.id)
        feedDao.deleteFeed(feed)
    }

    suspend fun toggleFavorite(feedId: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        feedDao.updateFavorite(feedId, isFavorite)
    }

    suspend fun refreshAllFeeds(): List<FeedRefreshResult> = withContext(Dispatchers.IO) {
        feedFetcher.refreshAllFeeds()
    }

    suspend fun refreshFeed(feed: Feed): FeedRefreshResult = withContext(Dispatchers.IO) {
        feedFetcher.refreshFeed(feed)
    }

    suspend fun refreshFeedsForBackground(maxFeeds: Int = 10): List<FeedRefreshResult> = withContext(Dispatchers.IO) {
        feedFetcher.refreshFeedsForBackground(maxFeeds)
    }
}
