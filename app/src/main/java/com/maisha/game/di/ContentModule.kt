package com.maisha.game.di

import com.maisha.game.data.content.AdviceCatalog
import com.maisha.game.data.content.AdviceRepository
import com.maisha.game.data.content.BookCatalog
import com.maisha.game.data.content.BookRepository
import com.maisha.game.data.content.CarMakeCatalog
import com.maisha.game.data.content.CarMakeRepository
import com.maisha.game.data.content.InsultCatalog
import com.maisha.game.data.content.InsultRepository
import com.maisha.game.data.content.QuoteCatalog
import com.maisha.game.data.content.QuoteRepository
import com.maisha.game.data.content.SpaceNewsCatalog
import com.maisha.game.data.content.SpaceNewsRepository
import com.maisha.game.data.content.StreetCatalog
import com.maisha.game.data.content.StreetRepository
import com.maisha.game.data.content.TriviaCatalog
import com.maisha.game.data.content.TriviaRepository
import com.maisha.game.data.content.WantedCatalog
import com.maisha.game.data.content.WantedRepository
import com.maisha.game.data.content.ArtCatalog
import com.maisha.game.data.content.ArtRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ContentModule {

    @Binds
    @Singleton
    abstract fun bindAdviceCatalog(impl: AdviceRepository): AdviceCatalog

    @Binds
    @Singleton
    abstract fun bindTriviaCatalog(impl: TriviaRepository): TriviaCatalog

    @Binds
    @Singleton
    abstract fun bindCarMakeCatalog(impl: CarMakeRepository): CarMakeCatalog

    @Binds
    @Singleton
    abstract fun bindInsultCatalog(impl: InsultRepository): InsultCatalog

    @Binds
    @Singleton
    abstract fun bindBookCatalog(impl: BookRepository): BookCatalog

    @Binds
    @Singleton
    abstract fun bindQuoteCatalog(impl: QuoteRepository): QuoteCatalog

    @Binds
    @Singleton
    abstract fun bindStreetCatalog(impl: StreetRepository): StreetCatalog

    @Binds
    @Singleton
    abstract fun bindArtCatalog(impl: ArtRepository): ArtCatalog

    @Binds
    @Singleton
    abstract fun bindWantedCatalog(impl: WantedRepository): WantedCatalog

    @Binds
    @Singleton
    abstract fun bindSpaceNewsCatalog(impl: SpaceNewsRepository): SpaceNewsCatalog
}
