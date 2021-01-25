import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class ConstrainedChunksTest {

    @Test
    fun flattening_of_chunked_list_is_identity_if_items_less_than_limit(): Unit = runBlocking {
        checkAll(gen) {
            it.constrainedChunks(100).flatten() shouldBe it
        }
    }

    companion object {
        val gen = Arb.list(Arb.string(0, 100))
    }
}