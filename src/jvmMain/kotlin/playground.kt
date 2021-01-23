import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val context = PreTranslatedLocalizationContext(TranslationService.DUMMY)
    val filename = "test.csv"
    val sampleLocales = listOf("en", "de")
    val rawData = List(10) {
        mapOf(
            COL_ID to "id$it",
            *sampleLocales.zip(listOf("text", "")).toTypedArray()
        )
    }
    val contents = List(10) {
        Row("id$it", "text$it")
    }
    val source = PropertiesFile("basic_lang", "ru", contents)
    val result = context.process(filename, rawData, source)
    when (result) {
        is Success -> println(result.files.joinToString())
        is Failure -> println(result.reason)
    }
}