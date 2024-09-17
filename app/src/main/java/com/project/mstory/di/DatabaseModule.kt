package com.project.mstory.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import com.mstory.data.mongo.database.ImagesDatabase
import com.mstory.data.mongo.repository.AuthRepository
import com.mstory.data.mongo.repository.AuthRepositoryImpl
import com.project.mstory.util.connectActivity.NetworkConnectivityObserve
import com.project.mstory.util.Constant.IMAGE_DATABASE
import com.project.mstory.util.Constant.WEB_CLIENT_ID
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): ImagesDatabase {
        return Room.databaseBuilder(context = context, klass = ImagesDatabase::class.java, name = IMAGE_DATABASE).build()
    }
    @Provides
    @Singleton
    fun provideFirstDao(database: ImagesDatabase) = database.imageToUploadDao()

    @Provides
    @Singleton
    fun provideSecondDao(database: ImagesDatabase) = database.imageToDeleteDao()

    @Provides
    @Singleton
    fun provideNetworkConnectivityObserve(@ApplicationContext context: Context) = NetworkConnectivityObserve(context)
    @Provides
    fun provideAuthRepository(impl: AuthRepositoryImpl): AuthRepository = impl
    @Provides
    @Singleton
    fun provideFirebaseAuth() = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideCredentialManager(@ApplicationContext context: Context) = androidx.credentials.CredentialManager.create(context)

    @Provides
    @Singleton
    fun provideGetGoogleInOption() = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(WEB_CLIENT_ID)
        .build()

    @Provides
    @Singleton
    fun provideGetCredentialRequest(googleIdOption: GetGoogleIdOption) = androidx.credentials.GetCredentialRequest.Builder().addCredentialOption(googleIdOption)
        .build()

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context) = context
    @Provides
    fun provideGoogleSignInOptions(
        app: Application,
    ) = GoogleSignInOptions
        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(WEB_CLIENT_ID)
        .requestEmail()
        .build()

    @Provides
    fun provideGoogleSignInClient(
        app: Application,
        options: GoogleSignInOptions
    ) = GoogleSignIn.getClient(app, options)

}