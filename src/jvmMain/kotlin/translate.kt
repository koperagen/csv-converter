import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.translate.*
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import kotlin.coroutines.CoroutineContext

private const val PATH = "/home/nikita/Downloads/HS Translation experimental-08b0557864e6.json"

class GoogleTranslate(
    private val ioContext: CoroutineContext,
    private val service: Translate
) : TranslationService {

    override suspend fun translate(text: String, translation: TranslationService.Translation): String =
        withContext(ioContext) {
            service
                .translate(text, *translateOptions(translation))
                .translatedText
        }

    override suspend fun translate(texts: List<String>, translation: TranslationService.Translation): List<String> =
        withContext(ioContext) {
            if (texts.isEmpty()) return@withContext emptyList()
            service
                .translate(texts, *translateOptions(translation))
                .map { it.translatedText }
        }

    private companion object {
        private fun translateOptions(translation: TranslationService.Translation) =
            arrayOf(
                Translate.TranslateOption.sourceLanguage(translation.from),
                Translate.TranslateOption.targetLanguage(translation.to),
                Translate.TranslateOption.model("base")
            )
    }
}

fun googleTranslateService(): Translate {
    val credentials = GoogleCredentials.fromStream(FileInputStream(PATH))
    return TranslateOptions.newBuilder().setCredentials(credentials).build().service
}

fun main() {
    val translate = googleTranslateService()
    val translation = translate.translate("¡Hola Mundo!")
    System.out.printf("Translated Text:\n\t%s\n", translation.translatedText)
}