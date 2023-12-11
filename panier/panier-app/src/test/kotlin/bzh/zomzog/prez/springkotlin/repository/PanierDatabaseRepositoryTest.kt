package iut.r504.projet.springkotlin.repository

import assertk.assertThat
import assertk.assertions.containsExactly
import iut.r504.projet.springkotlin.controller.dto.toArticleQuantiteDTO
import iut.r504.projet.springkotlin.domain.Panier
import iut.r504.projet.springkotlin.domain.ArticleQuantite
import iut.r504.projet.springkotlin.repository.entity.ArticleQuantiteEntity
import iut.r504.projet.springkotlin.repository.entity.PanierEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class PanierDatabaseRepositoryTest : PanierDatabaseTest() {

    @Autowired
    private lateinit var jpa: PanierJpaRepository

    @BeforeEach
    fun setUp() {
        repository = PanierDatabaseRepository(jpa)
        jpa.deleteAll()
    }

    @Test
    fun `list all return items filtered by user email`() {
        // GIVEN
        jpa.save(PanierEntity(1, "user1@email.com", listOf(ArticleQuantiteEntity(1, 2))))
        jpa.save(PanierEntity(2, "user2@email.com", listOf(ArticleQuantiteEntity(3, 4))))
        jpa.save(PanierEntity(3, "user1@email.com", listOf(ArticleQuantiteEntity(5, 6))))
        // WHEN
        val result = repository.list()
        // THEN
        assertThat(result).containsExactly(
                Panier(1, "user1@email.com", listOf(ArticleQuantite(1, 2))),
                Panier(2, "user2@email.com", listOf(ArticleQuantite(3, 4))),
                Panier(3, "user1@email.com", listOf(ArticleQuantite(5, 6)))
        )
    }
}
