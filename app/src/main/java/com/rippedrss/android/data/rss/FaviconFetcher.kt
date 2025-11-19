package com.rippedrss.android.data.rss

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request

class FaviconFetcher(private val okHttpClient: OkHttpClient) {

    companion object {
        private const val TAG = "FaviconFetcher"
    }

    fun getFaviconUrl(siteUrl: String?): String? {
        if (siteUrl.isNullOrEmpty()) return null

        return try {
            // Extract base URL
            val baseUrl = if (siteUrl.startsWith("http")) {
                siteUrl.substringBefore("://") + "://" + siteUrl.substringAfter("://").substringBefore("/")
            } else {
                "https://$siteUrl"
            }

            // Try common favicon locations
            val possibleUrls = listOf(
                "$baseUrl/favicon.ico",
                "$baseUrl/favicon.png",
                "$baseUrl/apple-touch-icon.png"
            )

            for (url in possibleUrls) {
                if (urlExists(url)) {
                    return url
                }
            }

            // Fallback to Google's favicon service
            val domain = baseUrl.substringAfter("://")
            "https://www.google.com/s2/favicons?domain=$domain&sz=64"
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching favicon: ${e.message}", e)
            null
        }
    }

    private fun urlExists(url: String): Boolean {
        return try {
            val request = Request.Builder()
                .url(url)
                .head()
                .build()

            val response = okHttpClient.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
