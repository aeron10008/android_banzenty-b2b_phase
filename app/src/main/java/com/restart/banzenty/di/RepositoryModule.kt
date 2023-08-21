package com.restart.banzenty.di

import com.restart.banzenty.utils.SessionManager
import com.restart.banzenty.network.ApiInterface
import com.restart.banzenty.presistence.AppDBDao
import com.restart.banzenty.repositories.AuthRepository
import com.restart.banzenty.repositories.ProfileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

//Think of the module as the “bag” from where we will get our dependencies from.

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    //Auth Repos
    @Singleton
    @Provides
    fun providesAuthRepository(
        appDBDao: AppDBDao,
        apiInterface: ApiInterface,
        sessionManager: SessionManager
    ): AuthRepository {
        return AuthRepository(appDBDao, apiInterface, sessionManager)
    }

    //Profile Repos
    @Singleton
    @Provides
    fun providesProfileRepository(
        appDBDao: AppDBDao,
        apiInterface: ApiInterface,
        sessionManager: SessionManager
    ): ProfileRepository {
        return ProfileRepository(appDBDao, apiInterface, sessionManager)
    }
}