import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import java.io.File

fun main() {
    val inputDirectory = env(FileSpec("CSV_CONV_INPUT", "input"))
    val outputDirectory = env(FileSpec("CSV_CONV_OUTPUT", "output"))
    val files = inputDirectory.walkTopDown().filter { it.extension == "properties" }
    for (file in files) {
        initCsv(file, "ru", outputDirectory, setOf("en"))
    }
}

internal fun initCsv(input: File, inputLocale: Locale, outputDirectory: File, additionalLocales: Set<Locale>) {
    val properties = propertiesFile(input, inputLocale)
    val csv = properties.toCsv(additionalLocales)
    File(outputDirectory, csv.filename).outputStream().use {
        csvWriter().write(csv, it)
    }
}

internal fun propertiesFile(file: File, locale: Locale, groupExtractor: (File) -> String = withoutExtension): PropertiesFile {
    val group = groupExtractor(file)
    val contents = file.useLines { it.asIterable().parse() }
    return PropertiesFile(group, locale, contents)
}

val withoutExtension = { file: File ->  file.nameWithoutExtension }
val withoutLocaleSuffix = { file: File -> file.nameWithoutExtension.substringBeforeLast("_") }