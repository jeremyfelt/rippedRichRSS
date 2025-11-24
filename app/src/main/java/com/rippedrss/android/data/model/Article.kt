package com.rippedrss.android.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "articles")
data class Article(
    @PrimaryKey
    val uniqueId: String = UUID.randomUUID().toString(),
    val guid: String? = null,
    val title: String,
    val summary: String,
    val content: String? = null,
    val link: String? = null,
    val author: String? = null,
    val pubDate: Long,
    val feedTitle: String,
    val feedId: String,
    val imageUrl: String? = null,
    val isRead: Boolean = false,
    val isSaved: Boolean = false
)
