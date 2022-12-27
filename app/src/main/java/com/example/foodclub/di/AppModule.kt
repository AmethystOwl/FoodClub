package com.example.foodclub.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.foodclub.shared.Repository
import com.example.foodclub.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideAuth() = FirebaseAuth.getInstance()

    @Singleton
    @Provides
    fun provideFireStore() = FirebaseFirestore.getInstance()

    @Singleton
    @Provides
    fun provideStorage() = FirebaseStorage.getInstance()

    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    fun provideRepository(
        auth: FirebaseAuth,
        fireStore: FirebaseFirestore,
        storage: FirebaseStorage
    ) = Repository(auth, fireStore, storage)

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory
            .create(produceFile = { appContext
                .preferencesDataStoreFile(Constants.DATASTORE_FILENAME) })
}
