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