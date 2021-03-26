@OptIn(ExperimentalStdlibApi::class)
fun PropertiesFile.toCsv(additionalLocales: Set<Locale>): CsvFile {
    require(locale !in additionalLocales)

    fun toCsvRow(it: Row) = buildMap<String, String>(2 + additionalLocales.size) {
        put(COL_ID, it.id)
        put(locale, it.text)
        for (locale in additionalLocales) {
            put(locale, "")
        }
    }

    val data = contents.map { toCsvRow(it) }
    return CsvFile(groupName, data)
}

