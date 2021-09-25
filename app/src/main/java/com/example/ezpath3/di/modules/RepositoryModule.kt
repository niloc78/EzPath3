package com.example.ezpath3.di.modules

import android.content.Context
import com.example.ezpath3.repository.PreferencesManagerImpl
import com.example.ezpath3.repository.Repository
import com.example.ezpath3.retrofit.GoogleApiService
import com.example.ezpath3.retrofit.NetworkDirectionsResultsMapper
import com.example.ezpath3.retrofit.NetworkErrandResultsMapper
import com.example.ezpath3.util.PreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideSharedPrefsManager(@ApplicationContext context : Context) : PreferencesManager {
        return PreferencesManagerImpl(context.getSharedPreferences(context.packageName + "_preferences", Context.MODE_PRIVATE))
    }

    @Singleton
    @Provides
    fun provideRepository(retrofit : GoogleApiService, networkErrandResultsMapper: NetworkErrandResultsMapper, networkDirectionsResultsMapper: NetworkDirectionsResultsMapper, prefsManager : PreferencesManager) : Repository {
        return Repository(retrofit, networkErrandResultsMapper, networkDirectionsResultsMapper, prefsManager)
    }




}