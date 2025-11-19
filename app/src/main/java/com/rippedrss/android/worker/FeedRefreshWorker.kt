package com.rippedrss.android.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import com.rippedrss.android.data.AppDatabase
import com.rippedrss.android.data.preferences.AppPreferences
import com.rippedrss.android.data.repository.FeedRepository
import com.rippedrss.android.data.rss.FaviconFetcher
import com.rippedrss.android.data.rss.FeedDiscoverer
import com.rippedrss.android.data.rss.FeedFetcher
import com.rippedrss.android.util.NetworkUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class FeedRefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "FeedRefreshWorker"
        const val WORK_NAME = "feed_refresh_work"

        fun scheduleWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<FeedRefreshWorker>(
                15, TimeUnit.MINUTES,
                5, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )

            Log.d(TAG, "Background refresh work scheduled")
        }

        fun cancelWork(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "Background refresh work cancelled")
        }
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting background feed refresh")

            val appPreferences = AppPreferences(applicationContext)
            val backgroundRefreshEnabled = appPreferences.backgroundRefreshEnabled.first()

            if (!backgroundRefreshEnabled) {
                Log.d(TAG, "Background refresh is disabled")
                return Result.success()
            }

            val wifiOnly = appPreferences.wifiOnly.first()

            if (!NetworkUtils.canRefresh(applicationContext, wifiOnly)) {
                Log.d(TAG, "Cannot refresh: network conditions not met (Wi-Fi only: $wifiOnly)")
                return Result.retry()
            }

            // Initialize dependencies
            val database = AppDatabase.getDatabase(applicationContext)
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            val feedFetcher = FeedFetcher(
                database.feedDao(),
                database.articleDao(),
                okHttpClient
            )

            val repository = FeedRepository(
                database.feedDao(),
                database.articleDao(),
                feedFetcher,
                FeedDiscoverer(okHttpClient),
                FaviconFetcher(okHttpClient)
            )

            // Refresh feeds
            val results = repository.refreshFeedsForBackground(maxFeeds = 10)
            val successCount = results.count { it.success }

            Log.d(TAG, "Background refresh completed: $successCount/${results.size} feeds successful")

            // Update last refresh time
            appPreferences.setLastRefreshTime(System.currentTimeMillis())

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Background refresh failed: ${e.message}", e)
            Result.retry()
        }
    }
}
