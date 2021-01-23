import ParseResult.*

data class Localization(val file: String, val locales: Set<Locale>, val texts: List<Text>)

data class Text(val id: TextId, val variants: Map<Locale, PreEditedText>)

typealias TextId = String

typealias Locale = String

fun Localization.update(file: PropertiesFile): Localization {
    require(file.locale in locales)

    val translatedRows = file.contents
    val textsMap = texts.associateByTo(mutableMapOf()) { it.id }

    fun modifyText(row: Row, originalText: Text) {
        val translatedVariants = originalText
            .variants
            .toMutableMap()
            .also { it[file.locale] = Edited(row.text) }
            .toMap()

        textsMap[row.id] = originalText.copy(variants = translatedVariants)
    }

    fun addNewText(row: Row) {
        val translatedVariants = locales.associateWithTo(mutableMapOf<TextId, PreEditedText>()) { Empty }
        translatedVariants[file.locale] = Edited(row.text)
        textsMap[row.id] = Text(row.id, translatedVariants)
    }

    translatedRows.forEach { row ->
        val originalText = textsMap[row.id]
        if (originalText != null) {
            modifyText(row, originalText)
        } else {
            addNewText(row)
        }
    }
    val editedTexts = textsMap.values.toList()
    return copy(texts = editedTexts)
}

fun Localization.update(files: List<PropertiesFile>): Localization {
    return files.fold(this) { acc, file ->
        acc.update(file)
    }
}

/*
 |  COL_ID |  en_EN  |   ....  |  ru_RU  |
 |---------|---------|---------|---------|
 |    id   |  var1   |   ....  |   var3  |
 |   ....  |  ....   |   ....  |   ....  |
 |   idN   |  var1   |   ....  |   var3  |
 -----------------------------------------
 */
@OptIn(ExperimentalStdlibApi::class)
fun parse(rawData: List<Map<String, String>>): ParseResult {
    val anyRow = rawData.firstOrNull() ?: return Error.EmptyFile
    val locales: Set<Locale> = anyRow.keys - COL_ID

    val data = buildList {
        for (row in rawData) {
            val id = row[COL_ID] ?: return Error.MissingIdError(row)
            val variants = (row - COL_ID)
            if (variants.keys != locales) return Error.DifferentKeySet(locales, variants)
            add(Text(id, variants.mapValues { (_, value) -> PreEditedText.of(value) }))
        }
    }
    return Data(locales, data)
}

sealed class ParseResult {
    data class Data(val locales: Set<Locale>, val data: List<Text>) : ParseResult()

    sealed class Error : ParseResult() {
        data class MissingIdError(val row: Map<String, String>) : Error()
        object EmptyFile : Error()
        data class DifferentKeySet(val expectedKeys: Set<Locale> , val row: Map<String, String>) : Error()
    }
}

const val COL_ID = "id"