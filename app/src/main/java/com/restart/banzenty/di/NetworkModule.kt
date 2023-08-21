package com.restart.banzenty.di

import android.content.Context
import com.restart.banzenty.utils.SessionManager
import com.restart.banzenty.network.ApiInterface
import com.restart.banzenty.network.LiveDataCallAdapterFactory
import com.restart.banzenty.utils.Constants.Companion.BASE_URL
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

//Think of the module as the “bag” from where we will get our dependencies from.

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    //Retrofit
    @Singleton
    @Provides
    fun provideGsonBuilder(): Gson {
        return GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
    }

    @Singleton
    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        return logging
    }

    @Singleton
    @Provides
    fun provideHttpClientBuilder(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        sessionManager: SessionManager,
        @ApplicationContext appContext: Context
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(httpLoggingInterceptor)
            .addNetworkInterceptor(interceptor = { chain ->
                val requestBuilder: Request.Builder = chain.request().newBuilder()
                requestBuilder.header("Accept", "application/json")
                requestBuilder.header("Accept-Language", sessionManager.getLanguage())
                if (sessionManager.getToken().isNotEmpty())
                    requestBuilder.addHeader("authorization", "Bearer ${sessionManager.getToken()}")

                chain.proceed(requestBuilder.build())
            })
            .build()
    }
 
    @Singleton
    @Provides
    fun provideRetrofitBuilder(gson: Gson, httpClient: OkHttpClient): Retrofit.Builder {
        return Retrofit.Builder().baseUrl(BASE_URL)
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
    }

    @Singleton
    @Provides
    fun provideApiInterfaceService(retrofitBuilder: Retrofit.Builder): ApiInterface {
        return retrofitBuilder
            .build()
            .create(ApiInterface::class.java)
    }
}