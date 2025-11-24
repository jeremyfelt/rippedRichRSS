package com.rippedrss.android.util

/**
 * Sanitizes HTML content to prevent XSS and CSS injection attacks.
 * Removes dangerous elements while preserving safe content including iframes for embedded media.
 */
object HtmlSanitizer {

    // Tags that should be completely removed along with their content
    private val DANGEROUS_TAGS_WITH_CONTENT = listOf("script", "style", "noscript")

    // Attributes that could be dangerous (event handlers, javascript: URLs)
    private val DANGEROUS_ATTRIBUTES = listOf(
        "onclick", "onload", "onerror", "onmouseover", "onmouseout", "onmousedown",
        "onmouseup", "onkeydown", "onkeyup", "onkeypress", "onfocus", "onblur",
        "onchange", "onsubmit", "onreset", "onselect", "ondblclick", "oncontextmenu"
    )

    /**
     * Sanitizes HTML content by removing dangerous elements and attributes.
     *
     * @param html The raw HTML content to sanitize
     * @return Sanitized HTML safe for WebView rendering
     */
    fun sanitize(html: String?): String {
        if (html.isNullOrBlank()) return ""

        var result: String = html

        // Remove script tags and their content
        result = removeTagWithContent(result, "script")

        // Remove style tags and their content
        result = removeTagWithContent(result, "style")

        // Remove noscript tags and their content
        result = removeTagWithContent(result, "noscript")

        // Remove dangerous event handler attributes
        for (attr in DANGEROUS_ATTRIBUTES) {
            result = removeAttribute(result, attr)
        }

        // Remove javascript: URLs in href and src attributes
        result = removeJavaScriptUrls(result)

        // Remove data: URLs that could contain scripts (but allow images)
        result = removeDangerousDataUrls(result)

        return result
    }

    /**
     * Removes a tag and all its content from the HTML.
     */
    private fun removeTagWithContent(html: String, tagName: String): String {
        // Handle both self-closing and regular tags
        val pattern = Regex(
            """<$tagName[^>]*>[\s\S]*?</$tagName>|<$tagName[^>]*/>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE)
        )
        return html.replace(pattern, "")
    }

    /**
     * Removes a specific attribute from all tags.
     */
    private fun removeAttribute(html: String, attrName: String): String {
        // Match attribute with single quotes, double quotes, or no quotes
        val pattern = Regex(
            """\s+$attrName\s*=\s*(?:"[^"]*"|'[^']*'|[^\s>]+)""",
            RegexOption.IGNORE_CASE
        )
        return html.replace(pattern, "")
    }

    /**
     * Removes javascript: URLs from href and src attributes.
     */
    private fun removeJavaScriptUrls(html: String): String {
        val pattern = Regex(
            """(href|src)\s*=\s*["']?\s*javascript:[^"'>\s]*["']?""",
            RegexOption.IGNORE_CASE
        )
        return html.replace(pattern, """$1="#" """)
    }

    /**
     * Removes dangerous data: URLs (keeps image data URLs).
     */
    private fun removeDangerousDataUrls(html: String): String {
        // Remove data URLs that are not images
        val pattern = Regex(
            """(href|src)\s*=\s*["']data:(?!image/)[^"']*["']""",
            RegexOption.IGNORE_CASE
        )
        return html.replace(pattern, """$1="#" """)
    }

    /**
     * Strips all HTML tags, returning plain text.
     * Useful for summaries and previews.
     */
    fun stripAllTags(html: String?): String {
        if (html.isNullOrBlank()) return ""

        return html
            .replace(Regex("<[^>]*>"), "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&#x27;", "'")
            .replace("&#x2F;", "/")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
