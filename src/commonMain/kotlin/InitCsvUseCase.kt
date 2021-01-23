@OptIn(ExperimentalStdlibApi::class)
fun init(file: PropertiesFile, locales: Set<Locale>): CsvFile {
    require(file.locale !in locales)

    fun toCsvRow(it: Row) = buildMap<String, String>(2 + locales.size) {
        put(COL_ID, it.id)
        put(file.locale, it.text)
        for (locale in locales) {
            put(it.id, "")
        }
    }

    val data = file.contents.map { toCsvRow(it) }
    return CsvFile(file.groupName, data)
}

