import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.File

fun readRaw(file: File): List<Map<String, String>> {
    val reader = csvReader()
    return reader.readAllWithHeader(file)
}