package com.oliva.verde.android.divercitynewsapp.service.repository.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.oliva.verde.android.divercitynewsapp.service.model.Article
import com.oliva.verde.android.divercitynewsapp.service.model.StockArticle

@Database(entities = [StockArticle::class], version = 1)
abstract class Database : RoomDatabase() {
    abstract fun stockArticleDao() : StockArticleDao
}