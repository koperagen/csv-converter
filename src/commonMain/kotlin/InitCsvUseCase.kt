@OptIn(ExperimentalStdlibApi::class)
fun PropertiesFile.toCsv(locales: Set<Locale>): CsvFile {
    require(locale !in locales)

    fun toCsvRow(it: Row) = buildMap<String, String>(2 + locales.size) {
        put(COL_ID, it.id)
        put(locale, it.text)
        for (locale in locales) {
            put(locale, "")
        }
    }

    val data = contents.map { toCsvRow(it) }
    return CsvFile(groupName, data)
}

