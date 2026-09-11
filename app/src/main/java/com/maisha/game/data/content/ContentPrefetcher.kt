package com.maisha.game.data.content

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads cached procedural content into memory, then refreshes from the network
 * in the background. Gameplay only reads catalogs.
 */
@Singleton
class ContentPrefetcher @Inject constructor(
    private val adviceRepository: AdviceRepository,
    private val triviaRepository: TriviaRepository,
    private val carMakeRepository: CarMakeRepository,
    private val insultRepository: InsultRepository,
    private val bookRepository: BookRepository,
    private val quoteRepository: QuoteRepository,
    private val streetRepository: StreetRepository,
    private val artRepository: ArtRepository,
    private val wantedRepository: WantedRepository,
    private val spaceNewsRepository: SpaceNewsRepository
) {

    suspend fun hydrateFromDisk() {
        adviceRepository.hydrateFromDisk()
        triviaRepository.hydrateFromDisk()
        carMakeRepository.hydrateFromDisk()
        insultRepository.hydrateFromDisk()
        bookRepository.hydrateFromDisk()
        quoteRepository.hydrateFromDisk()
        streetRepository.hydrateFromDisk()
        artRepository.hydrateFromDisk()
        wantedRepository.hydrateFromDisk()
        spaceNewsRepository.hydrateFromDisk()
    }

    suspend fun refreshFromNetwork() {
        adviceRepository.refresh()
        triviaRepository.refresh()
        carMakeRepository.refresh()
        insultRepository.refresh()
        bookRepository.refresh()
        quoteRepository.refresh()
        artRepository.refresh()
        wantedRepository.refresh()
        spaceNewsRepository.refresh()
        streetRepository.refresh()
    }
}
