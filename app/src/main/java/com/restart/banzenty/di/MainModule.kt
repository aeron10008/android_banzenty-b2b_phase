package com.mywork.templateapp.di

import android.app.Application
import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.restart.banzenty.R
import com.restart.banzenty.presistence.AppDBDao
import com.restart.banzenty.utils.SessionManager
import com.restart.banzenty.utils.PreferenceKeys
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

//Think of the module as the “bag” from where we will get our dependencies from.

@Module
@InstallIn(SingletonComponent::class)
object MainModule {
    //Session Manager
    @Singleton
    @Provides
    fun providesSessionManager(
        @ApplicationContext context: Context,
        appDBDao: AppDBDao
    ): SessionManager {
        return SessionManager(
            context,
            appDBDao,
            context.getSharedPreferences(
                PreferenceKeys.APP_PREFERENCES,
                Context.MODE_PRIVATE
            )
        )
    }

    //Glide
    @Singleton
    @Provides
    fun provideRequestOptions(): RequestOptions {
        return RequestOptions
            .placeholderOf(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_foreground)
    }

    @Singleton
    @Provides
    fun provideGlideInstance(
        application: Application,
        requestOptions: RequestOptions
    ): RequestManager {
        return Glide.with(application)
            .setDefaultRequestOptions(requestOptions)
    }

}