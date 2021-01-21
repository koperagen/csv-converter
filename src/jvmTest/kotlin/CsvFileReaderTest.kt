import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CsvFileReaderTest {
    lateinit var data: List<Map<String, String>>

    @BeforeTest
    fun init() {
        val csv = javaClass.getResourceAsStream("empty_cells.csv")
        data = csvReader().readAllWithHeader(csv)
    }

    @Test
    fun empty_cells_are_present_in_resulting_map() {
        data.forEach {
            assertEquals(actual = setOf("Col1", "Col2", "Col3"), expected = it.keys)
        }
    }

    @Test
    fun empty_cells_are_empty_strings() {
        assertEquals(mapOf("Col1" to "11", "Col2" to "", "Col3" to "13"), data[0])
    }
}