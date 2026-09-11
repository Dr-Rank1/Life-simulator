package com.maisha.game.di

import com.maisha.game.data.remote.AdviceApi
import com.maisha.game.data.remote.AdviceHttpClient
import com.maisha.game.data.remote.BookApi
import com.maisha.game.data.remote.BookHttpClient
import com.maisha.game.data.remote.CarMakeApi
import com.maisha.game.data.remote.CarMakeHttpClient
import com.maisha.game.data.remote.InsultApi
import com.maisha.game.data.remote.InsultHttpClient
import com.maisha.game.data.remote.JsonHttpGateway
import com.maisha.game.data.remote.QuoteApi
import com.maisha.game.data.remote.QuoteHttpClient
import com.maisha.game.data.remote.RandomUserApi
import com.maisha.game.data.remote.RandomUserHttpClient
import com.maisha.game.data.remote.SpaceNewsApi
import com.maisha.game.data.remote.SpaceNewsHttpClient
import com.maisha.game.data.remote.StreetApi
import com.maisha.game.data.remote.StreetHttpClient
import com.maisha.game.data.remote.TriviaApi
import com.maisha.game.data.remote.TriviaHttpClient
import com.maisha.game.data.remote.WantedApi
import com.maisha.game.data.remote.WantedHttpClient
import com.maisha.game.data.remote.ArtApi
import com.maisha.game.data.remote.ArtHttpClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(2, TimeUnit.SECONDS)
        .readTimeout(4, TimeUnit.SECONDS)
        .writeTimeout(4, TimeUnit.SECONDS)
        .callTimeout(6, TimeUnit.SECONDS)
        .retryOnConnectionFailure(false)
        .build()

    @Provides
    @Singleton
    @Named("content")
    fun provideContentOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(false)
        .build()

    @Provides
    @Singleton
    fun provideJsonHttpGateway(okHttpClient: OkHttpClient): JsonHttpGateway =
        JsonHttpGateway(okHttpClient)

    @Provides
    @Singleton
    @Named("content")
    fun provideContentJsonHttpGateway(@Named("content") okHttpClient: OkHttpClient): JsonHttpGateway =
        JsonHttpGateway(okHttpClient)

    @Provides
    @Singleton
    fun provideRandomUserApi(okHttpClient: OkHttpClient): RandomUserApi =
        RandomUserHttpClient(okHttpClient)

    @Provides
    @Singleton
    fun provideAdviceApi(gateway: JsonHttpGateway): AdviceApi = AdviceHttpClient(gateway)

    @Provides
    @Singleton
    fun provideTriviaApi(gateway: JsonHttpGateway): TriviaApi = TriviaHttpClient(gateway)

    @Provides
    @Singleton
    fun provideCarMakeApi(@Named("content") gateway: JsonHttpGateway): CarMakeApi =
        CarMakeHttpClient(gateway)

    @Provides
    @Singleton
    fun provideInsultApi(@Named("content") gateway: JsonHttpGateway): InsultApi =
        InsultHttpClient(gateway)

    @Provides
    @Singleton
    fun provideBookApi(@Named("content") gateway: JsonHttpGateway): BookApi =
        BookHttpClient(gateway)

    @Provides
    @Singleton
    fun provideQuoteApi(@Named("content") gateway: JsonHttpGateway): QuoteApi =
        QuoteHttpClient(gateway)

    @Provides
    @Singleton
    fun provideStreetApi(@Named("content") gateway: JsonHttpGateway): StreetApi =
        StreetHttpClient(gateway)

    @Provides
    @Singleton
    fun provideArtApi(@Named("content") gateway: JsonHttpGateway): ArtApi =
        ArtHttpClient(gateway)

    @Provides
    @Singleton
    fun provideWantedApi(@Named("content") gateway: JsonHttpGateway): WantedApi =
        WantedHttpClient(gateway)

    @Provides
    @Singleton
    fun provideSpaceNewsApi(@Named("content") gateway: JsonHttpGateway): SpaceNewsApi =
        SpaceNewsHttpClient(gateway)
}
