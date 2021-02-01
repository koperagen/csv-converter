import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import java.io.File

private const val PATH = "/home/nikita/AndroidStudioProjects/hackersimulator/android/src/main/assets/lang/basic_lang.properties"
private const val DEST = "/home/nikita/IdeaProjects/csv-converter/output"

private const val INPUT_DIRECTORY = "/home/nikita/AndroidStudioProjects/hackersimulator/android/src/main/assets/lang/"

fun main() {
    val files = File(INPUT_DIRECTORY).walkTopDown().filter { it.extension == "properties" }
    for (file in files) {
        initCsv(file, "ru", File(DEST), setOf("en"))
    }
}

internal fun initCsv(input: File, inputLocale: Locale, outputDirectory: File, locales: Set<Locale>) {
    val properties = propertiesFile(input, inputLocale)
    val csv = init(properties, locales)
    File(outputDirectory, csv.filename).outputStream().use {
        csvWriter().write(csv, it)
    }
}

internal fun propertiesFile(file: File, locale: Locale): PropertiesFile {
    val group = file.nameWithoutExtension
    val contents = file.useLines { it.asIterable().parse() }
    return PropertiesFile(group, locale, contents)
}