package com.rippedrss.android.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.rippedrss.android.data.model.Article
import com.rippedrss.android.ui.theme.ArticleThemeColors
import com.rippedrss.android.ui.theme.TextScale
import com.rippedrss.android.ui.theme.ThemeMode
import com.rippedrss.android.util.ArticleHtmlCache
import com.rippedrss.android.util.HtmlSanitizer
import com.rippedrss.android.util.RelativeTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleReaderScreen(
    article: Article,
    onBack: () -> Unit,
    onToggleSaved: () -> Unit,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    textScale: TextScale = TextScale.DEFAULT,
    // Legacy parameter for backward compatibility
    isDarkMode: Boolean = false
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                        Text(
                            text = article.feedTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onToggleSaved) {
                        Icon(
                            imageVector = if (article.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (article.isSaved) "Remove from saved" else "Save"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        ArticleWebView(
            article = article,
            themeMode = themeMode,
            textScale = textScale,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

@Composable
private fun ArticleWebView(
    article: Article,
    themeMode: ThemeMode,
    textScale: TextScale,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isSystemDark = isSystemInDarkTheme()
    val cache = remember { ArticleHtmlCache.getInstance(context) }

    val htmlContent = remember(article.uniqueId, themeMode, textScale, isSystemDark) {
        // Try to get cached HTML first
        cache.getCachedHtml(article.uniqueId, themeMode, textScale)
            ?: buildHtmlContent(article, themeMode, textScale, isSystemDark).also { html ->
                // Cache the generated HTML
                cache.cacheHtml(article.uniqueId, html, themeMode, textScale)
            }
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                webViewClient = WebViewClient()
                settings.apply {
                    javaScriptEnabled = false
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    builtInZoomControls = false
                }
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(
                null,
                htmlContent,
                "text/html",
                "UTF-8",
                null
            )
        },
        modifier = modifier
    )
}

private fun buildHtmlContent(
    article: Article,
    themeMode: ThemeMode,
    textScale: TextScale,
    isSystemDark: Boolean
): String {
    val colors = ArticleThemeColors.forThemeMode(themeMode, isSystemDark)
    val scale = textScale.scaleFactor

    // Sanitize the content to remove dangerous elements
    val rawContent = article.content ?: article.summary
    val sanitizedContent = HtmlSanitizer.sanitize(rawContent)

    // Escape title for safe HTML insertion
    val safeTitle = article.title
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    // Escape author for safe HTML insertion
    val safeAuthor = article.author?.let {
        it.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
    }

    // Calculate scaled font sizes
    val baseFontSize = (16 * scale).toInt()
    val titleFontSize = (24 * scale).toInt()
    val metaFontSize = (14 * scale).toInt()
    val codeFontSize = (14 * scale).toInt()
    val h1FontSize = (28 * scale).toInt()
    val h2FontSize = (24 * scale).toInt()
    val h3FontSize = (20 * scale).toInt()

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=3.0, user-scalable=yes">
            <style>
                * {
                    box-sizing: border-box;
                }
                body {
                    font-family: 'Charter', 'Georgia', 'Times New Roman', serif;
                    font-size: ${baseFontSize}px;
                    line-height: 1.8;
                    color: ${colors.textColor};
                    background-color: ${colors.backgroundColor};
                    padding: 16px;
                    margin: 0;
                    -webkit-text-size-adjust: 100%;
                }
                h1, h2, h3, h4, h5, h6 {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                    margin-top: 24px;
                    margin-bottom: 12px;
                    line-height: 1.3;
                    font-weight: 600;
                }
                h1 {
                    font-size: ${h1FontSize}px;
                }
                h2 {
                    font-size: ${h2FontSize}px;
                    padding-bottom: 8px;
                    border-bottom: 1px solid ${colors.borderColor};
                }
                h3 {
                    font-size: ${h3FontSize}px;
                }
                p {
                    margin: 0 0 1.5em 0;
                }
                a {
                    color: ${colors.linkColor};
                    text-decoration: none;
                }
                a:hover {
                    text-decoration: underline;
                }
                img {
                    max-width: 100%;
                    height: auto;
                    border-radius: 8px;
                    margin: 16px 0;
                    display: block;
                }
                figure {
                    margin: 16px 0;
                    padding: 0;
                }
                figcaption {
                    font-size: ${metaFontSize}px;
                    color: ${colors.secondaryTextColor};
                    text-align: center;
                    margin-top: 8px;
                }
                iframe {
                    max-width: 100%;
                    border-radius: 8px;
                    margin: 16px 0;
                }
                pre {
                    background-color: ${colors.codeBackgroundColor};
                    padding: 12px;
                    border-radius: 8px;
                    overflow-x: auto;
                    margin: 16px 0;
                }
                code {
                    font-family: 'SF Mono', 'Menlo', 'Monaco', 'Courier New', monospace;
                    font-size: ${codeFontSize}px;
                    background-color: ${colors.codeBackgroundColor};
                    padding: 2px 6px;
                    border-radius: 4px;
                }
                pre code {
                    background: none;
                    padding: 0;
                }
                blockquote {
                    border-left: 4px solid ${colors.linkColor};
                    padding-left: 16px;
                    margin: 16px 0;
                    margin-left: 0;
                    color: ${colors.secondaryTextColor};
                    font-style: italic;
                    background-color: ${colors.codeBackgroundColor};
                    padding: 12px 16px;
                    border-radius: 0 8px 8px 0;
                }
                ul, ol {
                    padding-left: 24px;
                    margin: 16px 0;
                }
                li {
                    margin-bottom: 8px;
                }
                hr {
                    border: none;
                    border-top: 1px solid ${colors.borderColor};
                    margin: 24px 0;
                }
                table {
                    width: 100%;
                    border-collapse: collapse;
                    margin: 16px 0;
                }
                th, td {
                    border: 1px solid ${colors.borderColor};
                    padding: 8px 12px;
                    text-align: left;
                }
                th {
                    background-color: ${colors.codeBackgroundColor};
                }
                .article-meta {
                    color: ${colors.secondaryTextColor};
                    font-size: ${metaFontSize}px;
                    margin-bottom: 24px;
                    padding-bottom: 16px;
                    border-bottom: 1px solid ${colors.borderColor};
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                }
                .article-title {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                    font-size: ${titleFontSize}px;
                    font-weight: 600;
                    margin-bottom: 12px;
                    line-height: 1.3;
                }
                .read-more {
                    margin-top: 32px;
                    padding-top: 16px;
                    border-top: 1px solid ${colors.borderColor};
                }
                @media (max-width: 480px) {
                    body {
                        padding: 12px;
                        font-size: ${(baseFontSize * 0.95).toInt()}px;
                    }
                    .article-title {
                        font-size: ${(titleFontSize * 0.9).toInt()}px;
                    }
                }
            </style>
        </head>
        <body>
            <div class="article-title">$safeTitle</div>
            <div class="article-meta">
                ${safeAuthor?.let { "By $it • " } ?: ""}
                ${RelativeTimeFormatter.format(article.pubDate)}
            </div>
            <div class="article-content">
                $sanitizedContent
            </div>
            ${article.link?.let { link ->
                val safeLink = link.replace("\"", "&quot;")
                """<div class="read-more"><a href="$safeLink">Read original article →</a></div>"""
            } ?: ""}
        </body>
        </html>
    """.trimIndent()
}
