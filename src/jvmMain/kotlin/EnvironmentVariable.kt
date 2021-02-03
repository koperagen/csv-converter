import kotlin.coroutines.CoroutineContext

fun env(key: String, default: String): String {
    return System.getenv(key) ?: default
}

interface EnvironmentVariable<T> {
    val key: String
    val default: String

    fun convert(value: String): T
}

fun <V, T: EnvironmentVariable<V>> env(spec: T): V {
    return spec.convert(env(spec.key, spec.default))
}

class LocalizationSpec(val ioContext: CoroutineContext) : EnvironmentVariable<LocalizationContext> {
    override val key: String = "localization"
    override val default: String = "dummy"

    override fun convert(value: String): LocalizationContext {
        println(value)
        return when (value) {
            "dummy" -> PreTranslatedLocalizationContext(TranslationService.DUMMY)
            "google" -> PreTranslatedLocalizationContext(GoogleTranslate(ioContext, googleTranslateService()))
            else -> error("$key enviroment variable should be either 'dummy' or 'google'")
        }
    }
}