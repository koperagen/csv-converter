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
    suspend fun translate(source: String, translation: Translation): String

    data class Translation(val from: Locale, val to: Locale)
}

interface LocalizationContext {
    suspend fun translate(row: Row, translation: TranslationService.Translation): Row
}

suspend fun LocalizationContext.localize(
    file: PropertiesFile,
    locales: List<Locale>
): List<PropertiesFile> {
    return locales.map {
        val localizedContent = file.contents.map { row ->
            translate(row, TranslationService.Translation(file.locale, it))
        }
        file.copy(locale = it, contents = localizedContent)
    }
}