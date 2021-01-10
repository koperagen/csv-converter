data class Row(val id: TextId, val text: String)

data class PropertiesFile(val file: String, val contents: List<Row>)

fun Iterable<String>.parse(): Iterable<Row> =
    filterNot { it.startsWith('#') || it.isBlank() }
    .map {
        Row(
            id = it.substringBefore('='),
            text = it.substringAfter('=')
        )
    }

interface LocalizationContext {
    suspend fun translate(source: String, translation: Translation): String

    data class Translation(val from: Locale, val to: Locale)
}

suspend fun LocalizationContext.localize(
    file: PropertiesFile,
    locales: List<Locale>
): Localization = TODO()