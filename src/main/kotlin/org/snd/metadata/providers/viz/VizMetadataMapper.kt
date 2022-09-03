package org.snd.metadata.providers.viz

import org.snd.config.BookMetadataConfig
import org.snd.config.SeriesMetadataConfig
import org.snd.metadata.MetadataConfigApplier
import org.snd.metadata.model.Author
import org.snd.metadata.model.AuthorRole.*
import org.snd.metadata.model.BookMetadata
import org.snd.metadata.model.Provider.VIZ
import org.snd.metadata.model.ProviderBookId
import org.snd.metadata.model.ProviderBookMetadata
import org.snd.metadata.model.ProviderSeriesId
import org.snd.metadata.model.ProviderSeriesMetadata
import org.snd.metadata.model.SeriesBook
import org.snd.metadata.model.SeriesMetadata
import org.snd.metadata.model.SeriesMetadata.Status.ENDED
import org.snd.metadata.model.Thumbnail
import org.snd.metadata.providers.viz.model.VizBook
import org.snd.metadata.providers.viz.model.VizSeriesBook

class VizMetadataMapper(
    private val seriesMetadataConfig: SeriesMetadataConfig,
    private val bookMetadataConfig: BookMetadataConfig,
) {
    private val artistRoles = listOf(
        PENCILLER,
        INKER,
        COLORIST,
        LETTERER,
        COVER
    )

    fun toSeriesMetadata(book: VizBook, allBooks: Collection<VizSeriesBook>, thumbnail: Thumbnail? = null): ProviderSeriesMetadata {
        val metadata = SeriesMetadata(
            status = if (allBooks.any { it.final }) ENDED else null,
            title = book.seriesName,
            titleSort = book.seriesName,
            summary = book.description,
            publisher = book.publisher,
            ageRating = book.ageRating?.age,
            genres = book.genres,
            tags = emptyList(),
            totalBookCount = allBooks.size.let { if (it < 1) null else it },
            authors = getAuthors(book),
            thumbnail = thumbnail,
        )

        val providerMetadata = ProviderSeriesMetadata(
            id = ProviderSeriesId(book.id.id),
            provider = VIZ,
            metadata = metadata,
            books = allBooks.map {
                SeriesBook(
                    id = ProviderBookId(it.id.id),
                    number = it.number,
                    name = it.name,
                    type = null,
                    edition = null
                )
            }
        )

        return MetadataConfigApplier.apply(providerMetadata, seriesMetadataConfig)
    }

    fun toBookMetadata(book: VizBook, thumbnail: Thumbnail? = null): ProviderBookMetadata {
        val metadata = BookMetadata(
            title = book.name,
            summary = book.description,
            number = book.number,
            releaseDate = book.releaseDate,
            authors = getAuthors(book),
            isbn = book.isbn,
            startChapter = null,
            endChapter = null,
            thumbnail = thumbnail
        )

        val providerMetadata = ProviderBookMetadata(
            id = ProviderBookId(book.id.id),
            provider = VIZ,
            metadata = metadata
        )
        return MetadataConfigApplier.apply(providerMetadata, bookMetadataConfig)
    }

    private fun getAuthors(book: VizBook): List<Author> {
        val authorsArt = book.authorArt?.let { name ->
            artistRoles.map { role -> Author(name, role) }
        } ?: emptyList()
        val authorStory = book.authorStory?.let { name -> Author(name, WRITER) }
        return authorStory?.let { authorsArt + it } ?: authorsArt
    }
}