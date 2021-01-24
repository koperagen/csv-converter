@ExperimentalStdlibApi
suspend fun LocalizationContext.process(
    csvFile: CsvFile,
    source: PropertiesFile
): LocalizationResult {
    val result = parse(csvFile.rawData)
    val localization = when (result) {
        is ParseResult.Data -> Localization(csvFile.name, source.locale, result.locales, result.data)
        is ParseResult.Error -> return Failure(result.toString())
    }
    val cache = localization.cache()
    val localizedFiles = localize(cache, source)
    val updatedLocalization = localization.update(localizedFiles)
    return Success(localizedFiles, updatedLocalization)
}

sealed class LocalizationResult
data class Success(val files: List<PropertiesFile>, val localization: Localization) : LocalizationResult()
data class Failure(val reason: String) : LocalizationResult()