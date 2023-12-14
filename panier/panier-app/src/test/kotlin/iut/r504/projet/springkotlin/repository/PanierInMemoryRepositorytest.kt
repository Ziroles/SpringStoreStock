package iut.r504.projet.springkotlin.repository

import assertk.assertThat
import assertk.assertions.*
import iut.r504.projet.springkotlin.domain.ArticleQuantite
import iut.r504.projet.springkotlin.domain.Panier
import iut.r504.projet.springkotlin.repository.PanierInMemoryRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PanierInMemoryRepositoryTest {

    private lateinit var repository: PanierInMemoryRepository

    @BeforeEach
    fun setUp() {
        repository = PanierInMemoryRepository()
    }

    @Test
    fun `create panier successfully`() {

        val panier = Panier("user@example.com", mutableListOf())


        val result = repository.create(panier)


        assertThat(result).isSuccess().isEqualTo(panier)
        assertThat(repository.list()).containsExactly(panier)
    }

    @Test
    fun `create panier with existing id fails`() {

        val userEmail = "user@example.com"
        val panier1 = Panier(userEmail, mutableListOf())
        val panier2 = Panier(userEmail, mutableListOf())


        repository.create(panier1)
        val result = repository.create(panier2)


        assertThat(result).isFailure().hasMessage("Panier already exists")
    }

    @Test
    fun `list all paniers`() {

        val panier1 = Panier("user1@example.com", mutableListOf())
        val panier2 = Panier("user2@example.com", mutableListOf())


        repository.create(panier1)
        repository.create(panier2)


        assertThat(repository.list()).containsExactly(panier1, panier2)
    }

    @Test
    fun `get panier by id`() {

        val userEmail = "user@example.com"
        val panier = Panier(userEmail, mutableListOf())


        repository.create(panier)


        assertThat(repository.get(userEmail)).isEqualTo(panier)
    }

    @Test
    fun `update existing panier`() {

        val userEmail = "user@example.com"
        val panier = Panier(userEmail, mutableListOf())
        val updatedPanier = Panier(userEmail, mutableListOf(ArticleQuantite(1, 2)))


        repository.create(panier)
        val result = repository.update(updatedPanier)


        assertThat(result).isSuccess().isEqualTo(updatedPanier)
        assertThat(repository.get(userEmail)).isEqualTo(updatedPanier)
    }

    @Test
    fun `update non-existing panier fails`() {

        val panier = Panier("user@example.com", mutableListOf())


        val result = repository.update(panier)


        assertThat(result).isFailure().hasMessage("Panier doesn't exist")
    }

    @Test
    fun `delete existing panier`() {

        val userEmail = "user@example.com"
        val panier = Panier(userEmail, mutableListOf())


        repository.create(panier)
        val result = repository.delete(userEmail)


        assertThat(result).isEqualTo(panier)
        assertThat(repository.list()).isEmpty()
    }

    @Test
    fun `delete non-existing panier returns null`() {


        val userEmail = "user@example.com"


        val result = repository.delete(userEmail)


        assertThat(result).isNull()
    }
}
