import ParseResult.*

data class Localization(val file: String, val texts: List<Text>)

data class Text(val id: TextId, val variants: Map<Locale, String>)

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
    val data = buildList {
        for (row in rawData) {
            val id = row[COL_ID] ?: return Error.MissingIdError(row)
            val variants = row - COL_ID
            add(Text(id, variants))
        }
    }
    return Data(data)
}

sealed class ParseResult {
    data class Data(val data: List<Text>) : ParseResult()

    sealed class Error : ParseResult() {
        data class MissingIdError(val row: Map<String, String>) : Error()
    }
}

private const val COL_ID = "id"