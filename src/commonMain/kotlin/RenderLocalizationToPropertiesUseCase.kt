fun Localization.toRefinedTextVariants(): List<RefinedTextVariants> {
    return locales.map { locale ->
        val empties = mutableListOf<TextId>()
        val nulls = mutableListOf<TextId>()
        val rows = mutableListOf<Row>()
        texts.forEach {
            when (val text = it.variants[locale]) {
                null -> nulls.add(it.id)
                is Skipped -> {}
                is Edited -> rows.add(Row(it.id, text.result))
                is Empty -> empties.add(it.id)
            }
        }
        RefinedTextVariants(locale, empties, nulls, rows)
    }
}

class RefinedTextVariants
internal constructor(val locale: Locale, val empties: List<TextId>, val nulls: List<TextId>, val rows: List<Row>)

fun Localization.toPropertiesFiles(name: String, function: (RefinedTextVariants) -> Unit): List<PropertiesFile> {
    val refinedTextVariants = toRefinedTextVariants()
    return refinedTextVariants
        .onEach(function)
        .map { PropertiesFile(name, it.locale, it.rows) }
}