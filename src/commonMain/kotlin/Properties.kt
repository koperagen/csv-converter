data class Row(val id: TextId, val text: String)

data class PropertiesFile(val groupName: String, val locale: Locale, val contents: List<Row>)

fun Iterable<String>.parse(): Iterable<Row> =
    filterNot { it.startsWith('#') || it.isBlank() }
    .map {
        Row(
            id = it.substringBefore('='),
            text = it.substringAfter('=')
        )
    }

interface TranslationService {
    suspend fun translate(text: String, translation: Translation): String
    suspend fun translate(texts: List<String>, translation: Translation): List<String>

    data class Translation(val from: Locale, val to: Locale)
}

interface LocalizationContext {
    suspend fun translate(
        cache: Map<TextId, PreEditedText>,
        rows: List<Row>,
        translation: TranslationService.Translation
    ): List<Row>
}

class PreTranslatedLocalizationContext(
    private val service: TranslationService
) : LocalizationContext {

    override suspend fun translate(
        cache: Map<TextId, PreEditedText>,
        rows: List<Row>,
        translation: TranslationService.Translation
    ): List<Row> {
        val candidates = rows.filterNot { cache[it.id] is Skipped }
        val alreadyTranslated = candidates.mapNotNull { row ->
            (cache[row.id] as? Edited)?.let { row.copy(text = it.result) }
        }
        val toBeTranslated = candidates.filter { cache[it.id] == null }
        val (ids, texts) = toBeTranslated.map { it.id to it.text }.unzip()
        val translatedTexts = service.translate(texts, translation)
        return alreadyTranslated + recoverIds(ids, translatedTexts)
    }

    private fun recoverIds(ids: List<TextId>, texts: List<String>): List<Row> {
        return ids.mapIndexed { index, id -> Row(id, texts[index]) }
    }

}

sealed class PreEditedText {
    companion object {
        fun of(s: String): PreEditedText {
            return when (s) {
                "*" -> Skipped
                "" -> Empty
                else -> Edited(s)
            }
        }
    }
}

object Skipped : PreEditedText()
data class Edited(val result: String) : PreEditedText()
object Empty : PreEditedText()

suspend fun LocalizationContext.localize(
    cache: Map<TextId, PreEditedText>,
    file: PropertiesFile,
    locales: List<Locale>
): List<PropertiesFile> {
    return locales.map {
        val options = TranslationService.Translation(file.locale, it)
        val localizedContent = translate(cache, file.contents, options)

        file.copy(locale = it, contents = localizedContent)
    }
}