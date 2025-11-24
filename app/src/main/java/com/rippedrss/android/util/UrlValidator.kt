package com.rippedrss.android.util

import android.util.Patterns
import java.net.URI
import java.net.URISyntaxException

/**
 * Validates URLs to prevent SSRF and other URL-based attacks.
 */
object UrlValidator {

    // Private/internal IP ranges that should be blocked
    private val PRIVATE_IP_PATTERNS = listOf(
        Regex("""^10\.\d{1,3}\.\d{1,3}\.\d{1,3}$"""),                    // 10.0.0.0/8
        Regex("""^172\.(1[6-9]|2[0-9]|3[0-1])\.\d{1,3}\.\d{1,3}$"""),   // 172.16.0.0/12
        Regex("""^192\.168\.\d{1,3}\.\d{1,3}$"""),                       // 192.168.0.0/16
        Regex("""^127\.\d{1,3}\.\d{1,3}\.\d{1,3}$"""),                   // 127.0.0.0/8 (loopback)
        Regex("""^169\.254\.\d{1,3}\.\d{1,3}$"""),                       // 169.254.0.0/16 (link-local)
        Regex("""^0\.0\.0\.0$"""),                                        // 0.0.0.0
        Regex("""^::1$"""),                                               // IPv6 loopback
        Regex("""^fc00:""", RegexOption.IGNORE_CASE),                     // IPv6 private
        Regex("""^fe80:""", RegexOption.IGNORE_CASE)                      // IPv6 link-local
    )

    // Blocked hostnames
    private val BLOCKED_HOSTNAMES = setOf(
        "localhost",
        "localhost.localdomain",
        "local",
        "broadcasthost",
        "ip6-localhost",
        "ip6-loopback"
    )

    // Allowed schemes
    private val ALLOWED_SCHEMES = setOf("http", "https")

    /**
     * Validates a URL for safe network requests.
     *
     * @param url The URL to validate
     * @return ValidationResult indicating if the URL is safe
     */
    fun validate(url: String?): ValidationResult {
        if (url.isNullOrBlank()) {
            return ValidationResult.Invalid("URL is empty")
        }

        val trimmedUrl = url.trim()

        // Basic URL pattern check
        if (!Patterns.WEB_URL.matcher(trimmedUrl).matches() &&
            !trimmedUrl.startsWith("http://") &&
            !trimmedUrl.startsWith("https://")) {
            return ValidationResult.Invalid("Invalid URL format")
        }

        // Parse the URL
        val uri = try {
            URI(trimmedUrl)
        } catch (e: URISyntaxException) {
            return ValidationResult.Invalid("Malformed URL: ${e.message}")
        }

        // Check scheme
        val scheme = uri.scheme?.lowercase()
        if (scheme == null) {
            return ValidationResult.Invalid("Missing URL scheme")
        }
        if (scheme !in ALLOWED_SCHEMES) {
            return ValidationResult.Invalid("Blocked URL scheme: $scheme")
        }

        // Check host
        val host = uri.host?.lowercase()
        if (host.isNullOrBlank()) {
            return ValidationResult.Invalid("Missing host in URL")
        }

        // Check for blocked hostnames
        if (host in BLOCKED_HOSTNAMES) {
            return ValidationResult.Invalid("Blocked hostname: $host")
        }

        // Check for private/internal IPs
        if (isPrivateIp(host)) {
            return ValidationResult.Invalid("Private/internal IP addresses are not allowed")
        }

        // Check for IP address in hostname (could be used to bypass hostname checks)
        if (isIpAddress(host) && isPrivateIp(host)) {
            return ValidationResult.Invalid("Private IP addresses are not allowed")
        }

        return ValidationResult.Valid(trimmedUrl)
    }

    /**
     * Checks if a string is a private/internal IP address.
     */
    private fun isPrivateIp(host: String): Boolean {
        return PRIVATE_IP_PATTERNS.any { it.matches(host) }
    }

    /**
     * Checks if a string looks like an IP address.
     */
    private fun isIpAddress(host: String): Boolean {
        return Patterns.IP_ADDRESS.matcher(host).matches() ||
               host.contains(":") // IPv6
    }

    /**
     * Normalizes a URL by ensuring it has a scheme.
     *
     * @param url The URL to normalize
     * @return The normalized URL with https:// prefix if missing
     */
    fun normalizeUrl(url: String): String {
        val trimmed = url.trim()
        return when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.startsWith("//") -> "https:$trimmed"
            else -> "https://$trimmed"
        }
    }

    sealed class ValidationResult {
        data class Valid(val url: String) : ValidationResult()
        data class Invalid(val reason: String) : ValidationResult()

        fun isValid(): Boolean = this is Valid

        fun getUrlOrNull(): String? = (this as? Valid)?.url
    }
}
