package com.rippedrss.android.data.rss

import android.util.Log
import com.rippedrss.android.data.dao.ArticleDao
import com.rippedrss.android.data.dao.FeedDao
import com.rippedrss.android.data.model.Feed
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

data class FeedRefreshResult(
    val feedId: String,
    val success: Boolean,
    val error: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

class FeedFetcher(
    private val feedDao: FeedDao,
    private val articleDao: ArticleDao,
    private val okHttpClient: OkHttpClient
) {
    private val parser = RssFeedParser()
    private val maxConcurrent = 8

    companion object {
        private const val TAG = "FeedFetcher"
    }

    suspend fun refreshAllFeeds(): List<FeedRefreshResult> = withContext(Dispatchers.IO) {
        val feeds = feedDao.getAllFeeds()

        // Get the current list of feeds
        val feedList = mutableListOf<Feed>()
        val job = launch {
            feeds.collect { list ->
                feedList.clear()
                feedList.addAll(list)
                cancel() // Only take the first emission
            }
        }
        job.join()

        if (feedList.isEmpty()) {
            return@withContext emptyList()
        }

        val startTime = System.currentTimeMillis()
        val results = mutableListOf<FeedRefreshResult>()
        val resultsLock = Any()

        // Process feeds concurrently with a limit
        coroutineScope {
            val semaphore = kotlinx.coroutines.sync.Semaphore(maxConcurrent)

            feedList.map { feed ->
                async {
                    semaphore.withPermit {
                        val result = refreshFeed(feed)
                        synchronized(resultsLock) {
                            results.add(result)
                        }
                        result
                    }
                }
            }.awaitAll()
        }

        val duration = (System.currentTimeMillis() - startTime) / 1000.0
        val successCount = results.count { it.success }
        Log.d(TAG, "✅ Parallel refresh completed in ${String.format("%.2f", duration)}s: $successCount/${results.size} feeds successful")

        results
    }

    suspend fun refreshFeed(feed: Feed): FeedRefreshResult = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(feed.feedUrl)
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                val error = "HTTP ${response.code}"
                feedDao.updateLastRefreshError(feed.id, error)
                return@withContext FeedRefreshResult(feed.id, false, error)
            }

            response.body?.byteStream()?.use { inputStream ->
                val parsedFeed = parser.parse(inputStream, feed.id, feed.feedUrl)

                // Update feed metadata if needed
                if (feed.title != parsedFeed.title || feed.siteUrl != parsedFeed.siteUrl) {
                    val updatedFeed = feed.copy(
                        title = parsedFeed.title,
                        siteUrl = parsedFeed.siteUrl ?: feed.siteUrl,
                        feedSummary = parsedFeed.description ?: feed.feedSummary
                    )
                    feedDao.updateFeed(updatedFeed)
                }

                // Insert new articles
                articleDao.insertArticles(parsedFeed.articles)

                // Update last refresh timestamp
                feedDao.updateLastRefreshSuccess(feed.id, System.currentTimeMillis())

                FeedRefreshResult(feed.id, true)
            } ?: run {
                val error = "Empty response body"
                feedDao.updateLastRefreshError(feed.id, error)
                FeedRefreshResult(feed.id, false, error)
            }
        } catch (e: Exception) {
            val error = e.message ?: "Unknown error"
            Log.e(TAG, "Error refreshing feed ${feed.title}: $error", e)
            feedDao.updateLastRefreshError(feed.id, error)
            FeedRefreshResult(feed.id, false, error)
        }
    }

    suspend fun refreshFeedsForBackground(maxFeeds: Int = 10): List<FeedRefreshResult> = withContext(Dispatchers.IO) {
        // Get favorite feeds first
        val favoriteFeeds = feedDao.getFavoriteFeedsForRefresh()

        // Get oldest non-favorite feeds to fill up to maxFeeds
        val remainingSlots = maxFeeds - favoriteFeeds.size
        val oldestFeeds = if (remainingSlots > 0) {
            feedDao.getOldestNonFavoriteFeeds(remainingSlots)
        } else {
            emptyList()
        }

        val feedsToRefresh = (favoriteFeeds + oldestFeeds).take(maxFeeds)

        if (feedsToRefresh.isEmpty()) {
            return@withContext emptyList()
        }

        val startTime = System.currentTimeMillis()
        val results = mutableListOf<FeedRefreshResult>()
        val resultsLock = Any()

        // Use lower concurrency for background to be more conservative
        coroutineScope {
            val semaphore = kotlinx.coroutines.sync.Semaphore(5)

            feedsToRefresh.map { feed ->
                async {
                    semaphore.withPermit {
                        val result = refreshFeed(feed)
                        synchronized(resultsLock) {
                            results.add(result)
                        }
                        result
                    }
                }
            }.awaitAll()
        }

        val duration = (System.currentTimeMillis() - startTime) / 1000.0
        val successCount = results.count { it.success }
        Log.d(TAG, "✅ Background refresh completed in ${String.format("%.2f", duration)}s: $successCount/${results.size} feeds successful")

        results
    }
}
