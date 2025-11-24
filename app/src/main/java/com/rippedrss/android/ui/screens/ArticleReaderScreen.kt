package com.rippedrss.android.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
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
import com.rippedrss.android.util.RelativeTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleReaderScreen(
    article: Article,
    onBack: () -> Unit,
    onToggleSaved: () -> Unit,
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
            isDarkMode = isDarkMode,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

@Composable
private fun ArticleWebView(
    article: Article,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val htmlContent = remember(article, isDarkMode) {
        buildHtmlContent(article, isDarkMode)
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

private fun buildHtmlContent(article: Article, isDarkMode: Boolean): String {
    val backgroundColor = if (isDarkMode) "#1C1B1F" else "#FAFAFA"
    val textColor = if (isDarkMode) "#E6E1E5" else "#1C1B1F"
    val secondaryTextColor = if (isDarkMode) "#B0B0B0" else "#666666"
    val linkColor = if (isDarkMode) "#90CAF9" else "#1976D2"

    val content = article.content ?: article.summary

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                    font-size: 16px;
                    line-height: 1.6;
                    color: $textColor;
                    background-color: $backgroundColor;
                    padding: 16px;
                    margin: 0;
                }
                h1, h2, h3, h4, h5, h6 {
                    margin-top: 24px;
                    margin-bottom: 12px;
                    line-height: 1.3;
                }
                h1 {
                    font-size: 28px;
                    font-weight: 600;
                }
                p {
                    margin-bottom: 16px;
                }
                a {
                    color: $linkColor;
                    text-decoration: none;
                }
                img {
                    max-width: 100%;
                    height: auto;
                    border-radius: 8px;
                    margin: 16px 0;
                }
                pre {
                    background-color: ${if (isDarkMode) "#2C2C2C" else "#F5F5F5"};
                    padding: 12px;
                    border-radius: 8px;
                    overflow-x: auto;
                }
                code {
                    font-family: 'Courier New', monospace;
                    font-size: 14px;
                }
                blockquote {
                    border-left: 4px solid $linkColor;
                    padding-left: 16px;
                    margin-left: 0;
                    color: $secondaryTextColor;
                }
                .article-meta {
                    color: $secondaryTextColor;
                    font-size: 14px;
                    margin-bottom: 24px;
                    padding-bottom: 16px;
                    border-bottom: 1px solid ${if (isDarkMode) "#404040" else "#E0E0E0"};
                }
                .article-title {
                    font-size: 24px;
                    font-weight: 600;
                    margin-bottom: 12px;
                    line-height: 1.3;
                }
            </style>
        </head>
        <body>
            <div class="article-title">${article.title}</div>
            <div class="article-meta">
                ${article.author?.let { "By $it • " } ?: ""}
                ${RelativeTimeFormatter.format(article.pubDate)}
            </div>
            <div class="article-content">
                $content
            </div>
            ${article.link?.let { """<p><a href="$it">Read original article</a></p>""" } ?: ""}
        </body>
        </html>
    """.trimIndent()
}
