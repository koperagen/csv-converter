@ExperimentalStdlibApi
suspend fun LocalizationContext.process(
    localization: Localization,
    source: PropertiesFile
): LocalizationResult {
    val cache = localization.cache()
    val localizedFiles = localize(cache, source)
    val updatedLocalization = localization.update(localizedFiles)
    return LocalizationResult(localizedFiles, updatedLocalization)
}

data class LocalizationResult(val files: List<PropertiesFile>, val localization: Localization)