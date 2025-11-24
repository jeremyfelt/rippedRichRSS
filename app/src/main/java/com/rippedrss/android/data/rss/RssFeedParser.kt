package com.rippedrss.android.data.rss

import android.util.Xml
import com.rippedrss.android.data.model.Article
import com.rippedrss.android.util.HtmlSanitizer
import com.rippedrss.android.util.ThreadSafeDateParser
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

data class ParsedFeed(
    val title: String,
    val siteUrl: String?,
    val description: String?,
    val articles: List<Article>
)

class RssFeedParser {

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream, feedId: String, feedUrl: String): ParsedFeed {
        inputStream.use {
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)
            parser.nextTag()
            return readFeed(parser, feedId, feedUrl)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readFeed(parser: XmlPullParser, feedId: String, feedUrl: String): ParsedFeed {
        var feedTitle = ""
        var feedLink: String? = null
        var feedDescription: String? = null
        val articles = mutableListOf<Article>()

        parser.require(XmlPullParser.START_TAG, null, null)
        val rootTag = parser.name

        when (rootTag) {
            "rss" -> {
                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    if (parser.eventType != XmlPullParser.START_TAG) continue

                    if (parser.name == "channel") {
                        while (parser.next() != XmlPullParser.END_TAG || parser.name != "channel") {
                            if (parser.eventType != XmlPullParser.START_TAG) continue

                            when (parser.name) {
                                "title" -> if (feedTitle.isEmpty()) feedTitle = readText(parser, "title")
                                "link" -> if (feedLink == null) feedLink = readText(parser, "link")
                                "description" -> if (feedDescription == null) feedDescription = readText(parser, "description")
                                "item" -> articles.add(readRssItem(parser, feedId, feedTitle, feedUrl))
                                else -> skip(parser)
                            }
                        }
                    }
                }
            }
            "feed" -> {
                // Atom feed
                while (parser.next() != XmlPullParser.END_TAG || parser.name != "feed") {
                    if (parser.eventType != XmlPullParser.START_TAG) continue

                    when (parser.name) {
                        "title" -> if (feedTitle.isEmpty()) feedTitle = readText(parser, "title")
                        "link" -> if (feedLink == null) feedLink = readAtomLink(parser)
                        "subtitle" -> if (feedDescription == null) feedDescription = readText(parser, "subtitle")
                        "entry" -> articles.add(readAtomEntry(parser, feedId, feedTitle, feedUrl))
                        else -> skip(parser)
                    }
                }
            }
            else -> throw XmlPullParserException("Unknown feed format: $rootTag")
        }

        return ParsedFeed(
            title = feedTitle.ifEmpty { "Untitled Feed" },
            siteUrl = feedLink,
            description = feedDescription,
            articles = articles
        )
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readRssItem(parser: XmlPullParser, feedId: String, feedTitle: String, feedUrl: String): Article {
        parser.require(XmlPullParser.START_TAG, null, "item")

        var title = ""
        var link: String? = null
        var description = ""
        var content: String? = null
        var author: String? = null
        var pubDate: Long = System.currentTimeMillis()
        var guid: String? = null
        var imageUrl: String? = null

        while (parser.next() != XmlPullParser.END_TAG || parser.name != "item") {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            when (parser.name) {
                "title" -> title = readText(parser, "title")
                "link" -> link = readText(parser, "link")
                "description" -> description = readText(parser, "description")
                "content:encoded" -> content = readText(parser, "content:encoded")
                "author" -> author = readText(parser, "author")
                "dc:creator" -> if (author == null) author = readText(parser, "dc:creator")
                "pubDate" -> pubDate = ThreadSafeDateParser.parse(readText(parser, "pubDate"))
                "guid" -> guid = readText(parser, "guid")
                "media:content" -> imageUrl = parser.getAttributeValue(null, "url")
                "enclosure" -> {
                    val type = parser.getAttributeValue(null, "type")
                    if (type?.startsWith("image/") == true) {
                        imageUrl = parser.getAttributeValue(null, "url")
                    }
                    skip(parser)
                }
                else -> skip(parser)
            }
        }

        return Article(
            guid = guid ?: link,
            title = HtmlSanitizer.stripAllTags(title).ifEmpty { "Untitled" },
            summary = HtmlSanitizer.stripAllTags(description),
            content = content,
            link = link,
            author = author,
            pubDate = pubDate,
            feedTitle = feedTitle,
            feedId = feedId,
            imageUrl = imageUrl
        )
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readAtomEntry(parser: XmlPullParser, feedId: String, feedTitle: String, feedUrl: String): Article {
        parser.require(XmlPullParser.START_TAG, null, "entry")

        var title = ""
        var link: String? = null
        var summary = ""
        var content: String? = null
        var author: String? = null
        var pubDate: Long = System.currentTimeMillis()
        var id: String? = null

        while (parser.next() != XmlPullParser.END_TAG || parser.name != "entry") {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            when (parser.name) {
                "title" -> title = readText(parser, "title")
                "link" -> {
                    val rel = parser.getAttributeValue(null, "rel")
                    if (rel == null || rel == "alternate") {
                        link = parser.getAttributeValue(null, "href")
                    }
                    skip(parser)
                }
                "summary" -> summary = readText(parser, "summary")
                "content" -> content = readText(parser, "content")
                "author" -> author = readAtomAuthor(parser)
                "published" -> pubDate = ThreadSafeDateParser.parse(readText(parser, "published"))
                "updated" -> if (pubDate == System.currentTimeMillis()) pubDate = ThreadSafeDateParser.parse(readText(parser, "updated"))
                "media:thumbnail" -> if (imageUrl == null) imageUrl = parser.getAttributeValue(null, "url")
                "media:content" -> if (imageUrl == null) imageUrl = parser.getAttributeValue(null, "url")
                "id" -> id = readText(parser, "id")
                else -> skip(parser)
            }
        }

        return Article(
            guid = id ?: link,
            title = HtmlSanitizer.stripAllTags(title).ifEmpty { "Untitled" },
            summary = HtmlSanitizer.stripAllTags(summary),
            content = content,
            link = link,
            author = author,
            pubDate = pubDate,
            feedTitle = feedTitle,
            feedId = feedId,
            imageUrl = imageUrl
        )
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser, tagName: String): String {
        parser.require(XmlPullParser.START_TAG, null, tagName)
        var text = ""
        if (parser.next() == XmlPullParser.TEXT) {
            text = parser.text
            parser.nextTag()
        }
        parser.require(XmlPullParser.END_TAG, null, tagName)
        return text
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readAtomLink(parser: XmlPullParser): String? {
        val href = parser.getAttributeValue(null, "href")
        skip(parser)
        return href
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readAtomAuthor(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, null, "author")
        var name = ""

        while (parser.next() != XmlPullParser.END_TAG || parser.name != "author") {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            if (parser.name == "name") {
                name = readText(parser, "name")
            } else {
                skip(parser)
            }
        }

        return name
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}
