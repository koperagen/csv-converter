import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.File

private const val OUTPUT_DIRECTORY = "/home/nikita/IdeaProjects/csv-converter/output/translated"

private const val CSV_DIRECTORY = "/home/nikita/IdeaProjects/csv-converter/output/translated"
private const val PROPERTIES_DIRECTORY = "/home/nikita/AndroidStudioProjects/hackersimulator/android/src/main/assets/lang/"

interface Runtime {
  val context: LocalizationContext
  val csvReader: CsvReader
  val locales: Set<Locale>
  val sourceLocale: String

  val baseDirectory: File
  val outputDirectory: File
  val csvDirectory: File
}

@ExperimentalStdlibApi
fun main(): Unit = runBlocking {
    val context = PreTranslatedLocalizationContext(GoogleTranslate(Dispatchers.IO, googleTranslateService()))
    val csvReader = csvReader()
    val locales = setOf("en")
    val sourceLocale = "ru"

    val runtime = object : Runtime {
        override val context: LocalizationContext = context
        override val csvReader: CsvReader = csvReader
        override val locales: Set<Locale> = locales
        override val sourceLocale: String = sourceLocale

        override val baseDirectory: File = File(PROPERTIES_DIRECTORY)
        override val outputDirectory: File = File(OUTPUT_DIRECTORY)
        override val csvDirectory: File = File(CSV_DIRECTORY)
    }

    val directories = File(PROPERTIES_DIRECTORY)
      .walkTopDown()
      .filter { it.isDirectory }

    directories.forEach {
        runtime.processDirectory(it)
    }
}

@ExperimentalStdlibApi
private suspend fun Runtime.processDirectory(directory: File) {
    val relative = directory.relativeTo(baseDirectory)

    val propertiesFiles = directory
        .walkTopDown()
        .maxDepth(1)
        .filter { it.extension == "properties" }
        .map { propertiesFile(it, sourceLocale) }
        .toList()

    val relatedCsvDirectory = csvDirectory.resolve(relative)
    val csvFiles = propertiesFiles.map { propertiesFile ->
        val csvFile = File(relatedCsvDirectory, "${propertiesFile.groupName}.csv")
        if (csvFile.exists()) {
            csvReader
                .read(csvFile)
                .toLocalization(sourceLocale)
                ?.update(propertiesFile)
        } else {
            propertiesFile.toCsv(locales).toLocalization(sourceLocale)
        } ?: error("Failed to parse CSV file for ${propertiesFile.fullName}")
    }

    propertiesFiles.zip(csvFiles) { properties, csv ->
        context.applyLocalization(csv, properties, outputDirectory.resolve(relative))
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