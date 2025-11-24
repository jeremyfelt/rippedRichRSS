package com.rippedrss.android.data.rss

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParserFactory

data class DiscoveredFeed(
    val feedUrl: String,
    val title: String,
    val siteUrl: String?
)

class FeedDiscoverer(private val okHttpClient: OkHttpClient) {

    companion object {
        private const val TAG = "FeedDiscoverer"
    }

    suspend fun discoverFeed(url: String): DiscoveredFeed? {
        return try {
            // First, try the URL directly as a feed
            if (isValidFeed(url)) {
                val feedInfo = getFeedInfo(url)
                if (feedInfo != null) {
                    return feedInfo
                }
            }

            // If not a direct feed, try to find feed links in the HTML
            val feedUrl = findFeedInHtml(url)
            if (feedUrl != null) {
                val feedInfo = getFeedInfo(feedUrl)
                if (feedInfo != null) {
                    return feedInfo.copy(siteUrl = url)
                }
            }

            null
        } catch (e: Exception) {
            Log.e(TAG, "Error discovering feed: ${e.message}", e)
            null
        }
    }

    private fun isValidFeed(url: String): Boolean {
        return try {
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                return false
            }

            val contentType = response.header("Content-Type") ?: ""
            contentType.contains("xml") || contentType.contains("rss") || contentType.contains("atom")
        } catch (e: Exception) {
            false
        }
    }

    private fun getFeedInfo(feedUrl: String): DiscoveredFeed? {
        return try {
            val request = Request.Builder().url(feedUrl).build()
            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                return null
            }

            response.body?.byteStream()?.use { inputStream ->
                val factory = XmlPullParserFactory.newInstance()
                factory.isNamespaceAware = false
                val parser = factory.newPullParser()
                parser.setInput(inputStream, null)

                var title: String? = null
                var link: String? = null
                var inChannel = false
                var foundTitle = false

                while (parser.eventType != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                    when (parser.eventType) {
                        org.xmlpull.v1.XmlPullParser.START_TAG -> {
                            when (parser.name) {
                                "channel", "feed" -> inChannel = true
                                "title" -> {
                                    if (inChannel && !foundTitle) {
                                        parser.next()
                                        if (parser.eventType == org.xmlpull.v1.XmlPullParser.TEXT) {
                                            title = parser.text
                                            foundTitle = true
                                        }
                                    }
                                }
                                "link" -> {
                                    if (inChannel && link == null) {
                                        val href = parser.getAttributeValue(null, "href")
                                        if (href != null) {
                                            link = href
                                        } else {
                                            parser.next()
                                            if (parser.eventType == org.xmlpull.v1.XmlPullParser.TEXT) {
                                                link = parser.text
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    parser.next()

                    // Stop early if we have everything we need
                    if (title != null && link != null) {
                        break
                    }
                }

                if (title != null) {
                    DiscoveredFeed(feedUrl, title, link)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting feed info: ${e.message}", e)
            null
        }
    }

    private fun findFeedInHtml(url: String): String? {
        return try {
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                return null
            }

            val html = response.body?.string() ?: return null

            // Look for RSS/Atom feed links in the HTML
            val linkPattern = Regex("""<link[^>]*type=["']application/(rss|atom)\+xml["'][^>]*>""", RegexOption.IGNORE_CASE)
            val matches = linkPattern.findAll(html)

            for (match in matches) {
                val linkTag = match.value
                val hrefPattern = Regex("""href=["']([^"']+)["']""")
                val hrefMatch = hrefPattern.find(linkTag)

                if (hrefMatch != null) {
                    var feedUrl = hrefMatch.groupValues[1]

                    // Handle relative URLs
                    if (feedUrl.startsWith("/")) {
                        val baseUrl = url.substringBefore("://") + "://" + url.substringAfter("://").substringBefore("/")
                        feedUrl = baseUrl + feedUrl
                    } else if (!feedUrl.startsWith("http")) {
                        feedUrl = url.substringBeforeLast("/") + "/" + feedUrl
                    }

                    return feedUrl
                }
            }

            null
        } catch (e: Exception) {
            Log.e(TAG, "Error finding feed in HTML: ${e.message}", e)
            null
        }
    }
}
