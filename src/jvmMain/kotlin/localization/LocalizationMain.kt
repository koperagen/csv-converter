import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.File

private const val OUTPUT_DIRECTORY = "/home/nikita/IdeaProjects/csv-converter/output/translated"

private const val CSV_DIRECTORY = "/home/nikita/IdeaProjects/csv-converter/output/translated"
private const val PROPERTIES_DIRECTORY = "/home/nikita/AndroidStudioProjects/hackersimulator/android/src/main/assets/lang/"

@ExperimentalStdlibApi
fun main(): Unit = runBlocking {
    val context = PreTranslatedLocalizationContext(GoogleTranslate(Dispatchers.IO, googleTranslateService()))
    val csvReader = csvReader()
    val locales = setOf("en")
    val sourceLocale = "ru"

    val propertiesFiles = File(PROPERTIES_DIRECTORY)
        .walkTopDown()
        .filter { it.extension == "properties" }
        .map { propertiesFile(it, sourceLocale) }
        .toList()

    val csvFiles = propertiesFiles.map { propertiesFile ->
        val csvFile = File(CSV_DIRECTORY, "${propertiesFile.groupName}.csv")
        if (csvFile.exists()) {
            csvReader
                .read(csvFile)
                .toLocalization(sourceLocale)
                ?.update(propertiesFile)
        } else {
            init(propertiesFile, locales).toLocalization(sourceLocale)
        } ?: error("Failed to parse CSV file for ${propertiesFile.fullName}")
    }

    propertiesFiles.zip(csvFiles) { properties, csv ->
        context.applyLocalization(csv, properties, File(OUTPUT_DIRECTORY))
    }
}

@ExperimentalStdlibApi
private suspend fun LocalizationContext.applyLocalization(
    localization: Localization,
    properties: PropertiesFile,
    outputDirectory: File
) {
    val result = process(localization, properties)
    val (files, updatedLocalization) = result
    println(files)
    files.forEach {
        val file = outputDirectory.resolve(it.fullName)
        file.writeText(it.contentString)
    }
    val updatedCsv = updatedLocalization.toCsvFile()
    val file = outputDirectory.resolve(updatedCsv.filename)
    csvWriter().write(updatedCsv, file.outputStream())
}

private fun CsvReader.read(file: File): CsvFile {
    val rawData = readAllWithHeader(file)
    return CsvFile(file.nameWithoutExtension, rawData)
}