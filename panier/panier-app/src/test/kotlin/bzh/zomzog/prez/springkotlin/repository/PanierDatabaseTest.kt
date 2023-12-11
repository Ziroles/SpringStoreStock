package iut.r504.projet.springkotlin.repository

import assertk.assertThat
import assertk.assertions.*

import iut.r504.projet.springkotlin.domain.ArticleQuantite
import iut.r504.projet.springkotlin.domain.Panier
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

abstract class PanierDatabaseTest {

    lateinit var repository: PanierRepository

    @Nested
    inner class CreationTest {
        @Test
        fun `create once is ok`() {
            // GIVEN
            val panier = defaultPanier()
            // WHEN
            val result = repository.create(panier)
            // THEN
            assertThat(result).isSuccess()
                    .isEqualTo(panier)
        }

        @Test
        fun `create twice with same id is an error`() {
            // GIVEN
            val panier = defaultPanier()
            val panier2 = defaultPanier(userEmail = "another@email.pony")
            repository.create(panier)
            // WHEN
            val result = repository.create(panier2)
            // THEN
            assertThat(result).isFailure()
        }

        @Test
        fun `create twice with different id is ok`() {
            // GIVEN
            val panier = defaultPanier()
            val panier2 = defaultPanier(id = 2, userEmail = "another@email.pony")
            repository.create(panier)
            // WHEN
            val result = repository.create(panier2)
            // THEN
            assertThat(result).isSuccess()
                    .isEqualTo(panier2)
        }
    }

    @Nested
    inner class ListTests {
        @Test
        fun `list all paniers`() {
            // GIVEN
            val panier = defaultPanier()
            val panier2 = defaultPanier(id = 2, userEmail = "another@email.pony")
            val panier3 = defaultPanier(id = 3, userEmail = "anOld@email.pony", items = listOf(ArticleQuantite(1, 2)))
            repository.create(panier)
            repository.create(panier2)
            repository.create(panier3)
            // WHEN
            val result = repository.list()
            // THEN
            assertThat(result).containsExactlyInAnyOrder(panier, panier2, panier3)
        }

        @Test
        fun `list paniers filtered by user email`() {
            // GIVEN
            val panier = defaultPanier()
            val panier2 = defaultPanier(id = 2, userEmail = "another@email.pony")
            val panier3 = defaultPanier(id = 3, userEmail = "anOld@email.pony", items = listOf(ArticleQuantite(1, 2)))
            repository.create(panier)
            repository.create(panier2)
            repository.create(panier3)
            // WHEN
            val result = repository.list()
            // THEN
            assertThat(result).containsExactly(panier2)
        }
    }

    @Nested
    inner class GetTest {
        @Test
        fun `find existing one`() {
            // GIVEN
            val panier = defaultPanier()
            repository.create(panier)
            // WHEN
            val result = repository.get(panier.id)
            // THEN
            assertThat(result).isEqualTo(panier)
        }

        @Test
        fun `find one without paniers`() {
            // GIVEN
            // WHEN
            val result = repository.get(42)
            // THEN
            assertThat(result).isNull()
        }
    }

    @Nested
    inner class UpdatedTest {
        @Test
        fun `update an existing panier`() {
            // GIVEN
            val panier = defaultPanier()
            repository.create(panier)
            val update = defaultPanier(id = 1, userEmail = "newEmail", items = listOf(ArticleQuantite(3, 4)))
            // WHEN
            val result = repository.update(update)
            // THEN
            assertThat(result).isSuccess().isEqualTo(update)
        }

        @Test
        fun `update non-existing panier`() {
            // GIVEN
            val update = defaultPanier(id = 1, userEmail = "newEmail", items = listOf(ArticleQuantite(3, 4)))
            // WHEN
            val result = repository.update(update)
            // THEN
            assertThat(result).isFailure()
        }
    }

    @Nested
    inner class DeleteTests {
        @Test
        fun `delete an existing panier`() {
            // GIVEN
            val panier = defaultPanier()
            repository.create(panier)
            // WHEN
            val result = repository.delete(panier.id)
            // THEN
            assertThat(result).isEqualTo(panier)
        }

        @Test
        fun `update non-existing panier`() {
            // GIVEN
            // WHEN
            val result = repository.delete(42)
            // THEN
            assertThat(result).isNull()
        }
    }

    private fun defaultPanier(
            id: Long = 1,
            userEmail: String = "j@d.com",
            items: List<ArticleQuantite> = listOf(ArticleQuantite(1, 2))
    ) = Panier(id, userEmail, items)
}
