package com.rippedrss.android.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "feeds")
data class Feed(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val feedUrl: String,
    val siteUrl: String? = null,
    val feedSummary: String? = null,
    val lastUpdated: Long? = null,
    val faviconUrl: String? = null,
    val isFavorite: Boolean = false,
    val lastRefreshError: String? = null
) {
    // Transient property not stored in database
    @Transient
    var isRefreshing: Boolean = false
}
