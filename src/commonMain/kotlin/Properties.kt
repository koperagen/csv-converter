data class Row(val id: TextId, val text: String)

data class PropertiesFile(val groupName: String, val locale: Locale, val contents: List<Row>)

val PropertiesFile.fullName: String get() = "${groupName}_$locale.properties"

val PropertiesFile.contentString: String get() = contents.joinToString("\n") {
    "${it.id}=${it.text}"
}

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

    data class Cache(val locale: Locale, val map: Map<TextId, PreEditedText>)
}

fun Localization.cache(): List<LocalizationContext.Cache> {
    return locales.map { locale ->
        val map = texts.associate {
            it.id to (it.variants[locale] ?: error("Localization file $file misses locale $locale for id ${it.id}"))
        }
        LocalizationContext.Cache(locale, map)
    }
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
    translations: List<LocalizationContext.Cache>,
    file: PropertiesFile
): List<PropertiesFile> {
    return translations.map { (locale, cache) ->
        val options = TranslationService.Translation(file.locale, locale)
        val localizedContent = translate(cache, file.contents, options)

        file.copy(locale = locale, contents = localizedContent)
    }
}

