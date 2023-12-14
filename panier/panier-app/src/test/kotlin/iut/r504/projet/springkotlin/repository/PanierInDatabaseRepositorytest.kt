package iut.r504.projet.springkotlin.repository
import assertk.assertThat
import assertk.assertions.*
import iut.r504.projet.springkotlin.domain.ArticleQuantite
import iut.r504.projet.springkotlin.domain.Panier
import iut.r504.projet.springkotlin.repository.PanierDatabaseRepository
import iut.r504.projet.springkotlin.repository.PanierJpaRepository
import iut.r504.projet.springkotlin.repository.entity.PanierEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class PanierDatabaseRepositoryTest {

    @Autowired
    private lateinit var jpa: PanierJpaRepository

    private lateinit var repository: PanierDatabaseRepository

    @BeforeEach
    fun setUp() {
        repository = PanierDatabaseRepository(jpa)
        jpa.deleteAll()
    }

    @Test
    fun `create panier successfully`() {

        val panier = Panier("user@example.com", mutableListOf(ArticleQuantite(1, 2)))


        val result = repository.create(panier)


        assertThat(result).isSuccess().isEqualTo(panier)
        assertThat(repository.list()).containsExactly(panier)
    }

    @Test
    fun `create panier with existing id fails`() {

        val userEmail = "user@example.com"
        val panier1 = Panier(userEmail, mutableListOf(ArticleQuantite(1, 2)))
        val panier2 = Panier(userEmail,mutableListOf(ArticleQuantite(1, 2)))


        repository.create(panier1)
        val result = repository.create(panier2)


        assertThat(result).isFailure().hasMessage("Panier already in DB")
    }

    @Test
    fun `list all paniers`() {

        val panier1 = Panier("user1@example.com", mutableListOf(ArticleQuantite(1, 2)))
        val panier2 = Panier("user2@example.com", mutableListOf(ArticleQuantite(3, 4)))


        repository.create(panier1)
        repository.create(panier2)


        assertThat(repository.list()).containsExactly(panier1, panier2)
    }

    @Test
    fun `get panier by id`() {

        val userEmail = "user@example.com"
        val panier = Panier(userEmail, mutableListOf(ArticleQuantite(1, 2)))


        repository.create(panier)


        assertThat(repository.get(userEmail)).isEqualTo(panier)
    }

    @Test
    fun `get non-existing panier returns null`() {

        val userEmail = "nonexistent@example.com"


        val result = repository.get(userEmail)


        assertThat(result).isNull()
    }

    @Test
    fun `update existing panier`() {

        val userEmail = "user@example.com"
        val panier = Panier(userEmail, mutableListOf(ArticleQuantite(1, 2)))
        val updatedPanier = Panier(userEmail, mutableListOf(ArticleQuantite(3, 4)))


        repository.create(panier)
        val result = repository.update(updatedPanier)


        assertThat(result).isSuccess().isEqualTo(updatedPanier)
        assertThat(repository.get(userEmail)).isEqualTo(updatedPanier)
    }

    @Test
    fun `update non-existing panier fails`() {

        val panier = Panier("nonexistent@example.com", mutableListOf(ArticleQuantite(1, 2)))


        val result = repository.update(panier)


        assertThat(result).isFailure().hasMessage("Panier not in DB")
    }

    @Test
    fun `delete existing panier`() {

        val userEmail = "user@example.com"
        val panier = Panier(userEmail, mutableListOf(ArticleQuantite(1, 2)))


        repository.create(panier)
        val result = repository.delete(userEmail)


        assertThat(result).isEqualTo(panier)
        assertThat(repository.list()).isEmpty()
    }

    @Test
    fun `delete non-existing panier returns null`() {

        val userEmail = "nonexistent@example.com"


        val result = repository.delete(userEmail)


        assertThat(result).isNull()
    }
}
