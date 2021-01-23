data class CsvFile(val filename: String, val rawData: List<Map<String, String>>)

val CsvFile.fullName get() = "$filename.csv"

fun Localization.toCsvFile(): CsvFile {
    val data = texts.map { it.toCsvRow() }
    return CsvFile(file, data)
}

@OptIn(ExperimentalStdlibApi::class)
private fun Text.toCsvRow(): Map<String, String> = buildMap {
    put(COL_ID, id)
    for ((locale, content) in variants) {
        put(locale, content.string())
    }
}

private fun PreEditedText.string() = when (this) {
    is Skipped -> "*"
    is Edited -> result
    is Empty -> ""
}