import ParseResult.*

data class Localization(val file: String, val locales: Set<Locale>, val texts: List<Text>)

data class Text(val id: TextId, val variants: Map<Locale, PreEditedText>)

typealias TextId = String

typealias Locale = String

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

private const val COL_ID = "id"