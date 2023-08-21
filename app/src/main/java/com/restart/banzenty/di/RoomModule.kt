package com.mywork.templateapp.di

import android.content.Context
import androidx.room.Room
import com.restart.banzenty.presistence.AppDatabase
import com.restart.banzenty.presistence.AppDBDao
import com.restart.banzenty.utils.Constants.Companion.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

//Think of the module as the “bag” from where we will get our dependencies from.

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    //Database
    @Singleton
    @Provides
    fun provideAppDb(@ApplicationContext context: Context): AppDatabase {
        return Room
            .databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigration() // get correct db version if schema changed
            .build()
    }

    @Singleton
    @Provides
    fun provideAppDBDao(db: AppDatabase): AppDBDao {
        return db.getAppDBDao()
    }
}