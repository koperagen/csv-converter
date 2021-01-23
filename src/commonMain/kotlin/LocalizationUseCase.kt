suspend fun LocalizationContext.process(
    csvFilename: String,
    rawData: List<Map<String, String>>,
    source: PropertiesFile
): LocalizationResult {
    val result = parse(rawData)
    val localization = when (result) {
        is ParseResult.Data -> Localization(csvFilename, result.locales, result.data)
        is ParseResult.Error -> return Failure(result.toString())
    }
    val cache = localization.cache()
    val localizedFiles = localize(cache, source)
    return Success(localizedFiles)
}

sealed class LocalizationResult
data class Success(val files: List<PropertiesFile>) : LocalizationResult()
data class Failure(val reason: String) : LocalizationResult()