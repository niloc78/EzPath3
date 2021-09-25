package com.example.ezpath3.di.modules


import android.content.Context
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import com.example.ezpath3.R

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

import dagger.hilt.android.qualifiers.ActivityContext

import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Qualifier


@Module
@InstallIn(ActivityComponent::class)
object UiModule {

    @ActivityScoped
    @Provides
    @searchBarTransitionSet
    fun provideSearchBarTransitionSet(@ActivityContext context : Context) : Transition {
        return TransitionInflater.from(context).inflateTransition(R.transition.animate)
            .setDuration(200L)
    }

    @ActivityScoped
    @Provides
    @toggleFragTransitionSet
    fun provideToggleFragTransitionSet(@ActivityContext context : Context) : Transition {
        return TransitionInflater.from(context).inflateTransition(R.transition.animate)
                .setDuration(200L)
    }
    @ActivityScoped
    @Provides
    @movingBackgroundTransitionSet
    fun provideMovingBackgroundTransitionSet(@ActivityContext context : Context) : Transition {
        return TransitionInflater.from(context).inflateTransition(R.transition.animate)
                .setDuration(200L)
    }


}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class searchBarTransitionSet

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class toggleFragTransitionSet


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class movingBackgroundTransitionSet


