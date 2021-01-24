import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import java.io.OutputStream

fun CsvWriter.write(csvFile: CsvFile, dest: OutputStream) {
    val rows = convert(csvFile)
    writeAll(rows, dest)
}

@OptIn(ExperimentalStdlibApi::class)
private fun convert(csvFile: CsvFile): List<List<String>> = buildList(csvFile.rawData.size + 1) {
    add(csvFile.headers)
    for (row in csvFile.rawData) {
        add(row.values.toList())
    }
}

fun write(propertiesFile: PropertiesFile, dest: OutputStream) {
    val str = convert(propertiesFile)
    dest.writer().write(str)
}

private fun convert(file: PropertiesFile): String {
    val sb = StringBuilder(file.contentLength())
    return file.contents
        .joinTo(sb, separator = "\n") { "${it.id}=${it.text}" }
        .toString()
}

private fun PropertiesFile.contentLength(): Int =
    contents.sumOf { it.id.length + it.text.length }