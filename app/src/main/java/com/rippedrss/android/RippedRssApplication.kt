package com.rippedrss.android

import android.app.Application
import com.rippedrss.android.data.AppDatabase
import com.rippedrss.android.data.preferences.AppPreferences
import com.rippedrss.android.data.repository.ArticleRepository
import com.rippedrss.android.data.repository.FeedRepository
import com.rippedrss.android.data.rss.FaviconFetcher
import com.rippedrss.android.data.rss.FeedDiscoverer
import com.rippedrss.android.data.rss.FeedFetcher
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class RippedRssApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var appPreferences: AppPreferences
        private set

    lateinit var okHttpClient: OkHttpClient
        private set

    lateinit var feedRepository: FeedRepository
        private set

    lateinit var articleRepository: ArticleRepository
        private set

    override fun onCreate() {
        super.onCreate()

        // Initialize database
        database = AppDatabase.getDatabase(this)

        // Initialize preferences
        appPreferences = AppPreferences(this)

        // Initialize OkHttp client
        okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        // Initialize repositories
        val feedFetcher = FeedFetcher(
            database.feedDao(),
            database.articleDao(),
            okHttpClient
        )

        val feedDiscoverer = FeedDiscoverer(okHttpClient)
        val faviconFetcher = FaviconFetcher(okHttpClient)

        feedRepository = FeedRepository(
            database.feedDao(),
            database.articleDao(),
            feedFetcher,
            feedDiscoverer,
            faviconFetcher
        )

        articleRepository = ArticleRepository(database.articleDao())
    }
}
