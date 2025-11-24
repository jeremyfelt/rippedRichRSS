package com.rippedrss.android.util

import android.content.Context
import android.util.Log
import com.rippedrss.android.ui.theme.TextScale
import com.rippedrss.android.ui.theme.ThemeMode
import java.io.File

/**
 * File-based cache for rendered article HTML content.
 * Caches are keyed by article ID, theme mode, and text scale to ensure
 * proper invalidation when display settings change.
 */
class ArticleHtmlCache private constructor(private val context: Context) {

    companion object {
        private const val TAG = "ArticleHtmlCache"
        private const val CACHE_DIR_NAME = "article_html_cache"
        private const val MAX_CACHE_SIZE_MB = 50L
        private const val MAX_CACHE_AGE_DAYS = 7L

        @Volatile
        private var INSTANCE: ArticleHtmlCache? = null

        fun getInstance(context: Context): ArticleHtmlCache {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ArticleHtmlCache(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    private val cacheDir: File by lazy {
        File(context.cacheDir, CACHE_DIR_NAME).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    /**
     * Generates a cache key for the given article and display settings.
     */
    private fun generateCacheKey(
        articleId: String,
        themeMode: ThemeMode,
        textScale: TextScale
    ): String {
        // Sanitize article ID to be filesystem-safe
        val safeArticleId = articleId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        return "${safeArticleId}_${themeMode.value}_${textScale.value}.html"
    }

    /**
     * Retrieves cached HTML for an article, if available.
     *
     * @param articleId The unique identifier of the article
     * @param themeMode Current theme mode
     * @param textScale Current text scale setting
     * @return Cached HTML content, or null if not cached or stale
     */
    fun getCachedHtml(
        articleId: String,
        themeMode: ThemeMode,
        textScale: TextScale
    ): String? {
        return try {
            val cacheKey = generateCacheKey(articleId, themeMode, textScale)
            val cacheFile = File(cacheDir, cacheKey)

            if (!cacheFile.exists()) {
                return null
            }

            // Check if cache is too old
            val ageMillis = System.currentTimeMillis() - cacheFile.lastModified()
            val maxAgeMillis = MAX_CACHE_AGE_DAYS * 24 * 60 * 60 * 1000
            if (ageMillis > maxAgeMillis) {
                cacheFile.delete()
                return null
            }

            cacheFile.readText()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read cached HTML for article $articleId: ${e.message}")
            null
        }
    }

    /**
     * Caches HTML content for an article.
     *
     * @param articleId The unique identifier of the article
     * @param html The rendered HTML content
     * @param themeMode Current theme mode
     * @param textScale Current text scale setting
     */
    fun cacheHtml(
        articleId: String,
        html: String,
        themeMode: ThemeMode,
        textScale: TextScale
    ) {
        try {
            val cacheKey = generateCacheKey(articleId, themeMode, textScale)
            val cacheFile = File(cacheDir, cacheKey)
            cacheFile.writeText(html)

            // Cleanup old caches if needed (async would be better but keep it simple)
            cleanupIfNeeded()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to cache HTML for article $articleId: ${e.message}")
            // Caching is optional, so we don't throw
        }
    }

    /**
     * Clears cache for a specific article (all variations).
     */
    fun clearCacheForArticle(articleId: String) {
        try {
            val safeArticleId = articleId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            cacheDir.listFiles()?.filter { it.name.startsWith(safeArticleId) }?.forEach { it.delete() }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to clear cache for article $articleId: ${e.message}")
        }
    }

    /**
     * Clears all cached HTML files.
     *
     * @return Number of files deleted
     */
    fun clearAll(): Int {
        return try {
            val files = cacheDir.listFiles() ?: return 0
            var count = 0
            files.forEach {
                if (it.delete()) count++
            }
            Log.i(TAG, "Cleared $count cached HTML files")
            count
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear cache: ${e.message}")
            0
        }
    }

    /**
     * Gets the current cache size in bytes.
     */
    fun getCacheSizeBytes(): Long {
        return try {
            cacheDir.listFiles()?.sumOf { it.length() } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Cleans up old cache files if the cache is too large.
     */
    private fun cleanupIfNeeded() {
        try {
            val maxSizeBytes = MAX_CACHE_SIZE_MB * 1024 * 1024
            val currentSize = getCacheSizeBytes()

            if (currentSize > maxSizeBytes) {
                // Delete oldest files until we're under the limit
                val files = cacheDir.listFiles()?.sortedBy { it.lastModified() } ?: return
                var freedBytes = 0L
                val targetFree = currentSize - (maxSizeBytes * 0.8).toLong() // Free 20% extra

                for (file in files) {
                    if (freedBytes >= targetFree) break
                    val fileSize = file.length()
                    if (file.delete()) {
                        freedBytes += fileSize
                    }
                }

                Log.i(TAG, "Cache cleanup: freed ${freedBytes / 1024}KB")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Cache cleanup failed: ${e.message}")
        }
    }
}
