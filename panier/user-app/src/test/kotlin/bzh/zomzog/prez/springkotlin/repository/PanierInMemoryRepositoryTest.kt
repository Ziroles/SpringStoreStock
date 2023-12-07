package iut.r504.projet.springkotlin.repository
import org.junit.jupiter.api.BeforeEach

class PanierInMemoryRepositoryTest: PanierDatabaseTest() {

    @BeforeEach
    fun setUp() {
        repository = PanierInMemoryRepository()
    }
}